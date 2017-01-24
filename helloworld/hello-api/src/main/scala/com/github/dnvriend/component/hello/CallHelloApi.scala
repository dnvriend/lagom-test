package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.Service._

trait CallHelloApi extends Service {
  def callHello(username: String): ServiceCall[NotUsed, String]

  override def descriptor: Descriptor =
    named("call-hello-api").withCalls(
      pathCall("/api/call-hello/:name", callHello _)
    ).withAutoAcl(true)
}
