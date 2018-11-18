package me.archdev.foundationdb.algebra

import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler

import scala.language.higherKinds

trait MutationAlgebra[F[_]] {

  def set[K: Tupler, V: Tupler](key: K, value: V)(implicit s: Subspace = Subspace()): F[Unit]

  def clear[K: Tupler](key: K)(implicit s: Subspace = Subspace()): F[Unit]

  def clearRange[K: Tupler](range: (K, K))(implicit s: Subspace = Subspace()): F[Unit]

}
