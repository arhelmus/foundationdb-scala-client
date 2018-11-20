package me.archdev.foundationdb.utils

import com.apple.foundationdb.tuple.Tuple
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

  def toTuple[K: Tupler](k: K)(implicit subspace: Subspace): Tuple =
    Tuple.fromBytes(subspace.pack(k))

}
