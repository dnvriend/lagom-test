package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class HelloService extends HelloApi {
  //  A service call for an entity. A service call has a request and a response entity.
  override def sayHello(userName: String): ServiceCall[NotUsed, String] = ServiceCall { name =>
    Future.successful(s"Hello $userName!")
  }
}
