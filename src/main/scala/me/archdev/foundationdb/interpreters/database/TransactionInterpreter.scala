package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.algebra.TransactionAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler

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

  override def onError(e: Throwable): DatabaseContext[Transaction] =
    transactionAction {
      _.onError(e)
    }

  override def getVersionstamp(): DatabaseContext[Long] =
    transactionAction {
      _.getVersionstamp
        .thenApply[Tuple](javaClojure(Tuple.fromBytes))
        .thenApply(javaClojure(_.getLong(0)))
    }

  override def addReadConflictKey[K: Tupler](key: K)(implicit subspace: Subspace = Subspace()): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.addReadConflictKey(subspace.pack[K](key))
      null
    }

  override def addReadConflictRange[K: Tupler](range: (K, K))(
      implicit subspace: Subspace = Subspace()
  ): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.addReadConflictRange(subspace.pack[K](range._1), subspace.pack[K](range._2))
      null
    }

  override def addWriteConflictKey[K: Tupler](key: K)(implicit subspace: Subspace = Subspace()): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.addWriteConflictKey(subspace.pack[K](key))
      null
    }

  override def addWriteConflictRange[K: Tupler](range: (K, K))(
      implicit subspace: Subspace = Subspace()
  ): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.addWriteConflictRange(subspace.pack[K](range._1), subspace.pack[K](range._2))
      null
    }

}

object TransactionInterpreter extends TransactionInterpreter
