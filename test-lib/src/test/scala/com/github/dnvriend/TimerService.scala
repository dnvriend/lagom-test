package com.github.dnvriend

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import com.github.dnvriend.TimerActor.ReplyContext

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object TimerService {
  def delay[A](duration: FiniteDuration)(block: => A)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = 1.hour): Future[A] = {
    (system.actorOf(Props(classOf[TimerActor], duration, false, ec)) ? "")
      .map(_ => block)
  }

  def delayFailure[A](duration: FiniteDuration)(block: => A)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = 1.hour): Future[A] = {
    (system.actorOf(Props(classOf[TimerActor], duration, true, ec)) ? "")
      .map(_ => block)
  }
}

object TimerActor {
  case class ReplyContext(ref: ActorRef)
}
class TimerActor(delay: FiniteDuration, failedDelay: Boolean = false)(implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case ReplyContext(ref) if !failedDelay =>
      ref ! ""
      context.stop(self)
    case ReplyContext(ref) if failedDelay =>
      ref ! akka.actor.Status.Failure(new RuntimeException("System timeout"))
      context.stop(self)
    case _ =>
      context.system.scheduler.scheduleOnce(delay, self, ReplyContext(sender()))
  }
}