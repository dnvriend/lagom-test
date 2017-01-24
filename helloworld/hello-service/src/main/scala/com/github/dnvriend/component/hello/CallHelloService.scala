package com.github.dnvriend.component.hello
import javax.inject.Inject

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext

class CallHelloService @Inject() (hello: HelloApi)(implicit ec: ExecutionContext) extends CallHelloApi {
  override def callHello(username: String): ServiceCall[NotUsed, String] =
    ServiceCall { _ =>
      hello.sayHelloWithName(username).invoke().map { response =>
        val msg = s"Hello service said: $response"
        println(msg)
        msg
      }
    }
}
