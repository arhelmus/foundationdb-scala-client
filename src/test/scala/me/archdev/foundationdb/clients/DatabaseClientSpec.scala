package me.archdev.foundationdb.clients

import me.archdev.foundationdb.FoundationDB
import me.archdev.foundationdb.utils.ITTestSpec

class DatabaseClientSpec extends ClientSpec with ITTestSpec {
  override def buildDatabaseClient(): FoundationDB =
    FoundationDB.connect(520, fdbClusterFilePath)
}
