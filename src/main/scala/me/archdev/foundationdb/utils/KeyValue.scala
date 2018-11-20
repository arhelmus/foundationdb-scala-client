package me.archdev.foundationdb.utils

import com.apple.foundationdb.{ KeyValue => JavaKeyValue }
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler

case class KeyValue[K, V](key: K, value: V)

object KeyValue {

  def parse[K: Tupler, V: Tupler](kv: JavaKeyValue)(implicit subspace: Subspace): KeyValue[K, V] =
    KeyValue(
      key = SubspaceKey.parseUnsafe[K](kv.getKey),
      value = FDBObject.parseUnsafe[V](kv.getValue)
    )

}

case class SubspaceKeyValue[K, V](key: K, value: V, subspace: Option[Subspace]) {

  lazy val keyValue = KeyValue(key, value)

}

object SubspaceKeyValue {

  def apply[K, V](key: K, value: V)(implicit subspace: Subspace): SubspaceKeyValue[K, V] =
    SubspaceKeyValue(key, value, Some(subspace))

  def parse[K: Tupler, V: Tupler](kv: JavaKeyValue)(implicit subspace: Subspace): SubspaceKeyValue[K, V] = {
    val selectedKey = SelectedKey.parseUnsafe[K](kv.getKey)
    SubspaceKeyValue(
      key = selectedKey.key,
      value = FDBObject.parseUnsafe[V](kv.getValue),
      subspace = selectedKey.maybeSubspace
    )
  }

}
