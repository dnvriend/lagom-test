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

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, _ }

object SimpleService {
  final val Name = "simple-service"
}

trait SimpleService extends Service {
  def sayHello(name: String): ServiceCall[NotUsed, String]

  override def descriptor: Descriptor =
    named(SimpleService.Name).withCalls(
      pathCall("/api/simple/:name", sayHello _)
    ).withAutoAcl(true)
}
