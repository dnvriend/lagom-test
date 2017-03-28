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

import com.github.dnvriend.api.{ AclService, CallHelloApi, HelloApi }
import com.github.dnvriend.application.HelloWorldApplication
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents

import scala.collection.immutable

class HelloWorldApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new HelloWorldApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new HelloWorldApplication(context) with LagomDevModeComponents

  // to let ConductR discover the Lagom service API
  // A third method, describeServices, is optional, but may be used by tooling,
  // for example by ConductR, to discover what service APIs are offered by this service.
  // The meta data read from here may in turn be used to configure service gateways and other components.
  override def describeServices: immutable.Seq[Descriptor] = List(
    readDescriptor[HelloApi],
    readDescriptor[AclService]
  //    readDescriptor[CallHelloApi]
  )
}
