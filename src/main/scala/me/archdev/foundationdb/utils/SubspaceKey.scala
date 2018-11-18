package me.archdev.foundationdb.utils

import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._

object SubspaceKey {

  def parse[A: Tupler](bytes: Array[Byte])(implicit subspace: Subspace): Option[A] =
    Option(bytes)
      .filter(isFDBOutputMeaningful)
      .filter(subspace.raw.contains)
      .map(subspace.raw.unpack)
      .map(_.fromTuple[A])

  def parseUnsafe[A: Tupler](bytes: Array[Byte])(implicit subspace: Subspace): A =
    parse[A](bytes).get

}
