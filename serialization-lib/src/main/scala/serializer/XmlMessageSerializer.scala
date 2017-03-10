/*
 * Copyright 2016 Dennis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package serializer

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.scaladsl.api.deser.{ MessageSerializer, StrictMessageSerializer }
import com.lightbend.lagom.scaladsl.api.transport._
import play.api.libs.json.Format

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.xml.{ Elem, XML }

trait ElemFormat[Message] {
  def toElem(msg: Message): Elem
  def fromElem(xml: Elem): Message
}

object XmlMessageSerializer {
  final val ContentTypeApplicationXml = "application/xml"
  final val ContentTypeTextXml = "text/xml"
  private val `application/xml` = MessageProtocol(Some(ContentTypeApplicationXml), Some("utf-8"), None)
  private val `text/xml` = MessageProtocol(Some(ContentTypeTextXml), Some("utf-8"), None)

  implicit def elemFormatMessageSerializer[Message: ClassTag](implicit xmlMessageSerializer: MessageSerializer[Elem, ByteString], elemFormat: ElemFormat[Message], jsFormat: Format[Message] = null): StrictMessageSerializer[Message] = new StrictMessageSerializer[Message] {
    val messageClass: Class[_] = implicitly[ClassTag[Message]].runtimeClass
    val messageClassFQN: String = messageClass.getName
    val messageClassSimpleName: String = messageClass.getSimpleName

    private class ElemFormatSerializer(elemValueSerializer: NegotiatedSerializer[Elem, ByteString]) extends NegotiatedSerializer[Message, ByteString] {
      override def protocol: MessageProtocol = elemValueSerializer.protocol
      override def serialize(message: Message): ByteString = try {
        val xml: Elem = elemFormat.toElem(message)
        elemValueSerializer.serialize(xml)
      } catch {
        case NonFatal(e) => throw DeserializationException(e)
      }
    }

    private class ElemFormatDeserializer(xmlDeserializer: NegotiatedDeserializer[Elem, ByteString]) extends NegotiatedDeserializer[Message, ByteString] {
      override def deserialize(wire: ByteString): Message = {
        val xml = xmlDeserializer.deserialize(wire)
        elemFormat.fromElem(xml)
      }
    }

    override def acceptResponseProtocols: Seq[MessageProtocol] = xmlMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Message, ByteString] =
      new ElemFormatDeserializer(xmlMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: Seq[MessageProtocol]): NegotiatedSerializer[Message, ByteString] = {
      val contentTypes = acceptedMessageProtocols.flatMap(_.contentType)
      contentTypes match {
        case _ if contentTypes.contains("application/json") && Option(jsFormat).isDefined =>
          MessageSerializer.jsValueFormatMessageSerializer[Message].serializerForResponse(acceptedMessageProtocols)
        case _ if contentTypes.contains("application/json") && Option(jsFormat).isEmpty =>
          throw DeserializationException(s"No Json format defined for class '$messageClassFQN', please add an implicit val format: Format[$messageClassSimpleName] = Json.format to the companion object of '$messageClassFQN' or choose another content-type by changing the value of the Accept header of your request.")
        case _ =>
          new ElemFormatSerializer(xmlMessageSerializer.serializerForResponse(acceptedMessageProtocols))
      }
    }

    override def serializerForRequest: NegotiatedSerializer[Message, ByteString] =
      new ElemFormatSerializer(xmlMessageSerializer.serializerForRequest)
  }

  implicit val XmlMessageSerializer: StrictMessageSerializer[Elem] = new StrictMessageSerializer[Elem] {
    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(`application/xml`, `text/xml`)

    private class XmlSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[Elem, ByteString] {
      override def serialize(s: Elem) = ByteString.fromString(s.toString, protocol.charset.getOrElse("utf-8"))
    }

    private class XmlDeserializer(charset: String) extends NegotiatedDeserializer[Elem, ByteString] {
      override def deserialize(wire: ByteString) = XML.loadString(wire.decodeString(charset))
    }

    override def serializerForRequest: NegotiatedSerializer[Elem, ByteString] = new XmlSerializer(`application/xml`)

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Elem, ByteString] = protocol.contentType match {
      case Some(ContentTypeApplicationXml) =>
        new XmlDeserializer(protocol.charset.getOrElse("utf-8"))
      case Some(ContentTypeTextXml) =>
        new XmlDeserializer(protocol.charset.getOrElse("utf-8"))
      case _ =>
        throw UnsupportedMediaType(protocol, `application/xml`)
    }

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Elem, ByteString] = {
      new XmlSerializer(`application/xml`)
    }
  }
}
