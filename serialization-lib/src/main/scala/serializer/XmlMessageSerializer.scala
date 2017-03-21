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
import org.slf4j.{ Logger, LoggerFactory }
import play.api.libs.json.Format

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.xml.{ NodeSeq, XML }

object XmlMessageSerializer {
  val log: Logger = LoggerFactory.getLogger(getClass)
  final val ContentTypeApplicationXml = "application/xml"
  final val ContentTypeTextXml = "text/xml"
  private val `application/xml` = MessageProtocol(Some(ContentTypeApplicationXml), Some("utf-8"), None)
  private val `text/xml` = MessageProtocol(Some(ContentTypeTextXml), Some("utf-8"), None)

  implicit def XmlFormatMessageSerializer[Message: ClassTag](implicit xmlMessageSerializer: MessageSerializer[NodeSeq, ByteString], XmlFormat: XmlFormat[Message], jsFormat: Format[Message] = null): StrictMessageSerializer[Message] = new StrictMessageSerializer[Message] {
    val messageClass: Class[_] = implicitly[ClassTag[Message]].runtimeClass
    val messageClassFQN: String = messageClass.getName
    val messageClassSimpleName: String = messageClass.getSimpleName

    private class XmlFormatSerializer(elemValueSerializer: NegotiatedSerializer[NodeSeq, ByteString]) extends NegotiatedSerializer[Message, ByteString] {
      override def protocol: MessageProtocol = elemValueSerializer.protocol
      override def serialize(message: Message): ByteString = try {
        val xml: NodeSeq = XmlFormat.marshal(message)
        elemValueSerializer.serialize(xml)
      } catch {
        case NonFatal(e) => throw DeserializationException(e)
      }
    }

    private class XmlFormatDeserializer(xmlDeserializer: NegotiatedDeserializer[NodeSeq, ByteString]) extends NegotiatedDeserializer[Message, ByteString] {
      override def deserialize(wire: ByteString): Message = {
        log.debug("Deserializing: " + wire.decodeString("UTF-8"))
        val xml = xmlDeserializer.deserialize(wire)
        XmlFormat.unmarshal(xml)
      }
    }

    override def acceptResponseProtocols: Seq[MessageProtocol] = xmlMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Message, ByteString] =
      new XmlFormatDeserializer(xmlMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: Seq[MessageProtocol]): NegotiatedSerializer[Message, ByteString] = {
      val contentTypes = acceptedMessageProtocols.flatMap(_.contentType)
      contentTypes match {
        case _ if contentTypes.contains("application/json") && Option(jsFormat).isDefined =>
          MessageSerializer.jsValueFormatMessageSerializer[Message].serializerForResponse(acceptedMessageProtocols)
        case _ if contentTypes.contains("application/json") && Option(jsFormat).isEmpty =>
          throw DeserializationException(s"No Json format defined for class '$messageClassFQN', please add an implicit val format: Format[$messageClassSimpleName] = Json.format to the companion object of '$messageClassFQN' or choose another content-type by changing the value of the Accept header of your request.")
        case _ =>
          new XmlFormatSerializer(xmlMessageSerializer.serializerForResponse(acceptedMessageProtocols))
      }
    }

    override def serializerForRequest: NegotiatedSerializer[Message, ByteString] =
      new XmlFormatSerializer(xmlMessageSerializer.serializerForRequest)
  }

  implicit val XmlMessageSerializer: StrictMessageSerializer[NodeSeq] = new StrictMessageSerializer[NodeSeq] {
    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(`application/xml`, `text/xml`)

    private class XmlSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[NodeSeq, ByteString] {
      override def serialize(s: NodeSeq) = ByteString.fromString(s.toString, protocol.charset.getOrElse("utf-8"))
    }

    private class XmlDeserializer(charset: String) extends NegotiatedDeserializer[NodeSeq, ByteString] {
      override def deserialize(wire: ByteString) = XML.loadString(wire.decodeString(charset))
    }

    override def serializerForRequest: NegotiatedSerializer[NodeSeq, ByteString] = new XmlSerializer(`application/xml`)

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[NodeSeq, ByteString] = protocol.contentType match {
      case Some(ContentTypeApplicationXml) =>
        new XmlDeserializer(protocol.charset.getOrElse("utf-8"))
      case Some(ContentTypeTextXml) =>
        new XmlDeserializer(protocol.charset.getOrElse("utf-8"))
      case _ =>
        throw UnsupportedMediaType(protocol, `application/xml`)
    }

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[NodeSeq, ByteString] = {
      new XmlSerializer(`application/xml`)
    }
  }
}
