package me.archdev.foundationdb.interpreters.inmemory

import com.apple.foundationdb.StreamingMode
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.{ serializers, InMemoryContext, TupleMap }
import me.archdev.foundationdb.algebra.GetAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb.utils.{ KeyValue, SelectedKey, SubspaceKey }

trait GetInterpreter extends GetAlgebra[InMemoryContext] {

  override def get[K: serializers.Tupler, V: serializers.Tupler](
      key: K
  )(implicit s: Subspace): InMemoryContext[Option[V]] =
    modifyState(identity, { storage =>
      storage.get(Tuple.fromBytes(s.pack(key))).map(_.fromTuple[V])
    })

  override def getRange[K: Tupler, V: Tupler](
      range: (K, K)
  )(implicit s: Subspace): InMemoryContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range)
    )

  override def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): InMemoryContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range).take(limit)
    )

  override def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): InMemoryContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range).reverse.take(limit)
    )

  override def getRangeStream[K: Tupler, V: Tupler](
      range: (K, K),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): InMemoryContext[Iterator[KeyValue[K, V]]] =
    modifyState(
      identity,
      storage =>
        if (reverse) {
          getRange[K, V](storage, range).reverse.take(limit).toIterator
        } else {
          getRange[K, V](storage, range).take(limit).toIterator
      }
    )

  private def getRange[K: Tupler, V: Tupler](storage: TupleMap,
                                             range: (K, K))(implicit subspace: Subspace): Seq[KeyValue[K, V]] =
    enrichKeys[K, V](
      storage,
      scanKeys(storage, SelectedKey.range[K](range))
        .map(SelectedKey.toTuple[K])
    ).map(_.keyValue)

}

object GetInterpreter extends GetInterpreter
