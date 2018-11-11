package me.archdev.foundationdb.algebra

import com.apple.foundationdb.FDBException
import me.archdev.foundationdb._
import me.archdev.foundationdb.utils.TestSpec

import scala.reflect.ClassTag
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

    "getKey" should {

      "return value where key less than" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            r <- getKey[Int](KeySelector.lastLessThan(3))
          } yield r).expectResult(Some(2))
        }
      }

      "return value where key less or eq" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            r <- getKey[Int](KeySelector.lastLessOrEqual(3))
          } yield r).expectResult(Some(3))
        }
      }

      "return value where key greater than" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            r <- getKey[Int](KeySelector.firstGreaterThan(3))
          } yield r).expectResult(Some(4))
        }
      }

      "return value where key greater or eq" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            r <- getKey[Int](KeySelector.firstGreaterOrEqual(3))
          } yield r).expectResult(Some(3))
        }
      }

    }

    "getRange" should {

      "return values in some range" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            _ <- set(5, "5")
            r <- getRange[Int, String](2 -> 5)
          } yield r).expectResult(Seq(KeyValue(2, "2"), KeyValue(3, "3"), KeyValue(4, "4")))
        }
      }

      "return values in some range with limit" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            _ <- set(5, "5")
            r <- getRangeWithLimit[Int, String](2 -> 5, limit = 2)
          } yield r).expectResult(Seq(KeyValue(2, "2"), KeyValue(3, "3")))
        }
      }

      "return values in some range with limit and reversed" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(for {
            _ <- set(1, "1")
            _ <- set(2, "2")
            _ <- set(3, "3")
            _ <- set(4, "4")
            _ <- set(5, "5")
            r <- getRangeWithLimitReversed[Int, String](2 -> 5, limit = 2)
          } yield r).expectResult(Seq(KeyValue(4, "4"), KeyValue(3, "3")))
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

    "clear" should {

      "remove existing value from database" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set("delete-test", "test")
              _ <- clear("delete-test")
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
              _ <- clear("not-existed-value-test")
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
                _ <- clear("delete-namespace")
              } yield ()
            ).execute()
          }

          Query(get[String, String]("delete-namespace")).expectResult(Some("first-namespace"))
        }
      }

      "remove value in range" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set(1, "1")
              _ <- set(2, "2")
              _ <- set(3, "3")
              _ <- set(4, "4")
              _ <- set(5, "5")
              _ <- clearRange(2 -> 4)
              v <- getRange[Int, String](1 -> 6)
            } yield v
          ).expectResult(Seq(KeyValue(1, "1"), KeyValue(4, "4"), KeyValue(5, "5")))
        }
      }

    }

    "commit" should {

      "commit changes and disallow execution of commands after" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set(1, "1")
              _ <- set(2, "2")
              _ <- commit()
              _ <- set(4, "4")
              _ <- set(5, "5")
              _ <- clearRange(2 -> 4)
              v <- getRange[Int, String](1 -> 6)
            } yield v
          ).expectFDBFailure(2017)

          Query(getRange[Int, String](1 -> 6)).expectResult(Seq(KeyValue(1, "1"), KeyValue(2, "2")))
        }
      }

    }

    "cancel" should {

      "disallow execution of commands after and leave changes uncommited" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set(1, "1")
              _ <- set(2, "2")
              _ <- database.syntax.cancel()
              _ <- set(4, "4")
              _ <- set(5, "5")
              _ <- clearRange(2 -> 4)
              v <- getRange[Int, String](1 -> 6)
            } yield v
          ).expectFDBFailure(1025)

          Query(getRange[Int, String](1 -> 6)).expectResult(Nil)
        }
      }

    }

    "close" should {

      "disallow execution of commands after and leave changes uncommited" in new Context {
        withDatabase { implicit database =>
          import database.syntax._

          Query(
            for {
              _ <- set(1, "1")
              _ <- set(2, "2")
              _ <- close()
              _ <- set(4, "4")
              _ <- set(5, "5")
              _ <- clearRange(2 -> 4)
              v <- getRange[Int, String](1 -> 6)
            } yield v
          ).expectFailure[IllegalStateException]()

          Query(getRange[Int, String](1 -> 6)).expectResult(Nil)
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
        execute() shouldBe a

      def expectFailure(t: Throwable) =
        Try(expectResult(null.asInstanceOf[A])).toEither shouldBe Left(t)

      def expectFailure[A <: Throwable: ClassTag]() =
        an[A] should be thrownBy execute()

      def expectFDBFailure(code: Int) =
        Try(expectResult(null.asInstanceOf[A])).toEither.left.get.asInstanceOf[FDBException].getCode shouldBe code

      def execute() =
        database.prepare(q).unsafeRunSync()
    }
  }

}