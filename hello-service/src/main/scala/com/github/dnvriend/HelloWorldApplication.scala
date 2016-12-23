package com.github.dnvriend

import com.github.dnvriend.component.hello.{ HelloApi, HelloService }
import com.lightbend.lagom.scaladsl.server._
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._

abstract class HelloWorldApplication(context: LagomApplicationContext) extends LagomApplication(context) with AhcWSComponents {
  override lazy val lagomServer: LagomServer = LagomServer.forServices(
    bindService[HelloApi].to(wire[HelloService])
  )
}
