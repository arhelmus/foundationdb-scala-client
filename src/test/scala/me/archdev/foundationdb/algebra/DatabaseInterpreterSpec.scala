package me.archdev.foundationdb.algebra
import me.archdev.foundationdb.FoundationDB
import me.archdev.foundationdb.utils.ITTestSpec

class DatabaseInterpreterSpec extends AlgebraSpec with ITTestSpec {
  override def buildDatabaseClient(): FoundationDB =
    FoundationDB.connect(520, fdbClusterFilePath)
}
