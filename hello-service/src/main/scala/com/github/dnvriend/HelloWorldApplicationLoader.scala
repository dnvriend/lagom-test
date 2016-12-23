package com.github.dnvriend

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.api.ServiceLocator

class HelloWorldApplicationLoader extends LagomApplicationLoader {
  override def loadDevMode(context: LagomApplicationContext) =
    new HelloWorldApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) =
    new HelloWorldApplication(context) {
      override def serviceLocator = ServiceLocator.NoServiceLocator
    }
}
