package me.archdev.foundationdb

import com.apple.foundationdb.{ Database, FDB }
import me.archdev.foundationdb.clients.{ DatabaseClient, TestClient }

import scala.concurrent.ExecutionContext

object FoundationDB {

  def connect(apiVersion: Int)(implicit ec: ExecutionContext): FoundationDB =
    connect(apiVersion, null)

  def connect(apiVersion: Int, clusterFilePath: String)(implicit ec: ExecutionContext): FoundationDB =
    connect(FDB.selectAPIVersion(apiVersion).open(clusterFilePath, ExecutionContextExecutorServiceBridge(ec)))

  def connect(database: Database): FoundationDB =
    new DatabaseClient(database).asInstanceOf[FoundationDB]

  def mocked(): FoundationDB =
    new TestClient().asInstanceOf[FoundationDB]

}
