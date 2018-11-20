package me.archdev.foundationdb.interpreters

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb.utils.{ SelectedKey, SubspaceKeyValue }
import com.apple.foundationdb.{ KeyValue => JavaKeyValue }

package object inmemory {

  private[inmemory] def unit[A](a: A): Unit = ()

  private[inmemory] def modifyState[A](f: TupleMap => TupleMap, f2: TupleMap => A): InMemoryContext[A] =
    StateT { storage =>
      CompletableFuture.completedFuture(f(storage) -> f2(storage))
    }

  private[inmemory] val tupleOrdering = new Ordering[Tuple] {
    override def compare(x: Tuple, y: Tuple): Int = x.compareTo(y)
  }

  private[inmemory] def scanKeys[K: Tupler](storage: TupleMap, range: (SelectedKey[K], SelectedKey[K]))(
      implicit subspace: Subspace
  ): Seq[SelectedKey[K]] =
    storage.keys
      .filter { key =>
        val from = Tuple.fromBytes(SelectedKey.pack(range._1))
        val to   = Tuple.fromBytes(SelectedKey.pack(range._2))

        key.compareTo(from) >= 0 && key.compareTo(to) < 0
      }
      .map(_.pack())
      .map(SelectedKey.parseUnsafe[K])
      .toSeq

  private[inmemory] def enrichKeys[K: Tupler, V: Tupler](storage: TupleMap, keys: Seq[Tuple])(
      implicit subspace: Subspace
  ): Seq[SubspaceKeyValue[K, V]] =
    keys.map(key => SubspaceKeyValue.parse[K, V](new JavaKeyValue(key.pack, storage(key).pack())))

}
