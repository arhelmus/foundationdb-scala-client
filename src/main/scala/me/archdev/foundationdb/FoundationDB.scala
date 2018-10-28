package me.archdev.foundationdb

import cats.effect.IO
import com.apple.foundationdb.{ Database, FDB }

import scala.concurrent.ExecutionContext

class FoundationDB(db: Database) {

  def exec[A](f: Query[A])(implicit ec: ExecutionContext, ql: TransactionAlgebra): IO[A] =
    execution.IOTransactionExecutor.exec(db, f(ql))

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
