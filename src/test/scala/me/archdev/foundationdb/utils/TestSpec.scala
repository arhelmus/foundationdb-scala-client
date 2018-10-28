package me.archdev.foundationdb.utils

import com.dimafeng.testcontainers.{ ForEachTestContainer, GenericContainer }
import org.scalatest.{ Matchers, WordSpec }
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

trait TestSpec extends WordSpec with Matchers {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit class FutureSyntax[T](future: Future[T]) {
    def await: T =
      Await.result(future, 5.seconds)
  }

}

trait ITTestSpec extends TestSpec with ForEachTestContainer {

  override val container = GenericContainer(
    "archdev/foundationdb:latest",
    exposedPorts = Seq(4500),
    waitStrategy = Wait.forListeningPort()
  )

}
