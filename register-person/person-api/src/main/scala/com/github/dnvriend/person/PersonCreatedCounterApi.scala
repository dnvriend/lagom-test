package com.github.dnvriend.person

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.Service._

trait PersonCreatedCounterApi extends Service {

  def getCount: ServiceCall[NotUsed, Long]

  override def descriptor: Descriptor =
    named("personCreatedCounter").withCalls(
      // http :9000/api/count
      pathCall("/api/count", getCount _)
    ).withAutoAcl(true)
}
