package me.archdev.foundationdb.interpreters.inmemory

import me.archdev.foundationdb.InMemoryContext
import me.archdev.foundationdb.algebra.TransactionAlgebra
import me.archdev.foundationdb.clients.{ TransactionCanceled, TransactionClosed, TransactionCommited }

trait TransactionInterpreter extends TransactionAlgebra[InMemoryContext] {

  override def commit(): InMemoryContext[Unit] =
    modifyState(s => throw TransactionCommited(s), unit)

  override def cancel(): InMemoryContext[Unit] =
    modifyState(_ => throw TransactionCanceled(), unit)

  override def close(): InMemoryContext[Unit] =
    modifyState(_ => throw TransactionClosed(), unit)

}

object TransactionInterpreter extends TransactionInterpreter
