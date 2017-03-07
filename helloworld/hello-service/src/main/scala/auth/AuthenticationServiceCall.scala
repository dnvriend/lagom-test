package auth

import java.util.Base64

import authentikat.jwt.{ JsonWebToken, JwtClaimsSet, JwtHeader }
import com.github.dnvriend.component.hello.Credentials
import com.lightbend.lagom.scaladsl.api.transport.{ Forbidden, RequestHeader }
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.libs.json.Json

case class Auth(user: String, password: String)

trait AuthRepository {
  def getAuth(name: String): Option[Auth]
}

object AuthenticationServiceCall {
  final val JwtSecretKey = "secretKey"
  final val JwtSecretAlgo = "HS256"

  def createToken(payload: String): String = {
    val header = JwtHeader(JwtSecretAlgo)
    val claimsSet = JwtClaimsSet(payload)
    JsonWebToken(header, claimsSet, JwtSecretKey)
  }

  /**
   * JSON Web Token (JWT)
   * see: https://blog.knoldus.com/2017/02/14/jwt-authentication-with-play-framework/
   */
  def jwt[Request, Response](authRepo: AuthRepository)(serviceCall: Auth => ServerServiceCall[Request, Response]) = {
    def isValidToken(jwtToken: String): Boolean =
      JsonWebToken.validate(jwtToken, JwtSecretKey)

    def decodePayload(jwtToken: String): Option[String] =
      jwtToken match {
        case JsonWebToken(_, claimsSet, _) => Option(claimsSet.asJsonString)
        case _                             => None
      }

    def authJwt(requestHeader: RequestHeader): Option[Auth] = for {
      jwToken <- requestHeader.getHeader("jw_token")
      if isValidToken(jwToken)
      payload <- decodePayload(jwToken)
      creds <- Json.parse(payload).asOpt[Credentials]
      auth <- authRepo.getAuth(creds.user)
      if auth.password == creds.password
    } yield auth

    ServerServiceCall.compose { (requestHeader: RequestHeader) =>
      authJwt(requestHeader).map(auth => serviceCall(auth)).getOrElse(throw Forbidden(s"User must be authenticated to access this service call"))
    }
  }

  /**
   * Basic Auth (Base64 encoded)
   */
  def basic[Request, Response](authRepo: AuthRepository)(serviceCall: Auth => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { (requestHeader: RequestHeader) =>
      val maybeAuth: Option[Auth] = for {
        authHeader <- requestHeader.getHeader("Authorization")
        creds <- parseAuthHeader(authHeader)
        auth <- authRepo.getAuth(creds.user)
        if auth.password == creds.password
      } yield auth

      println(s"[BasicAuth.auth] => Received ${requestHeader.method} ${requestHeader.uri} => $maybeAuth")

      maybeAuth.map(auth => serviceCall(auth)).getOrElse(throw Forbidden(s"User must be authenticated to access this service call $maybeAuth"))
    }

  def parseAuthHeader(authHeader: String): Option[Credentials] =
    authHeader.split("""\s""") match {
      case Array("Basic", userAndPass) =>
        new String(Base64.getDecoder.decode(userAndPass), "UTF-8").split(":") match {
          case Array(user, password) => Some(Credentials(user, password))
          case _                     => None
        }
      case _ => None
    }
}

