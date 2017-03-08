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

import java.net.URI
import javax.inject.Inject

import akka.NotUsed
import auth._
import com.lightbend.lagom.scaladsl.api.transport.{ Forbidden, RequestHeader }
import com.lightbend.lagom.scaladsl.api.{ ServiceCall, ServiceLocator }
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import lagom.components.kafka.KafkaProducer
import play.api.libs.json.Json

import scala.compat.Platform
import scala.concurrent.ExecutionContext

class HelloService @Inject() (serviceLocator: ServiceLocator, entityRegistry: PersistentEntityRegistry, producer: KafkaProducer)(implicit ec: ExecutionContext) extends HelloApi {

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

  override def addItem(orderId: Long): ServiceCall[Item, NotUsed] =
    ServiceCall { item =>
      println(s"Adding item: $item")
      NotUsed
    }
}