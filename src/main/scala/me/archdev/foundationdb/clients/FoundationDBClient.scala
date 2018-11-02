package me.archdev.foundationdb.clients

import cats.effect.IO
import me.archdev.foundationdb.algebra.QueryAlgebra
import me.archdev.foundationdb.namespaces.Directory

import scala.concurrent.{ ExecutionContext, Future }

trait FoundationDBClient[F[_]] extends AutoCloseable {
  val syntax: QueryAlgebra[F]
  def openDirectory(directoryPath: Seq[String]): IO[Directory]
  def openDirectory(directoryPath: Seq[String], layer: Array[Byte]): IO[Directory]
  def prepare[A](transaction: F[A])(implicit ec: ExecutionContext): IO[A]
  def close(): Unit

  def openDirectorySync(directoryPath: Seq[String]): Directory =
    openDirectory(directoryPath).unsafeRunSync()
  def openDirectorySync(directoryPath: Seq[String], layer: Array[Byte]): Directory =
    openDirectory(directoryPath).unsafeRunSync()
  def execute[A](transaction: F[A])(implicit ec: ExecutionContext): Future[A] =
    prepare(transaction).unsafeToFuture()
}
