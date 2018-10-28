package me.archdev.foundationdb

import me.archdev.foundationdb.serializers._
import me.archdev.foundationdb.transactions._

import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App {

  case class StorageModel(a: String, b: Int, c: String)

  val fdb = FoundationDB(520)

  implicit val subspace: Subspace = Subspace("test_subspace")

  println(
    fdb
      .exec { db =>
        for {
          _      <- db.set("key", StorageModel("1", 2, "3"))
          _      <- db.set("key2", "value2")
          result <- db.get[String, StorageModel]("key")
          _      <- db.delete("key2")
        } yield result
      }
      .unsafeRunSync()
  )

}
