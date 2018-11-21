package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture
import com.apple.foundationdb.Transaction

import scala.language.higherKinds

trait UtilsAlgebra[F[_]] {

  def getReadVersion(): F[Long]
  def setReadVersion(version: Long): F[Unit]

  def getCommittedVersion(): F[Long]

  def raw[V](f: Transaction => CompletableFuture[V]): F[V]

}
