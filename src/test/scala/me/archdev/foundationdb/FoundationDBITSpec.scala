package me.archdev.foundationdb

import me.archdev.foundationdb.syntax._
import me.archdev.foundationdb.utils.ITTestSpec

class FoundationDBITSpec extends ITTestSpec {

  "FoundationDB wrapper" should {

    "support base transaction API" in {
      val db = FoundationDB(520)

      val transaction = db.prepare(
        for {
          _               <- set("sence-of-life", 42)
          _               <- set("Arthur", "Kushka")
          _               <- set("language", "PHP")
          _               <- delete("language")
          senceOfLife     <- get[String, Int]("sence-of-life")
          arthursLastName <- get[String, String]("Arthur")
          language        <- get[String, String]("language")
        } yield (senceOfLife, arthursLastName, language)
      )

      transaction.unsafeRunSync() shouldBe (Some(42), Some("Kushka"), None)
    }

    "support namespaces" in {
      val db                          = FoundationDB(520)
      implicit var subspace: Subspace = Subspace("my-pretty-subspace")

      val transaction = db.prepare(
        for {
          _           <- set("sence-of-life", 42)
          senceOfLife <- get[String, Int]("sence-of-life")
        } yield senceOfLife
      )

      transaction.unsafeRunSync() shouldBe Some(42)

      subspace = Subspace("another-subspace")

      val anotherTransaction = db.prepare(
        for {
          senceOfLife <- get[String, Int]("sence-of-life")
        } yield senceOfLife
      )

      anotherTransaction.unsafeRunSync() shouldBe None
    }

    "support case class generic derivation" in {
      val db = FoundationDB(520)

      case class DataStorageClass(a: String, b: Int)

      val transaction = db.prepare(
        for {
          _                <- set("derivation-test", DataStorageClass("test", 42))
          dataStorageClass <- get[String, DataStorageClass]("derivation-test")
        } yield dataStorageClass
      )

      transaction.unsafeRunSync() shouldBe Some(DataStorageClass("test", 42))
    }

  }

}
