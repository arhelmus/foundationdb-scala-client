package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb._

object TestInterpreter extends QueryAlgebra[TestContext] {

  override def set[K: serializers.Tupler, V: serializers.Tupler](key: K,
                                                                 value: V)(implicit s: Subspace): TestContext[Unit] =
    modifyState(_ + (packKey(s, key) -> value.toTuple), unit)

  override def get[K: serializers.Tupler, V: serializers.Tupler](key: K)(implicit s: Subspace): TestContext[Option[V]] =
    modifyState(identity, { storage =>
      storage.get(packKey(s, key)).map(_.fromTuple[V])
    })

  override def delete[K: serializers.Tupler](key: K)(implicit s: Subspace): TestContext[Unit] =
    modifyState(_ - packKey(s, key), unit)

  override def raw[V](f: Transaction => CompletableFuture[V]): TestContext[V] = ???

  private def packKey[A: Tupler](s: Subspace, key: A): Tuple =
    Tuple.fromBytes(s.raw.pack(key.toTuple))

  private def modifyState[A](f: TupleMap => TupleMap, f2: TupleMap => A): TestContext[A] =
    StateT { storage =>
      CompletableFuture.completedFuture(f(storage) -> f2(storage))
    }

  private def unit[A](a: A): Unit = ()

}
