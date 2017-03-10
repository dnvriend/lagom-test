package com.github.dnvriend

import java.net.URI

import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.TestServer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, OptionValues}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.test.WsTestClient

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

abstract class TestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with WsTestClient with ScalaFutures with OptionValues {

  def server: TestServer[LagomApplication with LocalServiceLocator with AhcWSComponents]

  implicit val pc: PatienceConfig = PatienceConfig(timeout = 60.minutes, interval = 300.millis)

  def getUrl(name: String): Option[URI] = {
    server.application.serviceLocator.locate(name).futureValue
  }

  def withServiceName[A](name: String)(block: URI => WSClient => A): A = {
    val uri = getUrl(name).getOrElse(fail(s"Service not found: '$name'"))
    withClient(client => block(uri)(client))
  }

  def withService[A](block: => URI => WSClient => A): A = {
    val name: String = server.application.serviceInfo.locatableServices.head._1
    val uri = getUrl(name).getOrElse(fail(s"Service not found: '$name'"))
    withClient(client => block(uri)(client))
  }

  implicit class PimpedByteArray(self: Array[Byte]) {
    def getString: String = new String(self)
  }

  implicit class PimpedFuture[T](self: Future[T]) {
    def toTry: Try[T] = Try(self.futureValue)
  }

  override protected def afterAll(): Unit = {
    server.stop()
  }

  implicit class WsClientOps(client: WSClient) {
    def withUrl(url: String)(implicit uri: URI): WSRequest = {
      val newUrl = uri.toString + url
      client.url(newUrl)
    }
  }
}
