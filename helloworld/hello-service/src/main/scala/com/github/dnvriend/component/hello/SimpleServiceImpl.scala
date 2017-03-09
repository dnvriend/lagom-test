package com.github.dnvriend.component.hello
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

class SimpleServiceImpl extends SimpleService {
  override def sayHello(name: String): ServiceCall[NotUsed, String] =
    ServiceCall(_ => "Hello")
}
