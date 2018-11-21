package me.archdev.foundationdb.interpreters.inmemory

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.InMemoryContext
import me.archdev.foundationdb.algebra.UtilsAlgebra

trait UtilsInterpreter extends UtilsAlgebra[InMemoryContext] {

  override def raw[V](f: Transaction => CompletableFuture[V]): InMemoryContext[V] = ???

  override def getReadVersion(): InMemoryContext[Long] = ???

  override def setReadVersion(version: Long): InMemoryContext[Unit] = ???

  override def getCommittedVersion(): InMemoryContext[Long] = ???

}

object UtilsInterpreter extends UtilsInterpreter
