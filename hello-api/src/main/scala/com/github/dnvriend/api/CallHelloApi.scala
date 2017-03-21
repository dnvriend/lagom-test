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

package com.github.dnvriend.api

import akka.NotUsed
import auth.{ JwtHeaderFilter, LoggingHeaderFilter }
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.HeaderFilter

trait CallHelloApi extends Service {
  def callHello(username: String): ServiceCall[NotUsed, String]

  override def descriptor: Descriptor =
    named("call-hello-api")
      .withHeaderFilter(HeaderFilter.composite(
        LoggingHeaderFilter,
        JwtHeaderFilter
      ))
      .withCalls(
        pathCall("/api/call-hello/:name", callHello _)
      ).withAutoAcl(true)
}
