package me.archdev.foundationdb.interpreters

import me.archdev.foundationdb.FoundationDB
import me.archdev.foundationdb.algebra._
import me.archdev.foundationdb.utils.ITTestSpec

class GetInterpreterDatabaseTest         extends GetAlgebraSpec with DatabaseInterpreterSpec
class MutationInterpreterDatabaseTest    extends MutationAlgebraSpec with DatabaseInterpreterSpec
class SelectInterpreterDatabaseTest      extends SelectAlgebraSpec with DatabaseInterpreterSpec
class TransactionInterpreterDatabaseTest extends TransactionAlgebraSpec with DatabaseInterpreterSpec

trait DatabaseInterpreterSpec extends AlgebraSpec with ITTestSpec {
  override def buildDatabaseClient(): FoundationDB =
    FoundationDB.connect(520, fdbClusterFilePath)
}
