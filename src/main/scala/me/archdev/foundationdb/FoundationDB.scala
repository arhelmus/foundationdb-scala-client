package me.archdev.foundationdb

import cats.effect.IO
import com.apple.foundationdb.{ Database, FDB }

import scala.concurrent.{ ExecutionContext, Future }

class FoundationDB(db: Database) {

  def prepare[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): IO[A] =
    execution.IOTransactionExecutor.exec(db, tr)

  def execute[A](tr: TransactionPlan[A])(implicit ec: ExecutionContext): Future[A] =
    prepare(tr).unsafeToFuture()

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
