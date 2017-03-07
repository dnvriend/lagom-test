package auth

import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

object LoggingServiceCall {
  def logged[Request, Response](serviceCall: ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { (requestHeader: RequestHeader) =>
      println(s"Received ${requestHeader.method} ${requestHeader.uri}")
      serviceCall
    }
}
