package me.archdev.foundationdb.algebra

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.utils.KeyValue
import me.archdev.foundationdb._

abstract class GetAlgebraSpec extends AlgebraSpec {

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

    "stream results" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(for {
          _ <- set(1, "1")
          _ <- set(2, "2")
          _ <- set(3, "3")
          _ <- set(4, "4")
          _ <- set(5, "5")
          r <- getRangeStream[Int, String](2 -> 5, limit = 2, reverse = false, StreamingMode.ITERATOR)
        } yield r.toSeq).expectResult(Seq(KeyValue(2, "2"), KeyValue(3, "3")))
      }
    }

//    "return range from specific subspace" in new Context {
//      withDatabase { implicit database =>
//        import database.syntax._
//
//        Query(set(3, "3")).execute()
//
//        withSubspace("get-range-subspace") { implicit subspace =>
//          Query(
//            for {
//              _   <- set(1, "1")
//              _   <- set(2, "2")
//              _   <- set(3, "42")
//              res <- getRange[Int, String](1 -> 4)
//            } yield res
//          ).expectResult(Seq(KeyValue(1, "1"), KeyValue(2, "2"), KeyValue(3, "42")))
//        }
//
//        Query(get[Int, String](3)).expectResult(Some("3"))
//      }
//    }

  }

}
