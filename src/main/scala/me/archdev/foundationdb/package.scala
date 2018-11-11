package me.archdev

import java.util.concurrent.{ AbstractExecutorService, CompletableFuture, CompletionException, TimeUnit }
import java.util.{ function, Collections }

import cats.Monad
import cats.data.StateT
import cats.effect.IO
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.clients.FoundationDBClient
import me.archdev.foundationdb.serializers._

import scala.collection.SortedMap
import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService, Future }

package object foundationdb {

  type TupleMap = SortedMap[Tuple, Tuple]

  type GenericContext[A]  = StateT[CompletableFuture, Any, A]
  type DatabaseContext[A] = StateT[CompletableFuture, Transaction, A]
  type TestContext[A]     = StateT[CompletableFuture, TupleMap, A]

  type FoundationDB = FoundationDBClient[GenericContext]

  private[foundationdb] def javaClojure[A, B](f: A => B): function.Function[A, B] =
    new function.Function[A, B] {
      override def apply(t: A): B = f(t)
    }

  private[foundationdb] def scalaFutureToIO[A](f: => Future[A]): IO[A] =
    IO.fromFuture(IO(f)).handleErrorWith {
      case ex: CompletionException =>
        throw ex.getCause
    }

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

  // https://groups.google.com/forum/#!topic/scala-user/ZyHrfzD7eX8
  private[foundationdb] object ExecutionContextExecutorServiceBridge {
    def apply(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
      case null                                  => throw null
      case eces: ExecutionContextExecutorService => eces
      case other =>
        new AbstractExecutorService with ExecutionContextExecutorService {
          override def prepare(): ExecutionContext                             = other
          override def isShutdown                                              = false
          override def isTerminated                                            = false
          override def shutdown()                                              = ()
          override def shutdownNow()                                           = Collections.emptyList[Runnable]
          override def execute(runnable: Runnable): Unit                       = other.execute(runnable)
          override def reportFailure(t: Throwable): Unit                       = other.reportFailure(t)
          override def awaitTermination(length: Long, unit: TimeUnit): Boolean = false
        }
    }
  }

}
