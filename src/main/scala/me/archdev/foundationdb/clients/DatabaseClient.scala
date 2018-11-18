package me.archdev.foundationdb.clients

import java.util.concurrent.CompletableFuture

import cats.effect.IO
import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.{ Database, Transaction }
import me.archdev.foundationdb.namespaces.Directory
import me.archdev.foundationdb._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

class DatabaseClient(val db: Database) extends FoundationDBClient[DatabaseContext] {

  override val syntax: algebra.QueryAlgebra[DatabaseContext] = interpreters.DatabaseInterpreter

  override def close(): Unit = db.close()

  override def prepare[A](transaction: DatabaseContext[A])(implicit ec: ExecutionContext): IO[A] =
    javaFutureToIO(
      db.runAsync(
        javaClojure(materializeTransaction(_, transaction)),
        ExecutionContextExecutorServiceBridge(ec)
      )
    )

  private def materializeTransaction[A](tr: Transaction, queryPlan: DatabaseContext[A]): CompletableFuture[A] =
    queryPlan
      .run(tr)
      .thenApply(javaClojure(_._2))

  override def openDirectory(directoryPath: Seq[String]): IO[Directory] =
    javaFutureToIO(DirectoryLayer.getDefault.createOrOpen(db, directoryPath.asJava)).map(Directory.apply)

  override def openDirectory(directoryPath: Seq[String], layer: Array[Byte]): IO[Directory] =
    javaFutureToIO(DirectoryLayer.getDefault.createOrOpen(db, directoryPath.asJava, layer)).map(Directory.apply)

}
