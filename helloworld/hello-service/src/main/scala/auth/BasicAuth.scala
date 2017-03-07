package auth

import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import java.util.Base64

case class Auth(user: String, password: String)

trait AuthRepository {
  def getAuth(name: String): Option[Auth]
}

object BasicAuth {
  case class Credentials(user: User, password: Password)
  case class User(value: String)
  case class Password(value: String)

  def auth[Request, Response](authRepo: AuthRepository)(serviceCall: Auth => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { (requestHeader: RequestHeader) =>
      val maybeAuth: Option[Auth] = for {
        authHeader <- requestHeader.getHeader("Authorization")
        creds <- parseAuthHeader(authHeader)
        user <- authRepo.getAuth(creds.user.value)
        if user.password == creds.password.value
      } yield user

      println(s"[BasicAuth.auth] => Received ${requestHeader.method} ${requestHeader.uri} => $maybeAuth")

      maybeAuth.map(auth => serviceCall(auth)).getOrElse(throw Forbidden(s"User must be authenticated to access this service call $maybeAuth"))
    }

  def parseAuthHeader(authHeader: String): Option[Credentials] =
    authHeader.split("""\s""") match {
      case Array("Basic", userAndPass) =>
        new String(Base64.getDecoder.decode(userAndPass), "UTF-8").split(":") match {
          case Array(user, password) => Some(Credentials(User(user), Password(password)))
          case _                     => None
        }
      case _ => None
    }
}

