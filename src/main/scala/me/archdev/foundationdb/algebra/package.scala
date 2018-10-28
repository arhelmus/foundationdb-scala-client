package me.archdev.foundationdb

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.serializers.Tupler

package object algebra {

  trait QueryAlgebra[F[_]] {
    def set[K, V](key: K, value: V)(implicit ks: Tupler[K], vs: Tupler[V], subspace: Subspace = Subspace()): F[Unit]
    def get[K, V](key: K)(implicit ks: Tupler[K], vs: Tupler[V], subspace: Subspace = Subspace()): F[Option[V]]
    def delete[K](key: K)(implicit ks: Tupler[K], subspace: Subspace = Subspace()): F[Unit]
    def raw[V](f: Transaction => V): F[V]
  }

}
