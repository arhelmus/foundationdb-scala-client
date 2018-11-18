package me.archdev.foundationdb.interpreters.database

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.DatabaseContext
import me.archdev.foundationdb.algebra.UtilsAlgebra

trait UtilsInterpreter extends UtilsAlgebra[DatabaseContext] {

  override def raw[V](f: Transaction => CompletableFuture[V]): DatabaseContext[V] =
    transactionAction(f)

}

object UtilsInterpreter extends UtilsInterpreter
