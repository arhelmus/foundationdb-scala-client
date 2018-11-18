package me.archdev.foundationdb.interpreters.inmemory

import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.algebra.MutationAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb.utils.SelectedKey
import me.archdev.foundationdb.{ serializers, InMemoryContext, TupleMap }

trait MutationInterpreter extends MutationAlgebra[InMemoryContext] {

  override def set[K: serializers.Tupler, V: serializers.Tupler](key: K, value: V)(
      implicit s: Subspace
  ): InMemoryContext[Unit] =
    modifyState(
      _ + (Tuple.fromBytes(s.pack(key)) -> value.toTuple),
      unit
    )

  override def clear[K: serializers.Tupler](key: K)(implicit s: Subspace): InMemoryContext[Unit] =
    modifyState(
      _ - Tuple.fromBytes(s.pack(key)),
      unit
    )

  override def clearRange[K: Tupler](range: (K, K))(implicit s: Subspace): InMemoryContext[Unit] =
    modifyState(
      clearRange(_, range),
      unit
    )

  private def clearRange[K: Tupler](storage: TupleMap, range: (K, K))(implicit subspace: Subspace): TupleMap = {
    val keysToRemove = scanKeys(storage, range)
      .map(SelectedKey.toSubspaceKey[K])
      .filter(_.isDefined)
      .map(_.get)
      .map(_.toTuple)
      .toSet

    storage.filterKeys(!keysToRemove.contains(_))
  }

}

object MutationInterpreter extends MutationInterpreter
