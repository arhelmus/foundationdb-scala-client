package me.archdev.foundationdb

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.serializers.Tupler

package object algebra {

  trait QueryAlgebra[F[_]] {
    def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace = Subspace()): F[Unit]
    def get[K: Tupler, V: Tupler](key: K)(implicit subspace: Subspace = Subspace()): F[Option[V]]
    def delete[K: Tupler](key: K)(implicit subspace: Subspace = Subspace()): F[Unit]
    def raw[V](f: Transaction => CompletableFuture[V]): F[V]
  }

}
