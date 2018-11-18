package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.tuple.Tuple
import com.apple.foundationdb.{ StreamingMode, Transaction, KeyValue => JavaKeyValue }
import me.archdev.foundationdb._
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._

import scala.collection.JavaConverters._

object DatabaseInterpreter extends QueryAlgebra[DatabaseContext] {

  override def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.set(subspace.pack(key), value.toTuple.pack())
      null
    }

  override def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Option[V]] =
    transactionAction {
      _.get(subspace.pack(key)).thenApply(javaClojure(parseFDBOutput[V]))
    }

  override def selectKey[K: Tupler](key: KeySelector)(implicit s: Subspace): DatabaseContext[Option[SelectedKey[K]]] =
    transactionAction {
      _.getKey(key.raw).thenApply(javaClojure { key =>
        parseFDBKey[K](key)
          .map(SelectedKey(Some(s).filter(!_.isEmpty), _))
          .orElse {
            parseFDBKeyWithoutSubspace[K](key)
              .map(SelectedKey(None, _))
          }
      })
    }

  override def findKey[K: Tupler](key: KeySelector)(implicit s: Subspace): DatabaseContext[Option[K]] =
    transactionAction {
      _.getKey(key.raw).thenApply(javaClojure(parseFDBKey[K]))
    }

  override def getRange[K: Tupler, V: Tupler](
      range: (K, K)
  )(implicit s: Subspace): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2)).asList().thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2), limit).asList().thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2), limit, true)
        .asList()
        .thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def getRangeStream[K: Tupler, V: Tupler](
      range: (K, K),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): DatabaseContext[Iterator[KeyValue[K, V]]] =
    transactionAction { tr =>
      CompletableFuture.completedFuture(
        tr.getRange(s.pack(range._1), s.pack(range._2), limit, reverse, streamingMode)
          .iterator()
          .asScala
          .map(buildKeyValue[K, V])
      )
    }

  override def selectRange[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector)
  )(implicit subspace: Subspace): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw).asList().thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit subspace: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw, limit).asList().thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit subspace: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw, limit, true)
        .asList()
        .thenApply(javaClojure(parseFDBOutput[K, V]))
    }

  override def selectRangeStream[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit subspace: Subspace): DatabaseContext[Iterator[KeyValue[K, V]]] =
    transactionAction { tr =>
      CompletableFuture.completedFuture(
        tr.getRange(range._1.raw, range._2.raw, limit, reverse, streamingMode)
          .iterator()
          .asScala
          .map(buildKeyValue[K, V])
      )
    }

  override def clear[K: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.clear(subspace.pack(key))
      null
    }

  override def clearRange[K: Tupler](range: (K, K))(implicit s: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.clear(s.pack(range._1), s.pack(range._2))
      null
    }

  override def commit(): DatabaseContext[Unit] =
    transactionAction {
      _.commit().asInstanceOf[CompletableFuture[Unit]]
    }

  override def cancel(): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.cancel()
      null
    }

  override def close(): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.close()
      null
    }

  override def raw[V](f: Transaction => CompletableFuture[V]): DatabaseContext[V] =
    transactionAction(f)

  private def parseFDBOutput[A](output: Array[Byte])(implicit vs: Tupler[A]): Option[A] =
    Option(output).map(parseFDBObject[A])

  private def parseFDBKey[A: Tupler](output: Array[Byte])(implicit subspace: Subspace): Option[A] =
    Option(output)
      .filter(isFDBOutputMeaningful)
      .filter(subspace.raw.contains)
      .map(subspace.raw.unpack)
      .map(_.fromTuple[A])

  private def parseFDBKeyWithoutSubspace[A: Tupler](output: Array[Byte]): Option[A] =
    Option(output)
      .filter(isFDBOutputMeaningful)
      .map(Tuple.fromBytes)
      .map(_.fromTuple[A])

  private def parseFDBOutput[K: Tupler, V: Tupler](
      output: java.util.List[JavaKeyValue]
  )(implicit subspace: Subspace): Seq[KeyValue[K, V]] =
    output.asScala.map(buildKeyValue[K, V])

  private def isFDBOutputMeaningful(output: Array[Byte]): Boolean =
    !output.sameElements(Array(-1))

  private def buildKeyValue[K: Tupler, V: Tupler](kv: JavaKeyValue)(implicit subspace: Subspace): KeyValue[K, V] =
    KeyValue(parseFDBKey[K](kv.getKey).get, parseFDBObject[V](kv.getValue))

  private def transactionAction[A](f: Transaction => CompletableFuture[A]): DatabaseContext[A] =
    StateT { tr =>
      Option(f(tr)) match {
        case None => CompletableFuture.completedFuture(tr -> null.asInstanceOf[A])
        case Some(result) =>
          result.thenApply(javaClojure(tr -> _))
      }
    }

}
