package me.archdev.foundationdb.algebra

import me.archdev.foundationdb.utils.KeyValue
import me.archdev.foundationdb._

abstract class MutationAlgebraSpec extends AlgebraSpec {

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

}
