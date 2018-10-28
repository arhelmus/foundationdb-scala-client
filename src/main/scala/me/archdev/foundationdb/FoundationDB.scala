package me.archdev.foundationdb

import cats.effect.IO
import com.apple.foundationdb.{ Database, FDB }

import scala.concurrent.{ ExecutionContext, Future }

class FoundationDB(db: Database) {

  def prepare[A](transactionPlan: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
    execution.IOTransactionExecutor.exec(db, transactionPlan)

  def execute[A](transactionPlan: TransactionPlan[A])(implicit ec: ExecutionContext): Future[A] =
    prepare(transactionPlan).unsafeToFuture()

}

object FoundationDB {

  def apply(version: Int) = {
    val fdb = if (FDB.isAPIVersionSelected) {
      FDB.instance()
    } else {
      FDB.selectAPIVersion(version)
    }
    new FoundationDB(fdb.open())
  }

}
