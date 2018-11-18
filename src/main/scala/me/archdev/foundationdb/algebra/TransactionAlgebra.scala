package me.archdev.foundationdb.algebra

import scala.language.higherKinds

trait TransactionAlgebra[F[_]] {
  def commit(): F[Unit]
  def cancel(): F[Unit]
  def close(): F[Unit]
}
