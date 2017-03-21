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

package com.github.dnvriend.person.adapters.services

import java.util.UUID

import akka.NotUsed
import com.github.dnvriend.person._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class PersonImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends PersonApi {
  override def addPerson: ServiceCall[CreatePersonRequestMessage, UUID] = ServiceCall { (person: CreatePersonRequestMessage) =>
    val id: UUID = UUID.randomUUID()
    val idAsString: String = id.toString

    println(s"===> Trying to create a person for $person with id: $idAsString")

    val ref = persistentEntityRegistry.refFor[PersonEntity](idAsString)
    ref.ask(CreatePerson(id, person.name, person.age))
  }

  override def getPerson(id: UUID): ServiceCall[NotUsed, GetPersonResponseMessage] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[PersonEntity](id.toString)
    ref.ask(GetPersonRequest()).map(reply => GetPersonResponseMessage(reply.id, reply.name, reply.age))
  }

  override def personCreatedTopic: Topic[TopicMessagePersonCreated] = TopicProducer.singleStreamWithOffset { offset =>
    persistentEntityRegistry.eventStream(PersonCreated.Tag, offset)
      .map { ev =>
        val event = convertEvent(ev)
        println(s"Publishing event: '$event' with offset: '$offset'")
        (event, offset)
      }
  }

  private def convertEvent(event: EventStreamElement[PersonCreated]): TopicMessagePersonCreated = {
    event.event match {
      case PersonCreated(id, name, age) =>
        println(s"Converting event to publish on kafka: $event")
        TopicMessagePersonCreated(id, name, age)
    }
  }
}

class PersonEntity extends PersistentEntity {
  override type Command = PersonCommand[_]
  override type Event = PersonEvent
  override type State = Option[Person]
  override def initialState: Option[Person] = Option.empty[Person]

  override def behavior: Behavior = {
    // a persistent entity can define different behaviors for different states

    // state = empty person
    case None => Actions().onCommand[CreatePerson, UUID] {
      // command handler
      case (cmd: CreatePerson, ctx, state) =>
        println(s"===> Handling create person for: '$entityId', entityName: '$entityTypeName', state: $state")
        val (newState, event) = Person.handleCommand(state, cmd)
        ctx.thenPersist(event) { _ =>
          ctx.reply(cmd.id)
        }
    }.onEvent {
      case (event: PersonEvent, state) =>
        println(s"===> Rebuilding state for entityId: '$entityId', entityName: '$entityTypeName', state: $state")
        Person.handleEvent(state, event)
    }

    // state = some person
    case Some(Person(id, name, age)) => Actions().onReadOnlyCommand[GetPersonRequest, GetPersonResponse] {
      case (GetPersonRequest(), ctx, state) =>
        println(s"===> Getting Person State for: '$entityId', entityName: '$entityTypeName', state: $state")
        ctx.reply(GetPersonResponse(id, name, age))
    }
  }
}

// commands
sealed trait PersonCommand[R] extends ReplyType[R]
object CreatePerson {
  implicit val format = Json.format[CreatePerson]
}
final case class CreatePerson(id: UUID, name: String, age: Int) extends PersonCommand[UUID]
case class GetPersonRequest() extends PersonCommand[GetPersonResponse]

object GetPersonResponse {
  implicit val format = Json.format[GetPersonResponse]
}
final case class GetPersonResponse(id: UUID, name: String, age: Int)

// events
sealed trait PersonEvent

object PersonCreated {
  implicit val format = Json.format[PersonCreated]
  val Tag: AggregateEventTag[PersonCreated] = AggregateEventTag[PersonCreated]("person-created")
}

final case class PersonCreated(id: UUID, name: String, age: Int) extends PersonEvent with AggregateEvent[PersonCreated] {
  override def aggregateTag = PersonCreated.Tag
}

// state
object Person {
  implicit val format = Json.format[Person]

  def handleCommand(state: Option[Person], cmd: PersonCommand[_]): (Option[Person], PersonEvent) = cmd match {
    case CreatePerson(id, name, age) => (Option(Person(id, name, age)), PersonCreated(id, name, age))
  }

  def handleEvent(state: Option[Person], event: PersonEvent): Option[Person] = event match {
    case PersonCreated(id, name, age) => Option(Person(id, name, age))
  }
}
final case class Person(id: UUID, name: String, age: Int)