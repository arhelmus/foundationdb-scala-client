package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb._
import me.archdev.foundationdb.algebra.UtilsAlgebra

trait UtilsInterpreter extends UtilsAlgebra[DatabaseContext] {

  override def raw[V](f: Transaction => CompletableFuture[V]): DatabaseContext[V] =
    transactionAction(f)

  override def getReadVersion(): DatabaseContext[Long] =
    transactionAction {
      _.getReadVersion.thenApply(javaClojure(_.longValue()))
    }

  override def setReadVersion(version: Long): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.setReadVersion(version)
      null
    }

  override def getCommittedVersion(): DatabaseContext[Long] =
    transactionAction { tr =>
      CompletableFuture.completedFuture(tr.getCommittedVersion)
    }

}

object UtilsInterpreter extends UtilsInterpreter
