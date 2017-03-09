package com.github.dnvriend

import java.net.URI

import akka.actor.ActorSystem
import com.github.dnvriend.component.hello.{ SimpleService, SimpleServiceImpl }
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomServer, LocalServiceLocator }
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.test.WsTestClient
import com.softwaremill.macwire._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.concurrent.duration._

abstract class TestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with WsTestClient with ScalaFutures {
  lazy val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new LagomApplication(ctx) with LocalServiceLocator with AhcWSComponents {
      override lazy val lagomServer: LagomServer =
        LagomServer.forServices(bindService[SimpleService].to(wire[SimpleServiceImpl]))
    }
  }

  implicit val pc: PatienceConfig = PatienceConfig(timeout = 60.minutes, interval = 300.millis)
  implicit val system: ActorSystem = server.application.actorSystem
  implicit val ec: ExecutionContext = server.application.executionContext

  def getUrl(name: String): Option[URI] = {
    server.application.serviceLocator.locate(name).futureValue
  }

  def withService[A](name: String)(block: URI => WSClient => A): A = {
    val uri = getUrl(name).getOrElse(fail(s"Service not found: '$name'"))
    withClient(client => block(uri)(client))
  }

  implicit class PimpedByteArray(self: Array[Byte]) {
    def getString: String = new String(self)
  }

  implicit class PimpedFuture[T](self: Future[T]) {
    def toTry: Try[T] = Try(self.futureValue)
  }

  override protected def beforeAll(): Unit = {
    server
  }

  override protected def afterAll(): Unit = {
    server.stop()
  }

  implicit class WsClientOps(client: WSClient) {
    def withUrl(url: String)(implicit uri: URI): WSRequest = client.url(uri.toString + url)
  }
}
