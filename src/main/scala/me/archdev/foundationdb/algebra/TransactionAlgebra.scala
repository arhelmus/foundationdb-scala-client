package me.archdev.foundationdb.algebra

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler

import scala.language.higherKinds

trait TransactionAlgebra[F[_]] {
  def commit(): F[Unit]
  def cancel(): F[Unit]
  def close(): F[Unit]
  def onError(e: Throwable): F[Transaction]

  def getVersionstamp(): F[Long]

  def addReadConflictKey[K: Tupler](key: K)(implicit subspace: Subspace = Subspace()): F[Unit]
  def addReadConflictRange[K: Tupler](range: (K, K))(implicit subspace: Subspace = Subspace()): F[Unit]
  def addWriteConflictKey[K: Tupler](key: K)(implicit subspace: Subspace = Subspace()): F[Unit]
  def addWriteConflictRange[K: Tupler](range: (K, K))(implicit subspace: Subspace = Subspace()): F[Unit]
}
