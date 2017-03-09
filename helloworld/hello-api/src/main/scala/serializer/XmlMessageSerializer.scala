package serializer

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.scaladsl.api.deser.StrictMessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.{ MessageProtocol, UnsupportedMediaType }

import scala.collection.immutable
import scala.xml.{ Elem, XML }

object XmlMessageSerializer {
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
