package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.serializers._

object CompletableFutureInterpreter extends QueryAlgebra[TransactionPlan] {

  override def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace): TransactionPlan[Unit] =
    StateT { tr =>
      inCompletedFuture(tr, _.set(subspace.raw.pack(key.toTuple), value.toTuple.pack()))
    }

  override def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace): TransactionPlan[Option[V]] =
    StateT { tr =>
      tr.get(subspace.raw.pack(key.toTuple)).thenApply(output => tr -> parseFDBOutput[V](output))
    }

  override def delete[K: Tupler](key: K)(implicit subspace: Subspace): TransactionPlan[Unit] =
    StateT(tr => inCompletedFuture(tr, _.clear(subspace.raw.pack(key.toTuple))))

  override def raw[V](f: Transaction => V): TransactionPlan[V] =
    StateT(tr => inCompletedFuture(tr, f))

  private def parseFDBOutput[A](output: Array[Byte])(implicit vs: Tupler[A]): Option[A] =
    Option(output)
      .map(Tuple.fromBytes)
      .map(_.fromTuple[A])

  private def inCompletedFuture[A](tr: Transaction, f: Transaction => A): CompletableFuture[(Transaction, A)] =
    CompletableFuture.completedFuture(tr -> f(tr))

}
