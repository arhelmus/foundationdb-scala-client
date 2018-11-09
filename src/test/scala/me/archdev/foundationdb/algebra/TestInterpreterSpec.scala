package me.archdev.foundationdb.algebra
import me.archdev.foundationdb.FoundationDB

class TestInterpreterSpec extends AlgebraSpec {
  override def buildDatabaseClient(): FoundationDB = FoundationDB.mocked()
}
