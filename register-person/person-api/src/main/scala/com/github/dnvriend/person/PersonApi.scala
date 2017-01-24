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