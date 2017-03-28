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
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, ServiceCall }
import com.lightbend.lagom.scaladsl.api.Service._

trait AclService extends Service {
  def access: ServiceCall[NotUsed, String]
  def noaccess: ServiceCall[NotUsed, String]

  // - by default no services are accessible from the Service Discovery Service (port 9000)
  //   by default the acl = false
  // - Services may publish ACLs in a Service Gateway to list what endpoints are provided by the service by
  //   setting withAutoAcl(true/false) on a per service basis
  // - The outer .withAutoAcl(true/false) has no influence on the inner .withAutoAcl so the noaccess is still not accessible
  // - THe outer .withAutoAcl(true/false) is convenient because you can set acl for all services
  // - The autoAcl instructs Lagom to generate Service ACLs from each call’s pathPattern.
  override def descriptor: Descriptor = {
    named("acl-service").withCalls(
      pathCall("/api/acl/access", access).withAutoAcl(true),
      pathCall("/api/acl/noaccess", noaccess).withAutoAcl(false)
    ).withAutoAcl(true)
  }
}
