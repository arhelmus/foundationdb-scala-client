package me.archdev.foundationdb.clients

import me.archdev.foundationdb._
import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb.utils.TestSpec

import scala.concurrent.Future

abstract class ClientSpec extends TestSpec {

  def buildDatabaseClient(): FoundationDB

  "Client" when {

    "prepare" should {

      "prepare query without execution" in new Context {
        import database.syntax._

        database.prepare(set("prepare-test", "test"))
        database.execute(get("prepare-test")).await shouldBe None
      }

    }

    "execute" should {

      "apply query on database" in new Context {
        import database.syntax._

        database
          .execute(for {
            _ <- set("execute-test", "1")
            v <- get[String, String]("execute-test")
          } yield v)
          .await shouldBe Some("1")
      }

      "mutate state of database" in new Context {
        import database.syntax._

        val flow: Future[Option[String]] = for {
          _ <- database.execute(set("flow-test", "test"))
          v <- database.execute(get("flow-test"))
        } yield v

        flow.await shouldBe Some("test")
      }

    }

    "open directory" should {

      "prepare directory to be opened" in new Context {
        database.openDirectory(Seq("path", "to", "dir"))
      }

      "provide opened directory" in new Context {
        database.openDirectorySync(Seq("path", "to", "dir")).buildSubspace().raw shouldNot be(null)
        database.openDirectorySync(Seq("path", "to", "dir"), Array[Byte](1, 0, 1)).buildSubspace().raw shouldNot be(
          null
        )
      }

    }

  }

  trait Context {
    val database: FoundationDB = buildDatabaseClient()
  }

}
