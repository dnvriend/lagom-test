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

package com.github.dnvriend.application

import akka.actor.ActorSystem
import com.github.dnvriend.adapters.api.NoopService
import com.github.dnvriend.adapters.services.{ AkkaHttpService, NoopServiceImpl }
import com.github.dnvriend.api.HelloApi
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomApplicationContext, LagomServer }
import play.api.libs.ws.ahc.AhcWSComponents

abstract class LagomScalaApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents {

  override lazy val lagomServer: LagomServer = {
    LagomServer.forServices(bindService[NoopService].to(new NoopServiceImpl))
  }

  implicit val system: ActorSystem = actorSystem
  val helloClient: HelloApi = serviceClient.implement[HelloApi]
  val simpleServer = new AkkaHttpService("0.0.0.0", 18080, helloClient)
}
