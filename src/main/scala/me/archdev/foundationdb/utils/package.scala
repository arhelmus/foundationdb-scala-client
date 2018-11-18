package me.archdev.foundationdb

package object utils {

  private[utils] def isFDBOutputMeaningful(output: Array[Byte]): Boolean =
    !output.sameElements(Array(-1))

}
