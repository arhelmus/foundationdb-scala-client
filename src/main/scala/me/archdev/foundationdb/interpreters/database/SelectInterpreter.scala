package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.algebra.SelectAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import me.archdev.foundationdb.utils._
import me.archdev.foundationdb.{ javaClojure, DatabaseContext }

import scala.collection.JavaConverters._

trait SelectInterpreter extends SelectAlgebra[DatabaseContext] {

  override def selectKey[K: Tupler](
      selector: KeySelector
  )(implicit s: Subspace): DatabaseContext[Option[SelectedKey[K]]] =
    transactionAction {
      _.getKey(selector.raw).thenApply(javaClojure(SelectedKey.parse[K]))
    }

  override def findKey[K: Tupler](selector: KeySelector)(implicit s: Subspace): DatabaseContext[Option[K]] =
    transactionAction {
      _.getKey(selector.raw).thenApply(javaClojure(SubspaceKey.parse[K]))
    }

  override def selectRange[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector)
  )(implicit subspace: Subspace): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw)
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
    }

  override def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit subspace: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw, limit)
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
    }

  override def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit subspace: Subspace
  ): DatabaseContext[Seq[KeyValue[K, V]]] =
    transactionAction {
      _.getRange(range._1.raw, range._2.raw, limit, true)
        .asList()
        .thenApply(javaClojure(_.asScala.map(KeyValue.parse[K, V])))
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
          .map(KeyValue.parse[K, V])
      )
    }

}

object SelectInterpreter extends SelectInterpreter
