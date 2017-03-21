/*
 * Copyright 2016 Dennis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import java.net.URI

import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LocalServiceLocator }
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.TestServer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers, OptionValues }
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.test.WsTestClient

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

abstract class ServerTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with WsTestClient with ScalaFutures with OptionValues {

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
