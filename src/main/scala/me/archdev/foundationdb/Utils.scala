package me.archdev.foundationdb

import java.util.concurrent.CompletableFuture

import cats.effect.IO

import scala.concurrent.Promise

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

}
