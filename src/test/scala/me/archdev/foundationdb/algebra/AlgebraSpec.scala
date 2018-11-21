package me.archdev.foundationdb.algebra

import com.apple.foundationdb.FDBException
import me.archdev.foundationdb.{ FoundationDB, GenericContext }
import me.archdev.foundationdb.utils.TestSpec

import scala.reflect.ClassTag
import scala.util.Try

trait AlgebraSpec extends TestSpec {

  def buildDatabaseClient(): FoundationDB

  trait Context {

    def withDatabase[A](f: FoundationDB => A): A = {
      val database: FoundationDB = buildDatabaseClient()
      val result                 = f(database)
      database.close()

      result
    }

    case class Query[A](q: GenericContext[A])(implicit database: FoundationDB) {
      def expectResult(a: A) =
        execute() shouldBe a

      def expectFailure(t: Throwable) =
        Try(expectResult(null.asInstanceOf[A])).toEither shouldBe Left(t)

      def expectFailure[A <: Throwable: ClassTag]() =
        an[A] should be thrownBy execute()

      def expectFDBFailure(code: Int) =
        Try(expectResult(null.asInstanceOf[A])).toEither.left.get.asInstanceOf[FDBException].getCode shouldBe code

      def execute() =
        database.prepare(q).unsafeRunSync()

      def executeAsync() =
        database.prepare(q).unsafeToFuture()
    }
  }

}
