package me.archdev

import cats.data.StateT
import cats.effect.IO
import com.apple.foundationdb.Transaction
import me.archdev.foundationdb.algebra.QueryAlgebra

package object foundationdb {

  type TransactionPlan[A] = StateT[IO, Transaction, A]
  type TransactionAlgebra = QueryAlgebra[TransactionPlan]

  val syntax: TransactionAlgebra = transactions.QueryAlgebraInterpreter

}
