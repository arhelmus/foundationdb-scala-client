package me.archdev.foundationdb

import com.apple.foundationdb.directory.{ DirectoryLayer, DirectorySubspace => JavaDirectorySubspace }
import me.archdev.foundationdb.serializers._

import scala.collection.JavaConverters._

case class Directory(raw: JavaDirectorySubspace) {

  def buildSubspace(): Subspace =
    Subspace(raw)

  def buildSubspace[A: Tupler](prefix: A): Subspace =
    Subspace(raw.subspace(prefix.toTuple))

}

object Directory {

  def apply(foundationDB: FoundationDB, directoryPath: Seq[String]): Directory =
    Directory(
      DirectoryLayer.getDefault.createOrOpen(foundationDB.db, directoryPath.asJava).get()
    )

  def apply(foundationDB: FoundationDB, directoryPath: Seq[String], layer: Array[Byte]): Directory =
    Directory(
      DirectoryLayer.getDefault.createOrOpen(foundationDB.db, directoryPath.asJava, layer).get()
    )

}
