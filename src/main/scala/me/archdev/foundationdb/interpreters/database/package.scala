package me.archdev.foundationdb.interpreters

import java.util.concurrent.CompletableFuture

import cats.data.StateT
import com.apple.foundationdb.Transaction
import me.archdev.foundationdb._

package object database {

  private[database] def transactionAction[A](f: Transaction => CompletableFuture[A]): DatabaseContext[A] =
    StateT { tr =>
      Option(f(tr)) match {
        case None => CompletableFuture.completedFuture(tr -> null.asInstanceOf[A])
        case Some(result) =>
          result.thenApply(javaClojure(tr -> _))
      }
    }

}
