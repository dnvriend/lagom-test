package com.github.dnvriend

import com.lightbend.lagom.scaladsl.playjson.{ SerializerRegistry, Serializers }

import scala.collection.immutable.Seq

class HelloWorldSerializerRegistry extends SerializerRegistry {
  override def serializers: Seq[Serializers[_]] = Seq.empty
}