package me.archdev.foundationdb.algebra

import java.util.concurrent.CompletableFuture
import com.apple.foundationdb.Transaction

import scala.language.higherKinds

trait UtilsAlgebra[F[_]] {

  def raw[V](f: Transaction => CompletableFuture[V]): F[V]

}
