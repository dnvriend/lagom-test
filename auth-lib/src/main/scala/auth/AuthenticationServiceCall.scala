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

package auth

import java.util.Base64

import authentikat.jwt.{ JsonWebToken, JwtClaimsSet, JwtHeader }
import com.lightbend.lagom.scaladsl.api.transport.{ Forbidden, RequestHeader }
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.libs.json.{ Format, Json }

case class Auth(user: String, password: String)

trait AuthRepository {
  def getAuth(name: String): Option[Auth]
}

case class JwtCredentials(user: String, password: String)
object JwtCredentials {
  implicit val format: Format[JwtCredentials] = Json.format
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
  def jwt[Request, Response](authRepo: AuthRepository)(serviceCall: Auth => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    def isValidToken(jwtToken: String): Boolean =
      JsonWebToken.validate(jwtToken, JwtSecretKey)

    def decodePayload(jwtToken: String): Option[String] =
      jwtToken match {
        case JsonWebToken(_, claimsSet, _) => Option(claimsSet.asJsonString)
        case _                             => None
      }

    def getJwToken(authorization: String): String = {
      authorization.replace(" ", "").split("Bearer").drop(1).mkString
    }

    def authJwt(requestHeader: RequestHeader): Option[Auth] = for {
      authorization <- requestHeader.getHeader("Authorization")
      jwToken = getJwToken(authorization)
      if isValidToken(jwToken)
      payload <- decodePayload(jwToken)
      creds <- Json.parse(payload).asOpt[JwtCredentials]
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
  def basic[Request, Response](authRepo: AuthRepository)(serviceCall: Auth => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
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

  def parseAuthHeader(authHeader: String): Option[JwtCredentials] =
    authHeader.split("""\s""") match {
      case Array("Basic", userAndPass) =>
        new String(Base64.getDecoder.decode(userAndPass), "UTF-8").split(":") match {
          case Array(user, password) => Some(JwtCredentials(user, password))
          case _                     => None
        }
      case _ => None
    }
}

