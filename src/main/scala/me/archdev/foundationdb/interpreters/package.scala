package me.archdev.foundationdb

import me.archdev.foundationdb.algebra.QueryAlgebra

package object interpreters {

  object DatabaseInterpreter
      extends QueryAlgebra[DatabaseContext]
      with database.GetInterpreter
      with database.SelectInterpreter
      with database.MutationInterpreter
      with database.TransactionInterpreter
      with database.UtilsInterpreter

  object InMemoryInterpreter
      extends QueryAlgebra[InMemoryContext]
      with inmemory.GetInterpreter
      with inmemory.SelectInterpreter
      with inmemory.MutationInterpreter
      with inmemory.TransactionInterpreter
      with inmemory.UtilsInterpreter

}
