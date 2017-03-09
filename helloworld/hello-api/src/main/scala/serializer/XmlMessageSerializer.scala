package serializer

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.scaladsl.api.deser.{ MessageSerializer, StrictMessageSerializer }
import com.lightbend.lagom.scaladsl.api.transport._

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.util.control.NonFatal
import scala.xml.{ Elem, XML }

trait ElemFormat[Message] {
  def toElem(msg: Message): Elem
  def fromElem(xml: Elem): Message
}

object XmlMessageSerializer {

  implicit def elemFormatMessageSerializer[Message](implicit xmlMessageSerializer: MessageSerializer[Elem, ByteString], elemFormat: ElemFormat[Message]): StrictMessageSerializer[Message] = new StrictMessageSerializer[Message] {
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

    override def serializerForResponse(acceptedMessageProtocols: Seq[MessageProtocol]): NegotiatedSerializer[Message, ByteString] =
      new ElemFormatSerializer(xmlMessageSerializer.serializerForResponse(acceptedMessageProtocols))

    override def serializerForRequest: NegotiatedSerializer[Message, ByteString] =
      new ElemFormatSerializer(xmlMessageSerializer.serializerForRequest)
  }

  implicit val XmlMessageSerializer: StrictMessageSerializer[Elem] = new StrictMessageSerializer[Elem] {
    final val ContentTypeApplicationXml = "application/xml"
    final val ContentTypeTextXml = "text/xml"
    private val `application/xml` = MessageProtocol(Some(ContentTypeApplicationXml), Some("utf-8"), None)
    private val `text/xml` = MessageProtocol(Some(ContentTypeTextXml), Some("utf-8"), None)
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
