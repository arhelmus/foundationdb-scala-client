package me.archdev.foundationdb

import java.util.Collections
import java.util.concurrent.{ AbstractExecutorService, CompletableFuture, TimeUnit }

import cats.effect.IO

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService, Promise }

object Utils {

  implicit class CompletableFutureSyntax[A](future: CompletableFuture[A]) {
    def toIO: IO[A] = {
      val result = Promise[A]()
      future.whenComplete((t: A, u: Throwable) => {
        if (u != null) {
          result.failure(u)
        } else {
          result.success(t)
        }
      })
      IO.fromFuture(IO(result.future))
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

}
