package me.archdev.foundationdb.utils

import com.apple.foundationdb.tuple.Tuple
import me.archdev.foundationdb.namespaces._
import me.archdev.foundationdb.serializers._

case class SelectedKey[K](maybeSubspace: Option[Subspace], key: K)

object SelectedKey {

  def parse[A: Tupler](bytes: Array[Byte])(implicit subspace: Subspace): Option[SelectedKey[A]] =
    Option(bytes)
      .filter(isFDBOutputMeaningful)
      .map {
        case fdbKeyBytes if !subspace.isEmpty && subspace.raw.contains(fdbKeyBytes) =>
          SelectedKey(Some(subspace), subspace.raw.unpack(fdbKeyBytes).fromTuple[A])
        case fdbKeyBytes =>
          SelectedKey(None, Tuple.fromBytes(fdbKeyBytes).fromTuple[A])
      }

  def parseUnsafe[A: Tupler](bytes: Array[Byte])(implicit subspace: Subspace): SelectedKey[A] =
    parse[A](bytes).get

  def toSubspaceKey[A](selectedKey: SelectedKey[A])(implicit subspace: Subspace): Option[A] =
    (selectedKey.maybeSubspace, subspace) match {
      case (Some(a), b) if a == b => Some(selectedKey.key)
      case (None, b) if b.isEmpty => Some(selectedKey.key)
      case _                      => None
    }

  def pack[K: Tupler](selectedKey: SelectedKey[K]): Array[Byte] =
    selectedKey match {
      case SelectedKey(Some(subspace), key) =>
        subspace.pack(key)
      case SelectedKey(None, key) =>
        key.toTuple.pack()
    }

  def toTuple[K: Tupler](selectedKey: SelectedKey[K]): Tuple =
    selectedKey.maybeSubspace
      .map(implicit s => SubspaceKey.toTuple[K](selectedKey.key))
      .getOrElse(selectedKey.key.toTuple)

  def range[K](range: (K, K))(implicit subspace: Subspace): (SelectedKey[K], SelectedKey[K]) =
    (
      SelectedKey(Some(subspace), range._1),
      SelectedKey(Some(subspace), range._2)
    )

}
