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
