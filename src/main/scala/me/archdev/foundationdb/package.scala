package me.archdev

import java.util.function
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
      fa.thenCompose(new function.Function[A, CompletableFuture[B]] {
        override def apply(a: A): CompletableFuture[B] = f(a)
      })

    override def tailRecM[A, B](a: A)(f: A => CompletableFuture[Either[A, B]]): CompletableFuture[B] =
      f(a).thenCompose(new function.Function[Either[A, B], CompletableFuture[B]] {
        override def apply(t: Either[A, B]): CompletableFuture[B] = t match {
          case Right(value) => CompletableFuture.completedFuture(value)
          case Left(value)  => tailRecM(value)(f)
        }
      })
  }

}
