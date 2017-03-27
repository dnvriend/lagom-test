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

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{ RequestHeader, ResponseHeader }
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ServerServiceCallTest {
  //
  // com.lightbend.lagom.scaladsl.api.transport
  // val Ok: ResponseHeader = ResponseHeaderImpl(200, MessageProtocol.empty, Map.empty)
  //

  //
  // com.lightbend.lagom.scaladsl.api.transport
  // val Default = RequestHeader(Method.GET, URI.create("/"), MessageProtocol.empty, Nil, None, Nil)
  //

  // invoke()
  // invoke(request: Request)
  val serviceCall: ServiceCall[NotUsed, String] = ServiceCall(_ => Future.successful("hello"))
  val serviceCall2: ServiceCall[String, String] = ServiceCall(msg => Future.successful(msg))

  // ServerServiceCall is a function from either:
  // 1. Payload => Future[Payload]
  // 2. (RequestHeader, Payload) => (ResponseHeader, Payload)
  val serviceCall3: ServerServiceCall[NotUsed, String] = ServerServiceCall { (requestHeader, request) =>
    Future.successful((ResponseHeader.Ok, "Hi there!"))
  }

  // ServerServiceCall.invokeWithHeaders needs a RequestHeader and a Payload
  val serviceCall4: ServerServiceCall[NotUsed, String] = ServerServiceCall { (requestHeader, request) =>
    serviceCall3.invokeWithHeaders(requestHeader, request).map {
      case (responseHeader, response) =>
        val newResponseHeader = responseHeader.withStatus(321)
        (newResponseHeader, response.dropRight(1) + " foobar!")
    }
  }

  // compose only gives the opportunity to handle the requestHeader, not the payload
  // its a function from RequestHeader => ServerServiceCall
  // so you can alter everything in the RequestHeader...
  // but you cannot directly invoke the ServerServiceCall.invokeWithHeaders
  val serviceCall5: ServerServiceCall[NotUsed, String] = ServerServiceCall.compose { (requestHeader: RequestHeader) =>
    serviceCall4
  }

  // be able to return a ServerServiceCall that exposes the RequestHeader and Payload that makes it possible to
  // invokeWithHeaders
  val serviceCall6: ServerServiceCall[NotUsed, String] = ServerServiceCall.compose { (requestHeader: RequestHeader) =>
    // so ServerServiceCall.compose focuses on RequestHeader => ServerServiceCall
    ServerServiceCall { (reqHeader, req) =>
      serviceCall4.invokeWithHeaders(reqHeader, req)
    }
  }
}

class ServerServiceCallTest extends SimpleTestSpec {
  import ServerServiceCallTest._
  it should "be invoked and return 'hello'" in {
    serviceCall.invoke().futureValue shouldBe "hello"
  }

  it should "be invoked with a payload and return the payload" in {
    serviceCall2.invoke("Hi there!").futureValue shouldBe "Hi there!"
  }

  it should "use a ServerServiceCall with RequestHeader and no payload" in {
    val (resp, body) = serviceCall3.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there!"
    resp.status shouldBe 200
  }

  it should "serviceCall4: alter the response header" in {
    val (resp, body) = serviceCall4.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe 321
  }

  it should "serviceCall5: invoke another serverServiceCall by using compose" in {
    val (resp, body) = serviceCall5.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe 321
  }

  it should "serviceCall6: invoke another serverServiceCall by using compose with nested ServerServiceCall" in {
    val (resp, body) = serviceCall6.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe 321
  }
}
