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

import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, ServiceCall }
import play.api.libs.json.{ Format, Json }

object HelloService {
  final val Name = "hello-service"
}

final case class SayHelloRequest(name: String)
object SayHelloRequest {
  implicit val format: Format[SayHelloRequest] = Json.format
}

final case class SayHelloResponse(message: String)
object SayHelloResponse {
  implicit val format: Format[SayHelloResponse] = Json.format
}

trait HelloService extends Service {
  def sayHello: ServiceCall[SayHelloRequest, SayHelloResponse]

  override def descriptor: Descriptor = {
    named(HelloService.Name)
      .withCalls(
        pathCall("/api/hello", sayHello)
      ).withAutoAcl(true)
  }
}
