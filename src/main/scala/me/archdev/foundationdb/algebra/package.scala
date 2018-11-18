package me.archdev.foundationdb

import scala.language.higherKinds

package object algebra {

  trait QueryAlgebra[F[_]]
      extends GetAlgebra[F]
      with SelectAlgebra[F]
      with MutationAlgebra[F]
      with TransactionAlgebra[F]
      with UtilsAlgebra[F]

  sealed trait QueryErrors
  case class DeserializationError(error: String) extends RuntimeException(s"Unable to deserialize Tuple, $error")

}
