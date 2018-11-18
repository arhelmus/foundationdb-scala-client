package me.archdev.foundationdb.clients
import me.archdev.foundationdb.FoundationDB

class InMemoryClientTest extends ClientSpec {
  override def buildDatabaseClient(): FoundationDB =
    FoundationDB.inMemory()
}
