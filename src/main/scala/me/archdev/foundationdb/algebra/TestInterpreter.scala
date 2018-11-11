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

  override def getKey[K: Tupler](key: KeySelector): TestContext[Option[K]] =
    modifyState(
      identity, { storage =>
        key.ksType match {
          case LessThan =>
            getLastMatchingKey(storage, key, tupleOrdering.lt)
          case LessOrEqual =>
            getLastMatchingKey(storage, key, tupleOrdering.lteq)
          case GreaterThan =>
            getFirstMatchingKey(storage, key, tupleOrdering.gt)
          case GreaterOrEqual =>
            getFirstMatchingKey(storage, key, tupleOrdering.gteq)
        }
      }
    )

  override def getRange[K: Tupler, V: Tupler](range: (K, K))(implicit s: Subspace): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range)
    )

  override def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range).take(limit)
    )

  override def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace
  ): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      getRange[K, V](_, range).reverse.take(limit)
    )

  override def getRangeStream[K: Tupler, V: Tupler](
      range: (K, K),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace): TestContext[Seq[KeyValue[K, V]]] =
    modifyState(
      identity,
      storage =>
        if (reverse) {
          getRange[K, V](storage, range).reverse.take(limit)
        } else {
          getRange[K, V](storage, range).take(limit)
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

  private def getFirstMatchingKey[K: Tupler](storage: TupleMap,
                                             ks: KeySelector,
                                             comparatorF: (Tuple, Tuple) => Boolean): Option[K] =
    storage.keys
      .find(k => comparatorF(k, ks.subspace.raw.unpack(ks.raw.getKey)))
      .map(_.fromTuple[K])

  private def getLastMatchingKey[K: Tupler](storage: TupleMap,
                                            ks: KeySelector,
                                            comparatorF: (Tuple, Tuple) => Boolean): Option[K] =
    storage.keys.toSeq.reverse
      .find(k => comparatorF(k, ks.subspace.raw.unpack(ks.raw.getKey)))
      .map(_.fromTuple[K])

  private def getRange[K: Tupler, V: Tupler](storage: TupleMap,
                                             range: (K, K))(implicit s: Subspace): Seq[KeyValue[K, V]] =
    getRange[K](storage, range)
      .map { case (key, value) => KeyValue(key.fromTuple[K], value.fromTuple[V]) }

  private def getRange[K: Tupler](storage: TupleMap, range: (K, K))(implicit s: Subspace): Seq[(Tuple, Tuple)] =
    storage.filter {
      case (key, _) =>
        val (from, to) = range
        key.compareTo(from.toTuple) >= 0 && key.compareTo(to.toTuple) < 0
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
