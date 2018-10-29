package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.serializers._

object CompletableFutureInterpreter extends QueryAlgebra[TransactionPlan] {

  override def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace): TransactionPlan[Unit] =
    transactionAction { tr =>
      tr.set(subspace.raw.pack(key.toTuple), value.toTuple.pack())
      null
    }

  override def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace): TransactionPlan[Option[V]] =
    transactionAction {
      _.get(subspace.raw.pack(key.toTuple)).thenApply(output => parseFDBOutput[V](output))
    }

  override def delete[K: Tupler](key: K)(implicit subspace: Subspace): TransactionPlan[Unit] =
    transactionAction { tr =>
      tr.clear(subspace.raw.pack(key.toTuple))
      null
    }

  override def raw[V](f: Transaction => CompletableFuture[V]): TransactionPlan[V] =
    transactionAction(f)

  private def parseFDBOutput[A](output: Array[Byte])(implicit vs: Tupler[A]): Option[A] =
    Option(output)
      .map(Tuple.fromBytes)
      .map(_.fromTuple[A])

  private def transactionAction[A](f: Transaction => CompletableFuture[A]): TransactionPlan[A] =
    StateT { tr =>
      Option(f(tr)) match {
        case Some(result) => result.thenApply(result => tr -> result)
        case None         => CompletableFuture.completedFuture(tr -> null.asInstanceOf[A])
      }
    }

}
