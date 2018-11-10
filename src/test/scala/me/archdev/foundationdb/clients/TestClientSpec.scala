package me.archdev.foundationdb.clients
import me.archdev.foundationdb.FoundationDB

class TestClientSpec extends ClientSpec {
  override def buildDatabaseClient(): FoundationDB =
    FoundationDB.mocked()
}
