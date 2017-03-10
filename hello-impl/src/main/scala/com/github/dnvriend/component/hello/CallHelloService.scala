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

package com.github.dnvriend.component.hello
import javax.inject.Inject

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import play.api.Configuration

import scala.concurrent.ExecutionContext

class CallHelloService @Inject() (hello: HelloApi, config: Configuration)(implicit ec: ExecutionContext) extends CallHelloApi {
  override def callHello(username: String): ServiceCall[NotUsed, String] =
    ServiceCall { _ =>
      hello.sayHelloWithName(username)
        //        .handleRequestHeader(_.withAcceptedResponseProtocols(List(MessageProtocol.fromContentTypeHeader(Option("application/xml")))))
        .invoke().map { (response: String) =>
          val msg = s"Hello service said: $response"
          println(msg)
          msg
        }
    }
}
