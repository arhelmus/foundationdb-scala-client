package me.archdev.foundationdb.algebra

import me.archdev.foundationdb._
import me.archdev.foundationdb.utils.TestSpec

import scala.util.Try

abstract class AlgebraSpec extends TestSpec {

  def buildDatabaseClient(): FoundationDB

  "Algebra" when {

    "get" should {

      "retrieve already saved object" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("get-test", "test")
              v <- get[String, String]("get-test")
            } yield v
          ).expectResult(Some("test"))
        }
      }

      "return None if object not exists" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              v <- get[String, String]("get-none")
            } yield v
          ).expectResult(None)
        }
      }

      "return deserialization error if object type is wrong" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("deserialization-test", "test")
              v <- get[String, Int]("deserialization-test")
            } yield v
          ).expectFailure(DeserializationError("java.lang.String cannot be cast to java.lang.Number"))
        }
      }

      "return saved object from correct namespace" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(set("get-namespace", "first-namespace")).execute()

          withSubspace("second-subspace") { implicit subspace =>
            Query(
              for {
                _  <- set("get-test-namespace", "second-namespace")
                v1 <- get[String, String]("get-namespace")
                v2 <- get[String, String]("get-test-namespace")
              } yield (v1, v2)
            ).expectResult((None, Some("second-namespace")))
          }

          Query(get[String, String]("get-namespace")).expectResult(Some("first-namespace"))
        }
      }

    }

    "set" should {

      "insert new value in database" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("insert-test", "test")
              v <- get[String, String]("insert-test")
            } yield v
          ).expectResult(Some("test"))
        }
      }

      "override value in database" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("replace-test", "test")
              v <- get[String, String]("replace-test")
            } yield v
          ).expectResult(Some("test"))
        }
      }

      "insert value in correct namespace" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(set("set-namespace", "first-namespace")).execute()

          withSubspace("second-subspace") { implicit subspace =>
            Query(
              for {
                _  <- set("set-test-namespace", "second-namespace")
                v1 <- get[String, String]("set-namespace")
                v2 <- get[String, String]("set-test-namespace")
              } yield (v1, v2)
            ).expectResult((None, Some("second-namespace")))
          }

          Query(get[String, String]("set-namespace")).expectResult(Some("first-namespace"))
        }
      }

    }

    "delete" should {

      "remove existing value from database" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("delete-test", "test")
              _ <- delete("delete-test")
              v <- get[String, String]("delete-test")
            } yield v
          ).expectResult(None)
        }
      }

      "return successful result if value was not existed before removal" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- delete("not-existed-value-test")
              v <- get[String, String]("not-existed-value-test")
            } yield v
          ).expectResult(None)
        }
      }

      "remove value from correct namespace" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(set("delete-namespace", "first-namespace")).execute()

          withSubspace("second-subspace") { implicit subspace =>
            Query(
              for {
                _ <- delete("delete-namespace")
              } yield ()
            ).execute()
          }

          Query(get[String, String]("delete-namespace")).expectResult(Some("first-namespace"))
        }
      }

    }

  }

  trait Context {

    def withDatabase[A](f: FoundationDB => A): A = {
      val database: FoundationDB = buildDatabaseClient()
      val result                 = f(database)
      database.close()

      result
    }

    case class Query[A](q: GenericContext[A])(implicit database: FoundationDB) {
      def expectResult(a: A) =
        database.prepare(q).unsafeRunSync() shouldBe a

      def expectFailure(t: Throwable) =
        Try(expectResult(null.asInstanceOf[A])).toEither shouldBe Left(t)

      def execute() =
        database.prepare(q).unsafeRunSync()
    }
  }

}
