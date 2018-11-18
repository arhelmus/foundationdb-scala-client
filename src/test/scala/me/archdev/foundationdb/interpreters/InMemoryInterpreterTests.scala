package me.archdev.foundationdb.interpreters

import me.archdev.foundationdb.FoundationDB
import me.archdev.foundationdb.algebra._

class GetInterpreterInMemoryTest         extends GetAlgebraSpec with InMemoryInterpreterSpec
class MutationInterpreterInMemoryTest    extends MutationAlgebraSpec with InMemoryInterpreterSpec
class SelectInterpreterInMemoryTest      extends SelectAlgebraSpec with InMemoryInterpreterSpec
class TransactionInterpreterInMemoryTest extends TransactionAlgebraSpec with InMemoryInterpreterSpec

trait InMemoryInterpreterSpec extends AlgebraSpec {

  override def buildDatabaseClient(): FoundationDB = FoundationDB.inMemory()

}
