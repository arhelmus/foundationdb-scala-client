package me.archdev.foundationdb.clients

import cats.effect.IO
import me.archdev.foundationdb.namespaces.Directory
import me.archdev.foundationdb._

import scala.concurrent.ExecutionContext

class TestClient extends FoundationDBClient[TestContext] {

  override val syntax: algebra.QueryAlgebra[TestContext] = algebra.TestInterpreter

  override def close(): Unit = ()

  override def openDirectory(directoryPath: Seq[String]): IO[Directory] = ???

  override def openDirectory(directoryPath: Seq[String], layer: Array[Byte]): IO[Directory] = ???

  override def prepare[A](transaction: TestContext[A])(implicit ec: ExecutionContext): IO[A] =
    javaFutureToIO(transaction.run(state)).map {
      case (newState, result) =>
        state = newState
        result
    }

  private var state: TupleMap = Map.empty

}
