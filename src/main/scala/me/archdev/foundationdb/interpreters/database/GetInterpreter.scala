package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.algebra.GetAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import me.archdev.foundationdb.utils.{ FDBObject, KeyValue }
import me.archdev.foundationdb.{ javaClojure, DatabaseContext }

import scala.collection.JavaConverters._

trait GetInterpreter extends GetAlgebra[DatabaseContext] {

  override def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Option[V]] =
    transactionAction {
      _.get(subspace.pack(key)).thenApply(javaClojure(FDBObject.parse[V]))
    }

  override def getRange[K: Tupler, V: Tupler](
      range: (K, K)
  )(implicit s: Subspace): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2))
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
    }

  override def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2), limit)
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
    }

  override def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(s.pack(range._1), s.pack(range._2), limit, true)
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
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
          .map(KeyValue.parse[K, V])
      )
    }

}

object GetInterpreter extends GetInterpreter
