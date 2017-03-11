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

import com.github.dnvriend.SimpleServer
import com.github.dnvriend.component.hello.HelloApi
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{ ServiceAcl, ServiceInfo }
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.softwaremill.macwire._
import controllers.Main
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ ApplicationLoader, BuiltInComponentsFromContext, Mode }
import router.Routes

import scala.concurrent.ExecutionContext

abstract class PlayService(context: Context) extends BuiltInComponentsFromContext(context)
    with I18nComponents
    with AhcWSComponents
    with LagomServiceClientComponents {

  println("==> Launching PlayService")

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "play-service",
    Map(
      "play-service" -> scala.collection.immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  lazy val helloClient = serviceClient.implement[HelloApi]
  lazy val main = wire[Main]
  lazy val simpleServer = new SimpleServer("0.0.0.0", 18081, helloClient)(actorSystem, materializer, executionContext)

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }
}

class PlayServiceLoader extends ApplicationLoader {
  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      new PlayService(context) with LagomDevModeComponents {}.application
    case _ =>
      new PlayService(context) {
        override def serviceLocator = NoServiceLocator
      }.application
  }
}
