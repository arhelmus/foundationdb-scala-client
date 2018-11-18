package me.archdev.foundationdb.algebra

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import me.archdev.foundationdb.utils.KeyValue

import scala.language.higherKinds

trait GetAlgebra[F[_]] {

  def get[K: Tupler, V: Tupler](key: K)(implicit s: Subspace = Subspace()): F[Option[V]]

  def getRange[K: Tupler, V: Tupler](range: (K, K))(implicit s: Subspace = Subspace()): F[Seq[KeyValue[K, V]]]

  def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace = Subspace()
  ): F[Seq[KeyValue[K, V]]]

  def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
      implicit s: Subspace = Subspace()
  ): F[Seq[KeyValue[K, V]]]

  def getRangeStream[K: Tupler, V: Tupler](range: (K, K), limit: Int, reverse: Boolean, streamingMode: StreamingMode)(
      implicit s: Subspace = Subspace()
  ): F[Iterator[KeyValue[K, V]]]

}
