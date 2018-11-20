package me.archdev.foundationdb.algebra

import com.apple.foundationdb.StreamingMode
import me.archdev.foundationdb.utils.{ KeySelector, KeyValue, SelectedKey, SubspaceKeyValue }
import me.archdev.foundationdb._
import me.archdev.foundationdb.namespaces.Subspace

abstract class SelectAlgebraSpec extends AlgebraSpec {

  "selectKey" should {

    "return value where key less than" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(for {
          _ <- set(1, "1")
          _ <- set(2, "2")
          _ <- set(3, "3")
          _ <- set(4, "4")
          r <- selectKey[Int](KeySelector.lastLessThan(3))
        } yield r).expectResult(Some(SelectedKey(None, 2)))
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
          r <- selectKey[Int](KeySelector.lastLessOrEqual(3))
        } yield r).expectResult(Some(SelectedKey(None, 3)))
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
          r <- selectKey[Int](KeySelector.firstGreaterThan(3))
        } yield r).expectResult(Some(SelectedKey(None, 4)))
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
          r <- selectKey[Int](KeySelector.firstGreaterOrEqual(3))
        } yield r).expectResult(Some(SelectedKey(None, 3)))
      }
    }

    "return value from specific subspace or search in other subspaces" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(set(3, "3")).execute()

        withSubspace("test-subspace") { implicit subspace =>
          Query(for {
            _  <- set(2, "2")
            r1 <- selectKey[Int](KeySelector.firstGreaterOrEqual(3))
            r2 <- selectKey[Int](KeySelector.firstGreaterOrEqual(2))
          } yield (r1, r2)).expectResult(
            (
              Some(SelectedKey(None, 3)),
              Some(SelectedKey(Some(subspace), 2))
            )
          )
        }

        Query(selectKey[Int](KeySelector.firstGreaterOrEqual(3))).expectResult(Some(SelectedKey(None, 3)))
      }
    }

    "return none if there is nothing to return" in new Context {
      withDatabase { implicit database =>
        import database.syntax._
        Query(selectKey[Int](KeySelector.firstGreaterOrEqual(3))).expectResult(None)
      }
    }

  }

  "findKey" should {

    "return value where key less than" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(for {
          _ <- set(1, "1")
          _ <- set(2, "2")
          _ <- set(3, "3")
          _ <- set(4, "4")
          r <- findKey[Int](KeySelector.lastLessThan(3))
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
          r <- findKey[Int](KeySelector.lastLessOrEqual(3))
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
          r <- findKey[Int](KeySelector.firstGreaterThan(3))
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
          r <- findKey[Int](KeySelector.firstGreaterOrEqual(3))
        } yield r).expectResult(Some(3))
      }
    }

    "return value from specific subspace or search in other subspaces" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(set(3, "3")).execute()

        withSubspace("test-subspace") { implicit subspace =>
          Query(for {
            _  <- set(2, "2")
            r1 <- findKey[Int](KeySelector.firstGreaterOrEqual(3))
            r2 <- findKey[Int](KeySelector.firstGreaterOrEqual(2))
          } yield (r1, r2)).expectResult((None, Some(2)))
        }

        Query(findKey[Int](KeySelector.firstGreaterOrEqual(3))).expectResult(Some(3))
      }
    }

    "return none if there is nothing to return" in new Context {
      withDatabase { implicit database =>
        import database.syntax._
        Query(selectKey[Int](KeySelector.firstGreaterOrEqual(3))).expectResult(None)
      }
    }

  }

  "selectRange" should {

    "return values in some range" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(for {
          _ <- set(1, "1")
          _ <- set(2, "2")
          _ <- set(3, "3")
          _ <- set(4, "4")
          _ <- set(5, "5")
          r <- selectRange[Int, String](KeySelector.firstGreaterThan(1) -> KeySelector.firstGreaterOrEqual(4))
        } yield r).expectResult(Seq(SubspaceKeyValue(2, "2", None), SubspaceKeyValue(3, "3", None)))
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
          r <- selectRangeWithLimit[Int, String](
            KeySelector.firstGreaterThan(1) -> KeySelector.firstGreaterOrEqual(4),
            2
          )
        } yield r).expectResult(Seq(SubspaceKeyValue(2, "2", None), SubspaceKeyValue(3, "3", None)))
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
          r <- selectRangeWithLimitReversed[Int, String](
            KeySelector.firstGreaterThan(1) -> KeySelector.firstGreaterOrEqual(4),
            2
          )
        } yield r).expectResult(Seq(SubspaceKeyValue(3, "3", None), SubspaceKeyValue(2, "2", None)))
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
          r <- selectRangeStream[Int, String](KeySelector.firstGreaterThan(1) -> KeySelector.firstGreaterOrEqual(5),
                                              limit = 2,
                                              reverse = false,
                                              StreamingMode.ITERATOR)
        } yield r.toSeq).expectResult(Seq(SubspaceKeyValue(2, "2", None), SubspaceKeyValue(3, "3", None)))
      }
    }

    "return range from specific subspace and subspaces around" in new Context {
      withDatabase { implicit database =>
        import database.syntax._

        Query(set(3, "3")).execute()
        Query(set(4, "4")).execute()

        withSubspace(1) { implicit subspace =>
          Query(
            for {
              _ <- set(1, "1")
              _ <- set(2, "2")
              res <- selectRange[Int, String](
                KeySelector.firstGreaterOrEqual(1) -> KeySelector.lastLessOrEqual(4, Subspace())
              )
            } yield res
          ).expectResult(Seq(SubspaceKeyValue(1, "1"), SubspaceKeyValue(2, "2"), SubspaceKeyValue(3, "3", None)))
        }

        Query(get[Int, String](3)).expectResult(Some("3"))
      }
    }

  }

}
