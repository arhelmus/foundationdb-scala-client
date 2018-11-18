package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.tuple.Tuple
import com.apple.foundationdb.{ StreamingMode, Transaction }
import me.archdev.foundationdb._
import me.archdev.foundationdb.clients.{ TransactionCanceled, TransactionClosed, TransactionCommited }
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._

object TestInterpreter extends QueryAlgebra[TestContext] {

  override def set[K: serializers.Tupler, V: serializers.Tupler](key: K,
                                                                 value: V)(implicit s: Subspace): TestContext[Unit] =
    modifyState(_ + (packKey(s, key) -> value.toTuple), unit)

  override def get[K: serializers.Tupler, V: serializers.Tupler](key: K)(implicit s: Subspace): TestContext[Option[V]] =
    modifyState(identity, { storage =>
      storage.get(packKey(s, key)).map(_.fromTuple[V])
    })

  override def selectKey[K: Tupler](key: KeySelector)(implicit s: Subspace): TestContext[Option[SelectedKey[K]]] =
    modifyState(identity, getMatchingKey[K](_, key, false))

  override def findKey[K: Tupler](key: KeySelector)(implicit s: Subspace): TestContext[Option[K]] =
    modifyState(identity, getMatchingKey[K](_, key, true).map(_.key))

  override def getRange[K: Tupler, V: Tupler](range: (K, K))(implicit s: Subspace): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range, true)
    )

  override def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range, true).take(limit)
    )

  override def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range, true).reverse.take(limit)
    )

  override def getRangeStream[K: Tupler, V: Tupler](
      range: (K, K),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): TestContext[Iterator[KeyValue[K, V]]] =
    modifyState(
      identity,
      storage =>
        if (reverse) {
          getRange[K, V](storage, range, true).reverse.take(limit).toIterator
        } else {
          getRange[K, V](storage, range, true).take(limit).toIterator
      }
    )

  override def selectRange[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector)
  )(implicit s: Subspace): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range)
    )

  override def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range).take(limit)
    )

  override def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      selectRange[K, V](_, range).reverse.take(limit)
    )

  override def selectRangeStream[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): TestContext[Iterator[KeyValue[K, V]]] =
    modifyState(
      identity,
      storage =>
        if (reverse) {
          selectRange[K, V](storage, range).reverse.take(limit).toIterator
        } else {
          selectRange[K, V](storage, range).take(limit).toIterator
      }
    )

  override def clear[K: serializers.Tupler](key: K)(implicit s: Subspace): TestContext[Unit] =
    modifyState(_ - packKey(s, key), unit)

  override def clearRange[K: Tupler](range: (K, K))(implicit s: Subspace): TestContext[Unit] =
    modifyState({ storage =>
      val keysToRemove = getRange[K](storage, range).map(_._1).toSet
      storage.filterKeys(!keysToRemove.contains(_))
    }, unit)

  override def raw[V](f: Transaction => CompletableFuture[V]): TestContext[V] = ???

  private def getMatchingKey[K: Tupler](storage: TupleMap, key: KeySelector, onlyFromSubspace: Boolean)(
      implicit subspace: Subspace
  ): Option[SelectedKey[K]] =
    key.ksType match {
      case LessThan =>
        getLastMatchingKey(storage, key, tupleOrdering.lt, onlyFromSubspace)
      case LessOrEqual =>
        getLastMatchingKey(storage, key, tupleOrdering.lteq, onlyFromSubspace)
      case GreaterThan =>
        getFirstMatchingKey(storage, key, tupleOrdering.gt, onlyFromSubspace)
      case GreaterOrEqual =>
        getFirstMatchingKey(storage, key, tupleOrdering.gteq, onlyFromSubspace)
    }

  private def getFirstMatchingKey[K: Tupler](
      storage: TupleMap,
      ks: KeySelector,
      comparatorF: (Tuple, Tuple) => Boolean,
      onlyFromSubspace: Boolean
  )(implicit subspace: Subspace): Option[SelectedKey[K]] =
    storage.keys
      .find(k => comparatorF(k, Tuple.fromBytes(ks.raw.getKey)))
      .flatMap(parseKey[K](_, onlyFromSubspace))

  private def getLastMatchingKey[K: Tupler](
      storage: TupleMap,
      ks: KeySelector,
      comparatorF: (Tuple, Tuple) => Boolean,
      onlyFromSubspace: Boolean
  )(implicit subspace: Subspace): Option[SelectedKey[K]] =
    storage.keys.toSeq.reverse
      .find(k => comparatorF(k, Tuple.fromBytes(ks.raw.getKey)))
      .flatMap(parseKey[K](_, onlyFromSubspace))

  private def parseKey[K: Tupler](key: Tuple,
                                  onlyFromSubspace: Boolean)(implicit subspace: Subspace): Option[SelectedKey[K]] =
    Option(key)
      .map(_.pack())
      .filter(subspace.raw.contains)
      .map(subspace.raw.unpack)
      .map(_.fromTuple[K])
      .map(SelectedKey(Some(subspace), _))
      .orElse(Some(SelectedKey(None, key.fromTuple[K])).filter(_ => !onlyFromSubspace))
      .map(selectedKey => selectedKey.copy(subspace = selectedKey.subspace.filter(!_.isEmpty)))

  private def getRange[K: Tupler, V: Tupler](storage: TupleMap, range: (K, K), onlyFromSubspace: Boolean)(
      implicit subspace: Subspace
  ): Seq[KeyValue[K, V]] =
    getRange[K](storage, range)
      .map { case (key, value) => parseKey[K](key, onlyFromSubspace).map(k => KeyValue(k.key, value.fromTuple[V])) }
      .filter(_.isDefined)
      .map(_.get)

  private def selectRange[K: Tupler, V: Tupler](storage: TupleMap, range: (KeySelector, KeySelector))(
      implicit subspace: Subspace
  ): Seq[KeyValue[K, V]] =
    (for {
      from <- getMatchingKey[K](storage, range._1, false).map(_.key)
      to   <- getMatchingKey[K](storage, range._2, false).map(_.key)
    } yield getRange[K, V](storage, (from, to), false)).getOrElse(Nil)

  private def getRange[K: Tupler](storage: TupleMap, range: (K, K))(implicit subspace: Subspace): Seq[(Tuple, Tuple)] =
    getRange(storage, (packKey(subspace, range._1), packKey(subspace, range._2)))

  private def getRange(storage: TupleMap, range: (Tuple, Tuple))(implicit subspace: Subspace): Seq[(Tuple, Tuple)] =
    storage.filter {
      case (key, _) =>
        val (from, to) = range
        subspace.raw.contains(key.pack()) && key.compareTo(from) >= 0 && key.compareTo(to) < 0
    }.toSeq

  private def packKey[A: Tupler](s: Subspace, key: A): Tuple =
    Tuple.fromBytes(s.raw.pack(key.toTuple))

  private def modifyState[A](f: TupleMap => TupleMap, f2: TupleMap => A): TestContext[A] =
    StateT { storage =>
      CompletableFuture.completedFuture(f(storage) -> f2(storage))
    }

  private val tupleOrdering = new Ordering[Tuple] {
    override def compare(x: Tuple, y: Tuple): Int = x.compareTo(y)
  }

  private def unit[A](a: A): Unit = ()

  override def commit(): TestContext[Unit] =
    modifyState(s => throw TransactionCommited(s), unit)

  override def cancel(): TestContext[Unit] =
    modifyState(_ => throw TransactionCanceled(), unit)

  override def close(): TestContext[Unit] =
    modifyState(_ => throw TransactionClosed(), unit)
}
