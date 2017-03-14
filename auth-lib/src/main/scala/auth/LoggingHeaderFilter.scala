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

import com.lightbend.lagom.scaladsl.api.transport.{ HeaderFilter, RequestHeader, ResponseHeader }

object LoggingHeaderFilter extends HeaderFilter {
  // Request -> LoggingFilter -> NextFilter or Service
  override def transformServerRequest(request: RequestHeader): RequestHeader = {
    val result: String = request.principal.map(_.toString).getOrElse("no-principal-found")
    println(s"LoggingHeaderFilter.transformServerRequest: Principal: '$result'")
    request
  }

  // Service or Previous Filter -> LoggingFilter -> Response
  override def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = {
    println(s"LoggingHeaderFilter.transformServerResponse")
    response
  }

  override def transformClientRequest(request: RequestHeader): RequestHeader = {
    println(s"transformClientRequest: '$request'")
    request
  }

  override def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = {
    println(s"transformClientResponse: '$request'")
    response
  }
}
