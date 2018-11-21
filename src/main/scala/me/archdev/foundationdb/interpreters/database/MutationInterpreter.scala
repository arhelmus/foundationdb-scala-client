package me.archdev.foundationdb.interpreters.database

import com.apple.foundationdb.MutationType
import me.archdev.foundationdb.DatabaseContext
import me.archdev.foundationdb.algebra.MutationAlgebra
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers._

trait MutationInterpreter extends MutationAlgebra[DatabaseContext] {

  override def set[K: Tupler, V: Tupler](key: K, value: V)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.set(subspace.pack(key), value.toTuple.pack())
      null
    }

  override def mutate[K: Tupler, P: Tupler](mutationType: MutationType, key: K, param: P)(
      implicit subspace: Subspace = Subspace()
  ): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.mutate(mutationType, subspace.pack(key), param.toTuple.pack())
      null
    }

  override def clear[K: Tupler](key: K)(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.clear(subspace.pack(key))
      null
    }

  override def clearRange[K: Tupler](range: (K, K))(implicit subspace: Subspace): DatabaseContext[Unit] =
    transactionAction { tr =>
      tr.clear(subspace.pack(range._1), subspace.pack(range._2))
      null
    }

}

object MutationInterpreter extends MutationInterpreter
