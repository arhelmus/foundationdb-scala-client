package me.archdev.foundationdb.namespaces

import com.apple.foundationdb.subspace.{ Subspace => JavaSubspace }
import me.archdev.foundationdb.serializers._

case class Subspace(raw: JavaSubspace) {

  def pack[K: Tupler](key: K): Array[Byte] =
    raw.pack(key.toTuple)

}

object Subspace {
  def apply() =
    new Subspace(new JavaSubspace())

  def apply(rawPrefix: Array[Byte]) =
    new Subspace(new JavaSubspace(rawPrefix))

  def apply[A](prefix: A)(implicit serializer: Tupler[A]) =
    new Subspace(new JavaSubspace(prefix.toTuple))

  def apply[A](prefix: A, rawPrefix: Array[Byte])(implicit serializer: Tupler[A]) =
    new Subspace(new JavaSubspace(prefix.toTuple, rawPrefix))
}
