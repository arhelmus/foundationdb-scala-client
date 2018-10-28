package me.archdev

import java.util.concurrent.CompletableFuture

import cats.Monad
import cats.data.StateT
import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.algebra.QueryAlgebra

package object foundationdb {

  type TransactionPlan[A] = StateT[CompletableFuture, Transaction, A]
  type TransactionAlgebra = QueryAlgebra[TransactionPlan]

  val syntax: TransactionAlgebra = algebra.CompletableFutureInterpreter

  implicit val completableFutureMonad: Monad[CompletableFuture] = new Monad[CompletableFuture] {
    override def pure[A](x: A): CompletableFuture[A] = CompletableFuture.completedFuture(x)

    override def flatMap[A, B](fa: CompletableFuture[A])(f: A => CompletableFuture[B]): CompletableFuture[B] =
      fa.thenCompose(a => f(a))

    override def tailRecM[A, B](a: A)(f: A => CompletableFuture[Either[A, B]]): CompletableFuture[B] =
      f(a).thenCompose {
        case Right(value) => CompletableFuture.completedFuture(value)
        case Left(value)  => tailRecM(value)(f)
      }
  }

}
