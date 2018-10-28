package me.archdev.foundationdb

import com.apple.foundationdb.tuple.Tuple
import shapeless._

package object serializers {

  trait Tupler[A] {
    def write(tuple: Tuple, value: A): Tuple
    def read(tuple: Tuple, idx: Int): A
  }

  implicit val StringToTuple: Tupler[String] =
    new Tupler[String] {
      override def write(tuple: Tuple, value: String): Tuple = tuple.add(value)
      override def read(tuple: Tuple, idx: Int): String      = tuple.getString(idx)
    }

  implicit val IntToTuple: Tupler[Int] =
    new Tupler[Int] {
      override def write(tuple: Tuple, value: Int): Tuple = tuple.add(value)
      override def read(tuple: Tuple, idx: Int): Int      = tuple.getLong(idx).toInt
    }

  implicit val hnilToTuple: Tupler[HNil] =
    new Tupler[HNil] {
      override def write(tuple: Tuple, value: HNil): Tuple = tuple
      override def read(tuple: Tuple, idx: Int): HNil      = HNil
    }

  implicit def hconsToTuple[Head, Tail <: HList](implicit headSerializer: Tupler[Head],
                                                 tailSerializer: Tupler[Tail]): Tupler[Head :: Tail] =
    new Tupler[Head :: Tail] {
      override def write(tuple: Tuple, hList: Head :: Tail): Tuple =
        tailSerializer.write(headSerializer.write(tuple, hList.head), hList.tail)
      override def read(tuple: Tuple, idx: Int): Head :: Tail =
        headSerializer.read(tuple, idx) :: tailSerializer.read(tuple, idx + 1)
    }

  implicit def lgenToTuple[T, Repr](implicit lgen: Generic.Aux[T, Repr], reprSerializer: Tupler[Repr]): Tupler[T] =
    new Tupler[T] {
      override def write(tuple: Tuple, value: T): Tuple = reprSerializer.write(tuple, lgen.to(value))
      override def read(tuple: Tuple, idx: Int): T      = lgen.from(reprSerializer.read(tuple, idx))
    }

  implicit class ToTupleOps[T](t: T)(implicit serializer: Tupler[T]) {
    def toTuple: Tuple = serializer.write(new Tuple, t)
  }

  implicit class FromTupleOps(tuple: Tuple) {
    def fromTuple[T](implicit serializer: Tupler[T]): T = serializer.read(tuple, 0)
  }

}
