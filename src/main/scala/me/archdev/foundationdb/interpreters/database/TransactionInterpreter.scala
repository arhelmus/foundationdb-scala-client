package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import me.archdev.foundationdb.DatabaseContext
import me.archdev.foundationdb.algebra.TransactionAlgebra

trait TransactionInterpreter extends TransactionAlgebra[DatabaseContext] {

  override def commit(): DatabaseContext[Unit] =
    transactionAction {
      _.commit().asInstanceOf[CompletableFuture[Unit]]
    }

  override def cancel(): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.cancel()
      null
    }

  override def close(): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.close()
      null
    }

}

object TransactionInterpreter extends TransactionInterpreter
