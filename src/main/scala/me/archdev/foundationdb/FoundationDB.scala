package me.archdev.foundationdb

import java.util.{ function, Collections }
import java.util.concurrent.{ AbstractExecutorService, CompletableFuture, TimeUnit }

import cats.effect.IO
import com.apple.foundationdb.{ Database, FDB, Transaction }

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService, Future }

class FoundationDB(db: Database) {

  def prepare[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
    IO.fromFuture(exec(db, tr))

  def execute[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): Future[A] =
    prepare(tr).unsafeToFuture()

  private def exec[A](db: Database, plan: TransactionPlan[A])(implicit ec: ExecutionContext): IO[Future[A]] =
    IO(
      db.runAsync(
          new function.Function[Transaction, CompletableFuture[A]] {
            override def apply(t: Transaction): CompletableFuture[A] = transformTransactionPlan(t, plan)
          },
          ExecutionContextExecutorServiceBridge(ec)
        )
        .toScala
    )

  private def transformTransactionPlan[A](tr: Transaction, plan: TransactionPlan[A]): CompletableFuture[A] =
    plan
      .run(tr)
      .thenApply(new function.Function[(Transaction, A), A] {
        override def apply(t: (Transaction, A)): A = t._2
      })

}

object FoundationDB {

  // Bad design, error should not be hidden
  def apply(version: Int) = {
    val fdb = if (FDB.isAPIVersionSelected) {
      FDB.instance()
    } else {
      FDB.selectAPIVersion(version)
    }

    new FoundationDB(fdb.open())
  }

}

// https://groups.google.com/forum/#!topic/scala-user/ZyHrfzD7eX8
private[this] object ExecutionContextExecutorServiceBridge {
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
