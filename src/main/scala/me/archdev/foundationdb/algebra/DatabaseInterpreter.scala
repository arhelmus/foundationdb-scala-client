package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._

object DatabaseInterpreter extends QueryAlgebra[DatabaseContext] {

  override def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.set(subspace.raw.pack(key.toTuple), value.toTuple.pack())
      null
    }

  override def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Option[V]] =
    transactionAction {
      _.get(subspace.raw.pack(key.toTuple)).thenApply(javaClojure(parseFDBOutput[V]))
    }

  override def delete[K: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.clear(subspace.raw.pack(key.toTuple))
      null
    }

  override def raw[V](f: Transaction => CompletableFuture[V]): DatabaseContext[V] =
    transactionAction(f)

  private def parseFDBOutput[A](output: Array[Byte])(implicit vs: Tupler[A]): Option[A] =
    Option(output)
      .map(Tuple.fromBytes)
      .map(_.fromTuple[A])

  private def transactionAction[A](f: Transaction => CompletableFuture[A]): DatabaseContext[A] =
    StateT { tr =>
      Option(f(tr)) match {
        case None => CompletableFuture.completedFuture(tr -> null.asInstanceOf[A])
        case Some(result) =>
          result.thenApply(javaClojure(tr -> _))
      }
    }

}
