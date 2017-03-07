package com.github.dnvriend.person

import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import play.api.libs.ws.ahc.AhcWSComponents
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import com.softwaremill.macwire._

import scala.collection.immutable.Seq

class LagomscalaLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomscalaApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomscalaApplication(context) with LagomDevModeComponents

  // to let ConductR discover the Lagom service API
  override def describeServices = List(
    readDescriptor[PersonApi]
  )
}

abstract class LagomscalaApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // personClient is needed by PersonCreatedCounter
  lazy val personClient: PersonApi = serviceClient.implement[PersonApi]

  override lazy val lagomServer: LagomServer = LagomServer.forServices(
    bindService[PersonApi].to(wire[PersonImpl]),
    bindService[PersonCreatedCounterApi].to(wire[PersonCreatedCounter])
  )

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = new JsonSerializerRegistry {
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer[CreatePerson],
      JsonSerializer[GetPersonResponse],
      JsonSerializer[PersonCreated],
      JsonSerializer[Person]
    )
  }

  //   Register the lagom-scala persistent entity
  persistentEntityRegistry.register(wire[PersonEntity])
}

