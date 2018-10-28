package me.archdev.foundationdb

import me.archdev.foundationdb.utils.ITTestSpec
import me.archdev.foundationdb.transactions._

class FoundationDBITSpec extends ITTestSpec {

  "FoundationDB wrapper" should {

    "support base transaction API" in {
      val db = FoundationDB(520)

      val transaction = db.exec(
        q =>
          for {
            _               <- q.set("sence-of-life", 42)
            _               <- q.set("Arthur", "Kushka")
            _               <- q.set("language", "PHP")
            _               <- q.delete("language")
            senceOfLife     <- q.get[String, Int]("sence-of-life")
            arthursLastName <- q.get[String, String]("Arthur")
            language        <- q.get[String, String]("language")
          } yield (senceOfLife, arthursLastName, language)
      )

      transaction.unsafeRunSync() shouldBe (Some(42), Some("Kushka"), None)
    }

    "support namespaces" in {
      val db                          = FoundationDB(520)
      implicit var subspace: Subspace = Subspace("my-pretty-subspace")

      val transaction = db.exec(
        q =>
          for {
            _           <- q.set("sence-of-life", 42)
            senceOfLife <- q.get[String, Int]("sence-of-life")
          } yield senceOfLife
      )

      transaction.unsafeRunSync() shouldBe Some(42)

      subspace = Subspace("another-subspace")

      val anotherTransaction = db.exec(
        q =>
          for {
            senceOfLife <- q.get[String, Int]("sence-of-life")
          } yield senceOfLife
      )

      anotherTransaction.unsafeRunSync() shouldBe None
    }

    "support case class generic derivation" in {
      val db = FoundationDB(520)

      case class DataStorageClass(a: String, b: Int)

      val transaction = db.exec(
        q =>
          for {
            _                <- q.set("derivation-test", DataStorageClass("test", 42))
            dataStorageClass <- q.get[String, DataStorageClass]("derivation-test")
          } yield dataStorageClass
      )

      transaction.unsafeRunSync() shouldBe Some(DataStorageClass("test", 42))
    }

  }

}
