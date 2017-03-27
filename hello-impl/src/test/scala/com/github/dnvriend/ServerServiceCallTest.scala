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
import play.api.http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

// see: https://www.lagomframework.com/documentation/1.3.x/scala/ServiceImplementation.html#Handling-headers
object ServerServiceCallTest {
  //
  // com.lightbend.lagom.scaladsl.api.transport
  // val Ok: ResponseHeader = ResponseHeaderImpl(200, MessageProtocol.empty, Map.empty)
  //

  //
  // com.lightbend.lagom.scaladsl.api.transport
  // val Default = RequestHeader(Method.GET, URI.create("/"), MessageProtocol.empty, Nil, None, Nil)
  //

  // ServiceCall has two invoke methods:
  // 1. invoke(): takes no arguments and is only invoked when the payload is 'NotUsed'
  // invoke(request: Request): invokes with the request payload (unmarshalled entity)
  val serviceCall: ServiceCall[NotUsed, String] = ServiceCall(_ => Future.successful("hello"))
  val serviceCall2: ServiceCall[String, String] = ServiceCall(msg => Future.successful(msg))

  // ServerServiceCall is a function from either:
  // 1. Payload => Future[Payload]
  // 2. (RequestHeader, Payload) => Future[(ResponseHeader, Payload)]
  val serviceCall3: ServerServiceCall[NotUsed, String] = ServerServiceCall { (requestHeader, request) =>
    Future.successful((ResponseHeader.Ok, "Hi there!"))
  }

  // ServerServiceCall.invokeWithHeaders needs a RequestHeader and a Payload
  val serviceCall4: ServerServiceCall[NotUsed, String] = ServerServiceCall { (requestHeader, request) =>
    serviceCall3.invokeWithHeaders(requestHeader, request).map {
      case (responseHeader, response) =>
        val newResponseHeader = responseHeader.withStatus(Status.USE_PROXY)
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

  // add an extra header to the response
  val serviceCall7: ServerServiceCall[NotUsed, NotUsed] = ServerServiceCall { (requestHeader, payload) =>
    serviceCall6.invokeWithHeaders(requestHeader, payload).map {
      case (responseHeader, _) =>
        (responseHeader.withHeader("MY_NEW_HEADER", "42").withStatus(Status.EXPECTATION_FAILED), payload)
    }
  }

  // our FutureOption monad transformer
  case class FutureOption[+A](future: Future[Option[A]]) extends AnyVal {
    def map[B](f: A => B)(implicit ec: ExecutionContext): FutureOption[B] =
      FutureOption(future.map(option => option.map(f)))
    def flatMap[B](f: A => FutureOption[B])(implicit ec: ExecutionContext): FutureOption[B] = {
      val flatMappedFuture = future.flatMap {
        case Some(a) => f(a).future
        case None    => Future.successful(None)
      }
      FutureOption(flatMappedFuture)
    }

    def filter(p: A => Boolean)(implicit ec: ExecutionContext): FutureOption[A] =
      withFilter(p)

    def withFilter(p: A => Boolean)(implicit ec: ExecutionContext): FutureOption[A] =
      FutureOption(future.map(_.filter(p)))
  }

  // composeAsync is a function from RequestHeader => Future[ServerServiceCall]
  val serviceCall8: ServerServiceCall[NotUsed, NotUsed] = ServerServiceCall.composeAsync { requestHeader =>
    val notAuthServerServiceCall: ServerServiceCall[NotUsed, NotUsed] = ServerServiceCall { (requestHeader, _) =>
      Future.successful((ResponseHeader.Ok.withStatus(Status.UNAUTHORIZED), NotUsed))
    }
    def getUserFromRequest(req: RequestHeader): Option[String] = for {
      p <- req.principal
      user <- Option(p.getName)
    } yield user

    def getUserAsync(name: String): Future[Option[String]] = {
      Future.successful(None)
    }

    // using a pattern called 'Monad Transformer' to deal with
    // composing multiple effects, here the Future[Option]
    val result: Future[Option[String]] = (for {
      userName: String <- FutureOption(Future.successful(getUserFromRequest(requestHeader)))
      user: String <- FutureOption(getUserAsync(userName))
    } yield user).future

    result.map { maybeUser =>
      maybeUser.map(_ => serviceCall7).getOrElse(notAuthServerServiceCall)
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
    resp.status shouldBe Status.OK
  }

  it should "serviceCall4: alter the response header" in {
    val (resp, body) = serviceCall4.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe Status.USE_PROXY
  }

  it should "serviceCall5: invoke another serverServiceCall by using compose" in {
    val (resp, body) = serviceCall5.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe Status.USE_PROXY
  }

  it should "serviceCall6: invoke another serverServiceCall by using compose with nested ServerServiceCall" in {
    val (resp, body) = serviceCall6.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe "Hi there foobar!"
    resp.status shouldBe Status.USE_PROXY
  }

  it should "serviceCall7: add an extra header and alter the response status" in {
    val (resp, body) = serviceCall7.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe NotUsed
    resp.status shouldBe Status.EXPECTATION_FAILED
    resp.getHeader("MY_NEW_HEADER").value shouldBe "42"
  }

  it should "return an unauthorized without throwing Forbidden" in {
    val (resp, body) = serviceCall8.invokeWithHeaders(RequestHeader.Default, NotUsed).futureValue
    body shouldBe NotUsed
    resp.status shouldBe Status.UNAUTHORIZED
  }
}
