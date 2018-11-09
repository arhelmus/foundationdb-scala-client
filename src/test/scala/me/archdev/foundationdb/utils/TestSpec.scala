package me.archdev.foundationdb.utils

import java.io.{ File, PrintWriter }
import java.util.function.Consumer

import com.dimafeng.testcontainers.{ ForEachTestContainer, GenericContainer }
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.{ ExposedPort, PortBinding, Ports }
import me.archdev.foundationdb.namespaces.Subspace
import me.archdev.foundationdb.serializers.Tupler
import org.scalatest.{ Matchers, WordSpec }
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.io.Source

trait TestSpec extends WordSpec with Matchers {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit class FutureSyntax[T](future: Future[T]) {
    def await: T =
      Await.result(future, 5.seconds)
  }

  def withSubspace[A: Tupler, B](subspace: A)(f: Subspace => B): B =
    f(Subspace(subspace))

}

trait ITTestSpec extends TestSpec with ForEachTestContainer {

  val fdbClusterFilePath = "fdb.cluster"
  val fdbPort            = 4500

  val cmd: Consumer[CreateContainerCmd] = e =>
    e.withPortBindings(new PortBinding(Ports.Binding.bindPort(fdbPort), new ExposedPort(fdbPort)))

  override val container: GenericContainer =
    GenericContainer(
      "archdev/foundationdb:latest",
      exposedPorts = Seq(4500),
      waitStrategy = Wait.forListeningPort()
    ).configure(_.withCreateContainerCmdModifier(cmd))

  override def afterStart() = {
    container.container.copyFileFromContainer("/etc/foundationdb/fdb.cluster", fdbClusterFilePath)
    updateClusterConfig(
      composeFoundationDbAddress(container),
      fdbClusterFilePath
    )
  }

  private def updateClusterConfig(foundationDbAddress: String, configPath: String): Unit = {
    val updatedClusterConfig = Source
      .fromFile(configPath)
      .getLines()
      .mkString
      .replace("127.0.0.1:4500", foundationDbAddress)

    val writer = new PrintWriter(new File(configPath))
    writer.write(updatedClusterConfig)
    writer.close()
  }

  private def composeFoundationDbAddress(container: GenericContainer) = {
    val containerIp = container.containerIpAddress match {
      case "localhost" => "127.0.0.1"
      case other       => other
    }

    s"$containerIp:${container.mappedPort(4500)}"
  }

}
