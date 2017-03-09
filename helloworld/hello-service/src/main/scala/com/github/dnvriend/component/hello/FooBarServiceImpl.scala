package com.github.dnvriend.component.hello
import java.util.UUID

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.apache.kafka.common.serialization.{ Deserializer, Serializer }
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

class FooBarServiceImpl(helloApi: HelloApi, entityRegistry: PersistentEntityRegistry)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends FooBarService {
  println("===> [FooBarServiceImpl] - Launching service")

  def randomId: String = UUID.randomUUID.toString

  def consumerSettings[K, V](system: ActorSystem, keySerializer: Option[Deserializer[K]], valueSerializer: Option[Deserializer[V]]) = {
    ConsumerSettings(system, keySerializer, valueSerializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId(randomId)
      .withClientId(randomId)
  }

  def producerSettings[K, V](system: ActorSystem, keySerializer: Option[Serializer[K]], valueSerializer: Option[Serializer[V]]): ProducerSettings[K, V] =
    ProducerSettings(system, keySerializer, valueSerializer)
      .withBootstrapServers("localhost:9092")

  def mapToEvent(msg: CommittableMessage[String, String]): (CommittableMessage[String, String], Option[FooBarEvent]) = {
    (msg, Json.parse(msg.record.value()).asOpt[FooBarEvent])
  }

  val future: Future[Done] = Consumer.committableSource(consumerSettings[String, String](system, None, None), Subscriptions.topics("FooBarTopic"))
    .map(mapToEvent)
    .map { msg => println(s"==> [FooBarServiceImpl] - Read from Topic: $msg"); msg }
    .mapAsync(1) {
      case (msg, Some(event @ FooDone(id, _))) => entityRegistry.refFor[FooBarEntity](id).ask(FooBarEntity.HandleFooDone(event)).map(_ => msg)
      case (msg, Some(event @ BarDone(id, _))) => entityRegistry.refFor[FooBarEntity](id).ask(FooBarEntity.HandleBarDone(event)).map(_ => msg)
      case (msg, _)                            => Future.successful(msg)
    }
    .map(_.committableOffset.commitScaladsl())
    .runWith(Sink.ignore)

  future.map { _ => println("===> [FooBarServiceImpl] - Completed consomer") }.recover { case t: Throwable => t.printStackTrace() }

  override def callFoobar: ServiceCall[NotUsed, String] = ServiceCall { _ => randomId }
}
