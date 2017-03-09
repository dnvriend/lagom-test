package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, _ }

object SimpleService {
  final val Name = "simple-service"
}

trait SimpleService extends Service {
  def sayHello(name: String): ServiceCall[NotUsed, String]

  override def descriptor: Descriptor =
    named(SimpleService.Name).withCalls(
      pathCall("/api/simple/:name", sayHello _)
    ).withAutoAcl(true)
}
