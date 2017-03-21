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

package com.github.dnvriend.adapters.services

import java.net.URI

import akka.util.Timeout
import akka.{ Done, NotUsed }
import auth._
import com.github.dnvriend.adapters.services.FooBarEntity._
import com.github.dnvriend.api._
import com.lightbend.lagom.scaladsl.api.{ ServiceCall, ServiceLocator }
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity, PersistentEntityRegistry }
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import kafka.KafkaProducer
import play.api.libs.json.{ Format, Json }
import play.api.libs.ws.WSClient

import scala.compat.Platform
import scala.concurrent.{ ExecutionContext, Future }
import scala.xml.NodeSeq

class HelloService(wsClient: WSClient, serviceLocator: ServiceLocator, entityRegistry: PersistentEntityRegistry, producer: KafkaProducer)(implicit ec: ExecutionContext, timeout: Timeout) extends HelloApi {

  val authRepo = new AuthRepository {
    override def getAuth(name: String): Option[Auth] =
      Option(name).find(_ == "foo").map(_ => Auth("foo", "bar"))
  }

  def handleMaybeUri(maybeUri: Option[URI]): Unit = maybeUri match {
    case Some(uri) => println("Got uri: " + uri)
    case _         => println("Got no uri")
  }
  serviceLocator.locate("hello-api").map(handleMaybeUri).recover { case t: Throwable => t.printStackTrace() }
  //  A service call for an entity. A service call has a request and a response entity.
  override def sayHelloWithName(userName: String): ServiceCall[NotUsed, String] =
    ServiceCall(_ => s"Hello $userName!")

  override def sayHelloWithNameAndAge(userName: String, age: Int): ServiceCall[NotUsed, String] =
    ServiceCall(_ => s"Hello $userName, you are $age old.")

  override def sayHelloWithNameAndAgeAndPageNoAndPageSize(userName: String, age: Int, pageNo: Long, pageSize: Int): ServiceCall[NotUsed, String] =
    ServiceCall(_ => s"Hello $userName, you are $age old, pageNo=$pageNo and pageSize=$pageSize")

  // A ServiceCall is an abstraction of a service call for an entity.
  override def sayHello: ServiceCall[NotUsed, String] = {
    LoggingServiceCall.logged(ServerServiceCall(_ => "Hello World!"))
  }

  override def sayHelloAuth: ServiceCall[NotUsed, String] = {
    AuthenticationServiceCall.basic(authRepo)(auth => ServerServiceCall(_ => s"Hello World! $auth"))
  }

  override def sayHelloAuthJwt: ServiceCall[NotUsed, String] = {
    AuthenticationServiceCall.jwt(authRepo)(auth => ServerServiceCall(_ => s"Hello World! $auth"))
  }

  override def createToken: ServiceCall[Credentials, String] = ServiceCall { creds =>
    AuthenticationServiceCall.createToken(Json.toJson(creds).toString)
  }

  override def produceMessage(msg: String, key: String): ServiceCall[NotUsed, NotUsed] = ServiceCall { _ =>
    producer.produceJson("hello", key, Message(msg, Platform.currentTime))
  }

  override def doFooBar(msg: String, key: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    println(s"===> [HelloServiceImpl] - doFooBar($msg, $key)")
    for {
      _ <- Future.sequence(List(producer.produceJson("FooBarTopic", key, DoFoo(msg, key)), producer.produceJson("FooBarTopic", key, DoBar(msg, key))))
      response <- entityRegistry.refFor[FooBarEntity](key).ask(GetStateRequest)
    } yield response.msg
  }

  override def addItem(orderId: Long): ServiceCall[Item, NotUsed] =
    ServiceCall { item =>
      println(s"Adding item: $item")
      NotUsed
    }

  override def respondWithXml: ServiceCall[NotUsed, NodeSeq] = ServiceCall { _ =>
    val xml =
      <people>
        <person>
          <name>FooBar</name>
          <age>42</age>
        </person>
      </people>
    Future.successful(xml)
  }

  override def postSomeXml: ServiceCall[NodeSeq, NodeSeq] = ServiceCall { entity =>
    println(s"==> Got: $entity")
    val xml =
      <envelope>
        <message>Hello World!</message>
      </envelope>
    Future.successful(xml)
  }

  override def respondWithXmlHello: ServiceCall[NotUsed, Hello] = ServiceCall { _ =>
    Future.successful(Hello("Hi there!"))
  }

  override def proxyPing: ServiceCall[NotUsed, String] = ServiceCall { _ =>
    serviceLocator.locate("akka-http-service").flatMap { uri =>
      val base = uri.get.toString
      wsClient.url(s"$base/api/ping").get().map(_.body)
    }
  }
}

object FooBarEntity {
  // the commands
  trait FooBarCmd[R] extends ReplyType[R]
  case object GetStateRequest extends FooBarCmd[GetStateResponse]
  case class GetStateResponse(msg: String)
  case class HandleFooDone(msg: FooDone) extends FooBarCmd[Done]
  case class HandleBarDone(msg: BarDone) extends FooBarCmd[Done]

  // the events
  trait FooBarStateEvent
  case class FooEventReceived(foo: FooDone) extends FooBarStateEvent with AggregateEvent[FooEventReceived] {
    override def aggregateTag = AggregateEventTag[FooEventReceived]("all")
  }
  object FooEventReceived {
    implicit val format: Format[FooEventReceived] = Json.format
  }
  case class BarEventReceived(foo: BarDone) extends FooBarStateEvent with AggregateEvent[BarEventReceived] {
    override def aggregateTag = AggregateEventTag[BarEventReceived]("all")
  }
  object BarEventReceived {
    implicit val format: Format[BarEventReceived] = Json.format
  }
}

class FooBarEntity extends PersistentEntity {
  import FooBarEntity._
  override type Command = FooBarCmd[_]
  override type Event = FooBarStateEvent
  override type State = FooBarState

  override def initialState: FooBarState = FooBarState.empty

  override def behavior: Behavior = {
    case _ => Actions()
      .onCommand[HandleFooDone, Done] {
        case (cmd: HandleFooDone, ctx, state) =>
          val (_, event) = FooBarState.handleCommand(state, cmd)
          ctx.thenPersist(event) { _ =>
            println("===> Handling FooDone")
            ctx.reply(Done)
          }
      }.onCommand[HandleBarDone, Done] {
        case (cmd: HandleBarDone, ctx, state) =>
          val (_, event) = FooBarState.handleCommand(state, cmd)
          ctx.thenPersist(event) { _ =>
            println("===> Handling BarDone")
            ctx.reply(Done)
          }
      }.onEvent {
        case (event: FooBarStateEvent, state) =>
          FooBarState.handleEvent(state, event)
      }.onReadOnlyCommand[GetStateRequest.type, GetStateResponse] {
        case (GetStateRequest, ctx, state) if state.isComplete =>
          val response = GetStateResponse(state.msg)
          println(s"===> Returning state: $response")
          ctx.reply(response)
      }
  }
}

case class FooBarState(foo: Option[FooDone], bar: Option[BarDone]) {
  def isComplete: Boolean = foo.isDefined && bar.isDefined
  def msg: String = s"foo=$foo, bar=$bar"
}

object FooBarState {
  val empty = FooBarState(None, None)
  implicit val format: Format[FooBarState] = Json.format

  def time: Long = Platform.currentTime

  def handleCommand(state: FooBarState, cmd: FooBarCmd[_]): (FooBarState, FooBarStateEvent) = cmd match {
    case HandleFooDone(msg) =>
      println(s"==> Handling command: $state, $cmd")
      (state.copy(foo = Option(msg)), FooEventReceived(msg))
    case HandleBarDone(msg) =>
      println(s"==> Handling command: $state, $cmd")
      (state.copy(bar = Option(msg)), BarEventReceived(msg))
  }

  def handleEvent(state: FooBarState, event: FooBarStateEvent): FooBarState = event match {
    case FooEventReceived(msg) =>
      println(s"==> Handling event: $state, $event")
      state.copy(foo = Option(msg))
    case BarEventReceived(msg) =>
      println(s"==> Handling event: $state, $event")
      state.copy(bar = Option(msg))
  }
}