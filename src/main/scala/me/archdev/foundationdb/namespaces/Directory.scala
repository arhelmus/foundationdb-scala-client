package me.archdev.foundationdb.namespaces

import com.apple.foundationdb.directory.{ DirectorySubspace => JavaDirectorySubspace }
import me.archdev.foundationdb.serializers._

case class Directory(raw: JavaDirectorySubspace) {

  def buildSubspace(): Subspace =
    Subspace(raw)

  def buildSubspace[A: Tupler](prefix: A): Subspace =
    Subspace(raw.subspace(prefix.toTuple))

}
