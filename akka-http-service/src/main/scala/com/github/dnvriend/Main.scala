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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import com.github.dnvriend.component.hello.{ Credentials, HelloApi }
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, ServiceLocator }
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer }
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }
import com.lightbend.lagom.scaladsl.api.Service._

import scala.concurrent.{ ExecutionContext, Future }

trait NoopService extends Service {
  def noop: ServiceCall[NotUsed, NotUsed]
  override def descriptor: Descriptor =
    named("noop").withCalls(call(noop)).withAutoAcl(true)
}

class NoopServiceImpl extends NoopService {
  override def noop: ServiceCall[NotUsed, NotUsed] = ServiceCall(_ => Future.successful(NotUsed))
}

class LagomScalaLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomScalaApplication(context) with AhcWSComponents {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomScalaApplication(context) with AhcWSComponents with LagomDevModeComponents

  override def describeServices = List(readDescriptor[NoopService])
}

abstract class LagomScalaApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents {
  println("===> Launching akka-http-service")

  override lazy val lagomServer: LagomServer =
    LagomServer.forServices(bindService[NoopService].to(new NoopServiceImpl))

  implicit val system: ActorSystem = actorSystem
  val helloClient: HelloApi = serviceClient.implement[HelloApi]
  val simpleServer = new SimpleServer("0.0.0.0", 18080, helloClient)
}

class SimpleServer(interface: String, port: Int, helloClient: HelloApi)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) {
  Http().bindAndHandle(SimpleServerRoutes.routes(helloClient), interface, port)
}

object SimpleServerRoutes extends Directives {
  def routes(helloClient: HelloApi)(implicit mat: Materializer, ec: ExecutionContext): Route =
    logRequestResult("akka-http-service") {
      pathPrefix("api") {
        path("ping") {
          complete("pong")
        } ~
          path("creds") {
            // http :18080/api/creds
            complete(helloClient.createToken.invoke(Credentials("foo", "bar")))
          }
      }
    }
}