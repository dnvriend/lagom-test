package com.github.dnvriend.person

import javax.inject.Inject

import akka.{ Done, NotUsed }
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.{ ExecutionContext, Future }

class PersonCreatedCounter @Inject() (personApi: PersonApi)(implicit ec: ExecutionContext) extends PersonCreatedCounterApi {
  // very ugly...
  var count = 0L

  override def getCount: ServiceCall[NotUsed, Long] = ServiceCall { _ =>
    Future.successful(count)
  }

  personApi.personCreatedTopic.subscribe.atLeastOnce {
    Flow[TopicMessagePersonCreated].map { msg =>
      println(s"==> Subscriber received: $msg")
      count += 1
      Done
    }
  }
}
