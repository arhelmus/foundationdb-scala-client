package me.archdev.foundationdb

import com.apple.foundationdb.{ KeySelector => JavaKeySelector }
import me.archdev.foundationdb.namespaces.{ Subspace => Sub }
import me.archdev.foundationdb.serializers.Tupler

case class KeySelector(raw: JavaKeySelector, ksType: KeySelectorType, subspace: Sub)

object KeySelector {

  def lastLessThan[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.lastLessThan(sub.pack(key)), LessThan, sub)

  def lastLessOrEqual[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.lastLessOrEqual(sub.pack(key)), LessOrEqual, sub)

  def firstGreaterThan[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.firstGreaterThan(sub.pack(key)), GreaterThan, sub)

  def firstGreaterOrEqual[K: Tupler](key: K)(implicit sub: Sub = Sub()): KeySelector =
    new KeySelector(JavaKeySelector.firstGreaterOrEqual(sub.pack(key)), GreaterOrEqual, sub)

}

sealed trait KeySelectorType
case object LessThan       extends KeySelectorType
case object LessOrEqual    extends KeySelectorType
case object GreaterThan    extends KeySelectorType
case object GreaterOrEqual extends KeySelectorType
