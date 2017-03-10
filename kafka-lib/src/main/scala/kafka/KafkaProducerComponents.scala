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

package kafka

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import com.sksamuel.avro4s.RecordFormat
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ Serializer, StringSerializer }
import play.api.libs.json.{ Format, Json }

import scala.concurrent.{ ExecutionContext, Future }

trait KafkaProducer {
  def produceJson[A: Format](topic: String, key: String, value: A): Future[NotUsed]
  def produceAvro[A](topic: String, key: String, value: A)(implicit recordFormat: RecordFormat[A]): Future[NotUsed]
}

class DefaultKafkaProducer(cb: CircuitBreaker, system: ActorSystem)(implicit mat: Materializer, ec: ExecutionContext) extends KafkaProducer {
  def producerSettings[K, V](keySerializer: Option[Serializer[K]], valueSerializer: Option[Serializer[V]]): ProducerSettings[K, V] =
    ProducerSettings(system, keySerializer, valueSerializer)
      .withBootstrapServers("localhost:9092")
      .withProperty("schema.registry.url", "http://localhost:8081")

  val stringSerializerSink: Sink[ProducerRecord[String, String], Future[Done]] =
    Producer.plainSink(producerSettings(Option(new StringSerializer), Option(new StringSerializer)))

  val avroSerializerSink: Sink[ProducerRecord[String, AnyRef], Future[Done]] =
    Producer.plainSink(producerSettings(None, None))

  def produce[K, V](producerRecord: ProducerRecord[K, V], sink: Sink[ProducerRecord[K, V], Future[Done]]): Future[Done] =
    cb.withCircuitBreaker(Source.single(producerRecord).runWith(sink))

  def produceJson[A: Format](topic: String, key: String, value: A): Future[NotUsed] = for {
    _ <- produce(new ProducerRecord[String, String](topic, key, Json.toJson(value).toString), stringSerializerSink)
  } yield NotUsed

  def produceAvro[A](topic: String, key: String, value: A)(implicit recordFormat: RecordFormat[A]): Future[NotUsed] = for {
    _ <- produce(new ProducerRecord[String, AnyRef](topic, key, recordFormat.to(value)), avroSerializerSink)
  } yield NotUsed
}
