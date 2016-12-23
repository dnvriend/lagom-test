package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.Service._

// see: http://www.lagomframework.com/documentation/1.3.x/scala/ServiceDescriptors.html
trait HelloApi extends Service {
  //  A service call for an entity. A service call has a request and a response entity.
  def sayHello(userName: String): ServiceCall[NotUsed, String]

  override def descriptor: Descriptor = {
    named("lagom-test").withCalls(
      pathCall("/api/hello/:id", sayHello _)
    ).withAutoAcl(true)
  }
}
