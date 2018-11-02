package me.archdev.foundationdb

import com.apple.foundationdb.{ Database, FDB }
import me.archdev.foundationdb.clients.{ DatabaseClient, TestClient }

object FoundationDB {

  def connect(apiVersion: Int): FoundationDB =
    connect(FDB.selectAPIVersion(apiVersion).open())

  def connect(database: Database): FoundationDB =
    new DatabaseClient(database).asInstanceOf[FoundationDB]

  def mocked(): FoundationDB =
    new TestClient().asInstanceOf[FoundationDB]

}
