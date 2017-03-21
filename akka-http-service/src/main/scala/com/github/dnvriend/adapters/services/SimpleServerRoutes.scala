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

package com.github.dnvriend.adapters.services

import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import com.github.dnvriend.api.{ Credentials, HelloApi }

import scala.concurrent.ExecutionContext

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
