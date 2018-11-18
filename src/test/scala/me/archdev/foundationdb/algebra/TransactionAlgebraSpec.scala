package me.archdev.foundationdb.algebra

import me.archdev.foundationdb.utils.KeyValue
import me.archdev.foundationdb._

abstract class TransactionAlgebraSpec extends AlgebraSpec {

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
