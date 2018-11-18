package me.archdev.foundationdb

import java.util.concurrent.CompletableFuture

import com.apple.foundationdb.{ StreamingMode, Transaction }
import me.archdev.foundationdb.namespaces.{ Subspace => Sub }
import me.archdev.foundationdb.serializers._

import scala.language.higherKinds

package object algebra {

  trait QueryAlgebra[F[_]] {
    def set[K: Tupler, V: Tupler](key: K, value: V)(implicit s: Sub = Sub()): F[Unit]
    def get[K: Tupler, V: Tupler](key: K)(implicit s: Sub = Sub()): F[Option[V]]
    def selectKey[K: Tupler](key: KeySelector)(implicit s: Sub = Sub()): F[Option[SelectedKey[K]]]
    def findKey[K: Tupler](key: KeySelector)(implicit s: Sub = Sub()): F[Option[K]]

    def getRange[K: Tupler, V: Tupler](range: (K, K))(implicit s: Sub = Sub()): F[Seq[KeyValue[K, V]]]
    def getRangeWithLimit[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
        implicit s: Sub = Sub()
    ): F[Seq[KeyValue[K, V]]]
    def getRangeWithLimitReversed[K: Tupler, V: Tupler](range: (K, K), limit: Int)(
        implicit s: Sub = Sub()
    ): F[Seq[KeyValue[K, V]]]
    def getRangeStream[K: Tupler, V: Tupler](range: (K, K), limit: Int, reverse: Boolean, streamingMode: StreamingMode)(
        implicit s: Sub = Sub()
    ): F[Iterator[KeyValue[K, V]]]

    def selectRange[K: Tupler, V: Tupler](range: (KeySelector, KeySelector))(
        implicit s: Sub = Sub()
    ): F[Seq[KeyValue[K, V]]]
    def selectRangeWithLimit[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
        implicit s: Sub = Sub()
    ): F[Seq[KeyValue[K, V]]]
    def selectRangeWithLimitReversed[K: Tupler, V: Tupler](range: (KeySelector, KeySelector), limit: Int)(
        implicit s: Sub = Sub()
    ): F[Seq[KeyValue[K, V]]]
    def selectRangeStream[K: Tupler, V: Tupler](range: (KeySelector, KeySelector),
                                                limit: Int,
                                                reverse: Boolean,
                                                streamingMode: StreamingMode)(
        implicit s: Sub = Sub()
    ): F[Iterator[KeyValue[K, V]]]

    def clear[K: Tupler](key: K)(implicit s: Sub = Sub()): F[Unit]
    def clearRange[K: Tupler](range: (K, K))(implicit s: Sub = Sub()): F[Unit]

    def commit(): F[Unit]
    def cancel(): F[Unit]
    def close(): F[Unit]
    def raw[V](f: Transaction => CompletableFuture[V]): F[V]
  }

  sealed trait QueryErrors
  case class DeserializationError(error: String) extends RuntimeException(s"Unable to deserialize Tuple, $error")

  case class KeyValue[K, V](key: K, value: V)
  case class SelectedKey[K](subspace: Option[Sub], key: K)

}
