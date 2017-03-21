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

package application

import adapters.controllers.MainController
import adapters.services.AkkaHttpService
import com.github.dnvriend.api.HelloApi
import com.lightbend.lagom.scaladsl.api.{ ServiceAcl, ServiceInfo }
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.i18n.I18nComponents
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes

import scala.concurrent.ExecutionContext

/**
 * Created by dennis on 21-03-17.
 */
abstract class PlayApplication(context: Context) extends BuiltInComponentsFromContext(context)
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
  lazy val main = wire[MainController]
  lazy val simpleServer = new AkkaHttpService("0.0.0.0", 18081, helloClient)(actorSystem, materializer, executionContext)

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }
}
