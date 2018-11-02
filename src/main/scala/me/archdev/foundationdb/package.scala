package me.archdev

import java.util.concurrent.CompletableFuture
import java.util.function

import cats.Monad
import cats.data.StateT
import cats.effect.IO
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.clients.FoundationDBClient

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

package object foundationdb {

  type TupleMap = Map[Tuple, Tuple]

  type GenericContext[A]  = StateT[CompletableFuture, Any, A]
  type DatabaseContext[A] = StateT[CompletableFuture, Transaction, A]
  type TestContext[A]     = StateT[CompletableFuture, TupleMap, A]

  type FoundationDB = FoundationDBClient[GenericContext]

  private[foundationdb] def javaClojure[A, B](f: A => B): function.Function[A, B] =
    new function.Function[A, B] {
      override def apply(t: A): B = f(t)
    }

  private[foundationdb] def scalaFutureToIO[A](f: => Future[A]): IO[A] =
    IO.fromFuture(IO(f))

  private[foundationdb] def javaFutureToIO[A](f: => java.util.concurrent.CompletableFuture[A]): IO[A] =
    scalaFutureToIO(f.toScala)

  private[foundationdb] implicit val completableFutureMonad: Monad[CompletableFuture] = new Monad[CompletableFuture] {
    override def pure[A](x: A): CompletableFuture[A] = CompletableFuture.completedFuture(x)

    override def flatMap[A, B](fa: CompletableFuture[A])(f: A => CompletableFuture[B]): CompletableFuture[B] =
      fa.thenCompose(javaClojure(f))

    override def tailRecM[A, B](a: A)(f: A => CompletableFuture[Either[A, B]]): CompletableFuture[B] =
      f(a).thenCompose(javaClojure({
        case Right(value) => CompletableFuture.completedFuture(value)
        case Left(value)  => tailRecM(value)(f)
      }))
  }

}
