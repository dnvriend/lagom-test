package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json._

object FooBarEvent {
  implicit val reads: Reads[FooBarEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "FooDone" => implicitly[Reads[FooDone]].map(identity)
      case "BarDone" => implicitly[Reads[BarDone]].map(identity)
      case other     => Reads(_ => JsError(s"Unknown event type $other"))
    }
  }
  implicit val writes: Writes[FooBarEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: FooDone => (Json.toJson(m)(FooDone.format), "FooDone")
      case m: BarDone => (Json.toJson(m)(BarDone.format), "BarDone")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}

trait FooBarEvent {
  def id: String
}
case class FooDone(id: String, msg: String) extends FooBarEvent
object FooDone {
  implicit val format: Format[FooDone] = Json.format
}
case class BarDone(id: String, msg: String) extends FooBarEvent
object BarDone {
  implicit val format: Format[BarDone] = Json.format
}

object FooBarService {
  final val TOPIC_NAME = "FooBarTopic"
}

trait FooBarService extends Service {
  def callFoobar: ServiceCall[NotUsed, String]
  override def descriptor: Descriptor =
    named("bar-service").withCalls(
      pathCall("/api/foobar", callFoobar)
    ).withAutoAcl(true)
}
