package com.github.dnvriend.component

import scala.concurrent.Future
import scala.language.implicitConversions

package object hello {
  implicit def toFuture[A](a: A): Future[A] = Future.successful(a)
}
