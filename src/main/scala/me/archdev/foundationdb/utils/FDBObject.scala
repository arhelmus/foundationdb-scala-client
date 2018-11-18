package me.archdev.foundationdb.utils

import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.serializers._

object FDBObject {

  def parse[A: Tupler](bytes: Array[Byte]): Option[A] =
    Option(bytes)
      .map(Tuple.fromBytes)
      .map(_.fromTuple[A])

  def parseUnsafe[A: Tupler](bytes: Array[Byte]): A =
    parse[A](bytes).get

}
