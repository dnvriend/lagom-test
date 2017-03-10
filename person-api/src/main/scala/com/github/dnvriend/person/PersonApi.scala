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

package com.github.dnvriend.person

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method
import play.api.libs.json.Json

object CreatePersonRequestMessage {
  implicit val format = Json.format[CreatePersonRequestMessage]
}

case class CreatePersonRequestMessage(name: String, age: Int)

object GetPersonResponseMessage {
  implicit val format = Json.format[GetPersonResponseMessage]
}

final case class GetPersonResponseMessage(id: UUID, name: String, age: Int)

object TopicMessagePersonCreated {
  implicit val format = Json.format[TopicMessagePersonCreated]
}
final case class TopicMessagePersonCreated(id: UUID, name: String, age: Int)

object PersonApi {
  final val TopicName: String = "person-created"
}

trait PersonApi extends Service {
  def addPerson: ServiceCall[CreatePersonRequestMessage, UUID]
  def getPerson(id: UUID): ServiceCall[NotUsed, GetPersonResponseMessage]

  def personCreatedTopic: Topic[TopicMessagePersonCreated]

  override def descriptor: Descriptor = {
    named("person-api")
      .withCalls(
        // http :9000/api/person
        restCall(Method.GET, "/api/person/:id", getPerson _),
        // http post :9000/api/person name=foo age:=50
        restCall(Method.POST, "/api/person", addPerson _)
      ).withTopics(
          topic(PersonApi.TopicName, personCreatedTopic)
        ).withAutoAcl(true)
  }
}