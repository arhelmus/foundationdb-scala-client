package me.archdev.foundationdb.interpreters.inmemory

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.InMemoryContext
import me.archdev.foundationdb.algebra.UtilsAlgebra

trait UtilsInterpreter extends UtilsAlgebra[InMemoryContext] {

  override def raw[V](f: Transaction => CompletableFuture[V]): InMemoryContext[V] = ???

}

object UtilsInterpreter extends UtilsInterpreter
