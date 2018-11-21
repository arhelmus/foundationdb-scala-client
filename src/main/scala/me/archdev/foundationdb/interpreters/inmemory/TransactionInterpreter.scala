package me.archdev.foundationdb.interpreters.inmemory

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.InMemoryContext
import me.archdev.foundationdb.algebra.TransactionAlgebra
import me.archdev.foundationdb.clients.{ TransactionCanceled, TransactionClosed, TransactionCommited }
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler

trait TransactionInterpreter extends TransactionAlgebra[InMemoryContext] {

  override def commit(): InMemoryContext[Unit] =
    modifyState(s => throw TransactionCommited(s), unit)

  override def cancel(): InMemoryContext[Unit] =
    modifyState(_ => throw TransactionCanceled(), unit)

  override def close(): InMemoryContext[Unit] =
    modifyState(_ => throw TransactionClosed(), unit)

  override def onError(e: Throwable): InMemoryContext[Transaction] = ???

  override def getVersionstamp(): InMemoryContext[Long] = ???

  override def addReadConflictKey[K: Tupler](key: K)(implicit subspace: Subspace): InMemoryContext[Unit] =
    modifyState(identity, unit)

  override def addReadConflictRange[K: Tupler](range: (K, K))(
      implicit subspace: Subspace
  ): InMemoryContext[Unit] = modifyState(identity, unit)

  override def addWriteConflictKey[K: Tupler](key: K)(implicit subspace: Subspace): InMemoryContext[Unit] =
    modifyState(identity, unit)

  override def addWriteConflictRange[K: Tupler](range: (K, K))(
      implicit subspace: Subspace
  ): InMemoryContext[Unit] = modifyState(identity, unit)

}

object TransactionInterpreter extends TransactionInterpreter
