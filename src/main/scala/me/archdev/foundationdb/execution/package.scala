package me.archdev.foundationdb

import java.util.Collections
import java.util.concurrent.{ AbstractExecutorService, TimeUnit }

import cats.effect.IO
import com.apple.foundationdb.Database
import me.archdev.foundationdb.Utils._

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

package object execution {
  trait QueryExecutor[F[_]] {
    def exec[A](db: Database, plan: TransactionPlan[A])(implicit ec: ExecutionContext): F[A]
  }

  implicit val IOTransactionExecutor: QueryExecutor[IO] =
    new QueryExecutor[IO] {
      override def exec[A](db: Database, plan: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
        db.runAsync(tr =>
                      plan
                        .run(tr)
                        .map(_._2)
                        .unsafeToFuture()
                        .toJava
                        .toCompletableFuture,
                    ExecutionContextExecutorServiceBridge(ec))
          .toIO
    }

}

// https://groups.google.com/forum/#!topic/scala-user/ZyHrfzD7eX8
object ExecutionContextExecutorServiceBridge {
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
