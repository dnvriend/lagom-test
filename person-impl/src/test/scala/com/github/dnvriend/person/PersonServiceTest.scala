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

package com.github.dnvriend.person

import auth.Authentication
import com.github.dnvriend.TestSpec
import com.github.dnvriend.person.adapters.services.HelloServiceImpl
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomApplicationContext, LagomServer, LocalServiceLocator }
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.Setup
import com.softwaremill.macwire.wire
import play.api.http.HeaderNames
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.Future

class PersonServiceTest extends TestSpec {
  val config: Setup = ServiceTest.defaultSetup.withCassandra(false)
  def app(ctx: LagomApplicationContext) = new LagomApplication(ctx) with LocalServiceLocator with AhcWSComponents {
    override def lagomServer: LagomServer = LagomServer.forServices(
      bindService[HelloService].to(wire[HelloServiceImpl])
    )
  }

  def sayHello(client: HelloService, user: String, pass: String, sayHelloRequest: SayHelloRequest): Future[SayHelloResponse] = {
    client.sayHello
      .handleRequestHeader(_.addHeader(HeaderNames.AUTHORIZATION, Authentication.generateBasicAuthHeader(user, pass)))
      .invoke(sayHelloRequest)
  }

  it should "call hello service" in {
    ServiceTest.withServer(config)(ctx => app(ctx)) { server =>
      val client: HelloService = server.serviceClient.implement[HelloService]
      val response = sayHello(client, "user", "pass", SayHelloRequest("Dennis")).futureValue
      println(response)
    }
  }
}
