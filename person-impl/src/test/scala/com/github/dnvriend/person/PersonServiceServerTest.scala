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

import com.github.dnvriend.ServerTestSpec
import com.github.dnvriend.person.adapters.services.HelloServiceImpl
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomServer, LocalServiceLocator }
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.TestServer
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

class PersonServiceServerTest extends ServerTestSpec {
  override val server: TestServer[LagomApplication with LocalServiceLocator with AhcWSComponents] = {
    ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(false)) { ctx =>
      new LagomApplication(ctx) with LocalServiceLocator with AhcWSComponents {
        override def lagomServer: LagomServer = LagomServer.forServices(
          bindService[HelloService].to(wire[HelloServiceImpl])
        )
      }
    }
  }

  val helloServiceClient: HelloService = server.serviceClient.implement[HelloService]

  it should "" in {
    val response = helloServiceClient.sayHello.invoke(SayHelloRequest("hi")).futureValue
    println(response)
  }
}
