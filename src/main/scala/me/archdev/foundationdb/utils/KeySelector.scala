package me.archdev.foundationdb.utils

import com.apple.foundationdb.{ KeySelector => JavaKeySelector }
import me.archdev.foundationdb.namespaces.{ Subspace => Sub }
import me.archdev.foundationdb.serializers.Tupler

case class KeySelector(raw: JavaKeySelector, ksType: KeySelectorType, subspace: Sub) {
  def unpacked = subspace.raw.unpack(raw.getKey)
}

object KeySelector {

  def lastLessThan[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.lastLessThan(sub.pack(key)), LessThan, sub)

  def lastLessThan[K: Tupler](key: K, subspace: Sub): KeySelector = {
    implicit val sub: Sub = subspace
    lastLessThan(key)
  }

  def lastLessOrEqual[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.lastLessOrEqual(sub.pack(key)), LessOrEqual, sub)

  def lastLessOrEqual[K: Tupler](key: K, subspace: Sub): KeySelector = {
    implicit val sub: Sub = subspace
    lastLessOrEqual(key)
  }

  def firstGreaterThan[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.firstGreaterThan(sub.pack(key)), GreaterThan, sub)

  def firstGreaterThan[K: Tupler](key: K, subspace: Sub): KeySelector = {
    implicit val sub: Sub = subspace
    firstGreaterThan(key)
  }

  def firstGreaterOrEqual[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.firstGreaterOrEqual(sub.pack(key)), GreaterOrEqual, sub)

  def firstGreaterOrEqual[K: Tupler](key: K, subspace: Sub): KeySelector = {
    implicit val sub: Sub = subspace
    firstGreaterOrEqual(key)
  }

}

sealed trait KeySelectorType
case object LessThan       extends KeySelectorType
case object LessOrEqual    extends KeySelectorType
case object GreaterThan    extends KeySelectorType
case object GreaterOrEqual extends KeySelectorType
