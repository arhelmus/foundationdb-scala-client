package me.archdev.foundationdb.namespaces

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

  def mocked(path: Seq[String]): Directory =
    Directory(darkRitualOfDirectorySubspaceSummoning(path, Array[Byte](), DirectoryLayer.getDefault))

  def mocked(path: Seq[String], layer: Array[Byte]): Directory =
    Directory(darkRitualOfDirectorySubspaceSummoning(path, Array[Byte](), DirectoryLayer.getDefault, layer))

  private def darkRitualOfDirectorySubspaceSummoning(path: Seq[String],
                                                     prefix: Array[Byte],
                                                     dirLayer: DirectoryLayer): JavaDirectorySubspace = {
    import java.lang.reflect.Constructor
    var constructor: Constructor[JavaDirectorySubspace] = null
    constructor = classOf[JavaDirectorySubspace].getDeclaredConstructor(classOf[java.util.List[String]],
                                                                        classOf[Array[Byte]],
                                                                        classOf[DirectoryLayer])
    constructor.setAccessible(true)
    constructor.newInstance(path.asJava, prefix, dirLayer)
  }

  private def darkRitualOfDirectorySubspaceSummoning(path: Seq[String],
                                                     prefix: Array[Byte],
                                                     dirLayer: DirectoryLayer,
                                                     layer: Array[Byte]): JavaDirectorySubspace = {
    import java.lang.reflect.Constructor
    var constructor: Constructor[JavaDirectorySubspace] = null
    constructor = classOf[JavaDirectorySubspace].getDeclaredConstructor(classOf[java.util.List[String]],
                                                                        classOf[Array[Byte]],
                                                                        classOf[DirectoryLayer],
                                                                        classOf[Array[Byte]])
    constructor.setAccessible(true)
    constructor.newInstance(path.asJava, prefix, dirLayer, layer)
  }

}
