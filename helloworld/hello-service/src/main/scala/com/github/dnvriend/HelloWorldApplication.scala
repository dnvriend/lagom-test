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

import com.github.dnvriend.component.hello.{ CallHelloApi, CallHelloService, HelloApi, HelloService }
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

abstract class HelloWorldApplication(context: LagomApplicationContext) extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // The service descriptor contains everything Lagom needs to know about how to invoke a service,
  // consequently, Lagom is able to implement service descriptor interface for you.
  //
  // The first thing necessary to consume a service is to create an implementation of it.
  // Lagom provides a macro to do this on the ServiceClient class, called 'implement'.
  //
  // The ServiceClient is provided by the LagomServiceClientComponents,
  // which is already implemented by LagomApplication, so to create a service
  // client from a Lagom application, you just have to do the following

  val helloServiceClient: HelloApi = serviceClient.implement[HelloApi]

  // Bind the services that this server provides
  override lazy val lagomServer: LagomServer = {
    LagomServer.forServices(
      bindService[HelloApi].to(wire[HelloService]),
      bindService[CallHelloApi].to(wire[CallHelloService])
    )
  }

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = HelloWorldSerializerRegistry
}
