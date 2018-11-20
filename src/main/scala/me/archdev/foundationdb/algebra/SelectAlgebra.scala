package me.archdev.foundationdb.algebra

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import me.archdev.foundationdb.utils.{ KeySelector, SelectedKey, SubspaceKeyValue }

import scala.language.higherKinds

trait SelectAlgebra[F[_]] {

  def selectKey[K: Tupler](selector: KeySelector)(implicit s: Subspace = Subspace()): F[Option[SelectedKey[K]]]

  def findKey[K: Tupler](selector: KeySelector)(implicit s: Subspace = Subspace()): F[Option[K]]

  def selectRange[K: Tupler, V: Tupler](range: (KeySelector, KeySelector))(
      implicit s: Subspace = Subspace()
  ): F[Seq[SubspaceKeyValue[K, V]]]

  def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace = Subspace()
  ): F[Seq[SubspaceKeyValue[K, V]]]

  def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
      implicit s: Subspace = Subspace()
  ): F[Seq[SubspaceKeyValue[K, V]]]

  def selectRangeStream[K: Tupler, V: Tupler](
      range: (KeySelector, KeySelector),
      limit: Int,
      reverse: Boolean,
      streamingMode: StreamingMode
  )(implicit s: Subspace = Subspace()): F[Iterator[SubspaceKeyValue[K, V]]]

}
