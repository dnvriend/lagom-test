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

package auth

import com.lightbend.lagom.scaladsl.api.transport.{ Forbidden, HeaderFilter, RequestHeader, ResponseHeader }
import com.sun.security.auth.UserPrincipal

object JwtHeaderFilter extends HeaderFilter {
  override def transformServerRequest(requestHeader: RequestHeader): RequestHeader = {
    println("JwtHeaderFilter.transformServerRequest")
    val creds: JwtCredentials = AuthenticationServiceCall.getJwtCredentials(requestHeader).getOrElse(throw Forbidden(s"User must be authenticated to access this service call"))
    requestHeader.withPrincipal(new UserPrincipal(creds.user))
  }

  override def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = {
    println("JwtHeaderFilter.transformServerResponse")
    response
  }

  override def transformClientRequest(request: RequestHeader): RequestHeader = request

  override def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = response
}
