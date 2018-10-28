package me.archdev.foundationdb.serializers

import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.utils.TestSpec

class ShapelessDerivationSpec extends TestSpec {

  "Shapeless derivation" should {

    "generate tuple out of basic types" in new Context {
      tuplerTest(true)
      tuplerTest('t')
      tuplerTest("test")
      tuplerTest(1.toByte)
      tuplerTest(255.toShort)
      tuplerTest(42324)
      tuplerTest(4252341212L)
      tuplerTest(36.6F)
      tuplerTest(36.6)
    }

    "generate tuple out of Scala tuple" in new Context {
      tuplerTest(("Arthur", "Kushka", 1995, true))
    }

    "generate tuple out of case class" in new Context {
      case class DataStorageModel(a: String, b: Int, c: Boolean)
      tuplerTest(DataStorageModel("test", 42, true))
    }

  }

  trait Context {

    def tuplerTest[A](value: A)(implicit tupler: Tupler[A]) =
      "test".toTuple shouldBe Tuple.from("test")

  }

}
