package me.archdev.foundationdb.interpreters.inmemory

import com.apple.foundationdb.StreamingMode
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.algebra.SelectAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import me.archdev.foundationdb.utils._

trait SelectInterpreter extends SelectAlgebra[InMemoryContext] {

  override def selectKey[K: Tupler](
      selector: KeySelector
  )(implicit s: Subspace): InMemoryContext[Option[SelectedKey[K]]] =
    modifyState(
      identity,
      selectMatchingKey[K](_, selector)
    )

  override def findKey[K: Tupler](selector: KeySelector)(implicit s: Subspace): InMemoryContext[Option[K]] =
    modifyState(
      identity,
      selectMatchingKey[K](_, selector).flatMap(SelectedKey.toSubspaceKey[K])
    )

  override def selectRange[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector)
  )(implicit s: Subspace): InMemoryContext[Seq[SubspaceKeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range)
    )

  override def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace
  ): InMemoryContext[Seq[SubspaceKeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range).take(limit)
    )

  override def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace
  ): InMemoryContext[Seq[SubspaceKeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range).reverse.take(limit)
    )

  override def selectRangeStream[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): InMemoryContext[Iterator[SubspaceKeyValue[K, V]]] =
    modifyState(
      identity,
      storage =>
        if (reverse) {
          selectRange[K, V](storage, range).reverse.take(limit).toIterator
        } else {
          selectRange[K, V](storage, range).take(limit).toIterator
      }
    )

  private def selectMatchingKey[K: Tupler](storage: TupleMap, key: KeySelector)(
      implicit subspace: Subspace
  ): Option[SelectedKey[K]] = {
    val keys = storage.keys.toSeq
    key.ksType match {
      case LessThan =>
        getFirstMatchingKey(keys.reverse, key, tupleOrdering.lt)
      case LessOrEqual =>
        getFirstMatchingKey(keys.reverse, key, tupleOrdering.lteq)
      case GreaterThan =>
        getFirstMatchingKey(keys, key, tupleOrdering.gt)
      case GreaterOrEqual =>
        getFirstMatchingKey(keys, key, tupleOrdering.gteq)
    }
  }

  private def getFirstMatchingKey[K: Tupler](
      keys: Seq[Tuple],
      ks: KeySelector,
      comparatorF: (Tuple, Tuple) => Boolean
  )(implicit subspace: Subspace): Option[SelectedKey[K]] =
    keys
      .find(k => comparatorF(k, Tuple.fromBytes(ks.raw.getKey)))
      .flatMap(tuple => SelectedKey.parse[K](tuple.pack()))

  private def selectRange[K: Tupler, V: Tupler](storage: TupleMap, range: (KeySelector, KeySelector))(
      implicit subspace: Subspace
  ): Seq[SubspaceKeyValue[K, V]] = {

    val result = for {
      from <- selectMatchingKey[K](storage, range._1)
      to   <- selectMatchingKey[K](storage, range._2)
      keys = scanKeys[K](storage, (from, to))
    } yield enrichKeys[K, V](storage, keys.map(SelectedKey.toTuple[K]))

    result.getOrElse(Nil)
  }

}

object SelectInterpreter extends SelectInterpreter
