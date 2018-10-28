package me.archdev.foundationdb

import java.util.concurrent.CompletableFuture

import cats.effect.IO
import com.apple.foundationdb.{ Database, FDB, Transaction }
import me.archdev.foundationdb.Utils._

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, Future }

class FoundationDB(db: Database) {

  def prepare[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
    exec(db, tr)

  def execute[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): Future[A] =
    prepare(tr).unsafeToFuture()

  private def exec[A](db: Database, plan: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
    db.runAsync(tr => transformTransactionPlan(tr, plan), ExecutionContextExecutorServiceBridge(ec)).toIO

  private def transformTransactionPlan[A](tr: Transaction, plan: TransactionPlan[A]): CompletableFuture[A] =
    plan
      .run(tr)
      .map(_._2)
      .unsafeToFuture()
      .toJava
      .toCompletableFuture

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
