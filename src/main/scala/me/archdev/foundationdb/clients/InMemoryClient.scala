package me.archdev.foundationdb.clients

import cats.effect.IO
import com.apple.foundationdb.FDBException
import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb._
import me.archdev.foundationdb.namespaces.Directory

import scala.collection.SortedMap
import scala.concurrent.ExecutionContext

class InMemoryClient extends FoundationDBClient[InMemoryContext] {

  override val syntax: algebra.QueryAlgebra[InMemoryContext] = interpreters.InMemoryInterpreter

  override def close(): Unit = ()

  override def openDirectory(directoryPath: Seq[String]): IO[Directory] =
    IO(Directory.mocked(directoryPath))

  override def openDirectory(directoryPath: Seq[String], layer: Array[Byte]): IO[Directory] =
    IO(Directory.mocked(directoryPath, layer))

  override def prepare[A](transaction: InMemoryContext[A])(implicit ec: ExecutionContext): IO[A] =
    javaFutureToIO(transaction.run(state))
      .map {
        case (newState, result) =>
          state = newState
          result
      }
      .handleErrorWith {
        case TransactionCommited(newState) =>
          state = newState
          throw new FDBException("Operation issued while a commit was outstanding", 2017)
        case TransactionCanceled() =>
          throw new FDBException("Operation aborted because the transaction was cancelled", 1025)
        case TransactionClosed() =>
          throw new IllegalStateException()
        case ex: Throwable => throw ex
      }

  private var state: TupleMap = SortedMap.empty(new Ordering[Tuple] {
    override def compare(x: Tuple, y: Tuple): Int = x.compareTo(y)
  })

}

// TODO: refactor it
case class TransactionCommited(state: TupleMap) extends RuntimeException
case class TransactionCanceled()                extends RuntimeException
case class TransactionClosed()                  extends RuntimeException
