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

package com.github.dnvriend.component.hello

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.transport.Method
import play.api.libs.json.{ Format, Json }

case class Message(msg: String, time: Long)
object Message {
  implicit val format: Format[Message] = Json.format
}

object Item {
  implicit val format: Format[Item] = Json.format
}
final case class Item(name: String)

case class Credentials(user: String, password: String)
object Credentials {
  implicit val format: Format[Credentials] = Json.format
}

// http://www.lagomframework.com/documentation/1.3.x/scala/ServiceDescriptors.html
//
// The HelloApi (which is-a Service) mixes in a DSL for describing a (Lagom) Service.
//
// A service describes itself by providing information about the 'calls' that can be made
// (the capabilities of the service) and providing a mapping of 'calls-to-transport'
// by providing this mapping in the Descriptor
//
trait HelloApi extends Service {

  // A ServiceCall is an abstraction of a service call for an entity.
  //
  // A ServiceCall, is a representation of the call that can be invoked when consuming the service,
  // and implemented by the Service itself.
  //
  // A service call has a Request and a Response entity. (ServiceCall[Request, Response])
  // Entity may be either 'NotUsed', if there is no entity associated with the call or it may be an Akka streams Source,
  // in situations where the endpoint serves a stream. In all other cases, the entities will be considered "strict" entities,
  // that is, they will be parsed into memory, eg, using Json.
  //
  def sayHello: ServiceCall[NotUsed, String]
  def sayHelloAuth: ServiceCall[NotUsed, String]
  def sayHelloAuthJwt: ServiceCall[NotUsed, String]
  def sayHelloWithName(userName: String): ServiceCall[NotUsed, String]
  def sayHelloWithNameAndAge(userName: String, age: Int): ServiceCall[NotUsed, String]
  def sayHelloWithNameAndAgeAndPageNoAndPageSize(userName: String, age: Int, pageNo: Long, pageSize: Int): ServiceCall[NotUsed, String]
  def addItem(orderId: Long): ServiceCall[Item, NotUsed]
  def createToken: ServiceCall[Credentials, String]
  def produceMessage(msg: String, key: String): ServiceCall[NotUsed, NotUsed]

  // While the 'sayHello' method describes how the call will be programmatically invoked or implemented,
  // it does not describe how this call gets mapped down onto the transport.
  //
  // This mapping is done by providing an implementation of the descriptor call,
  // that describes the service
  //
  override def descriptor: Descriptor = {
    // named: Create a descriptor (API) for a service with the given name
    // Lagom services are described by an interface, known as a service descriptor.
    //
    // The descriptor describes how the service is invoked (called) and implemented,
    // and defines the metadata that describes how the interface is mapped down onto an
    // underlying transport protocol.
    //
    // Generally, the service descriptor, its implementation and consumption should remain agnostic to what transport is being used,
    // whether that’s REST, websockets, or some other transport.
    val descriptor: Descriptor = named("hello-api")

    // Each service call needs to have an identifier.
    // An identifier is used to provide 'routing information' to the implementation of the client, and the service,
    // so that calls over the wire can be mapped to the appropriate call.

    // Identifiers can be a static name or path, or they can have dynamic components,
    // where dynamic path parameters are extracted from the path and passed to the service call methods.

    // ###################
    // ## Call Identifiers
    // ###################
    //
    // The simplest type of identifier is 'a name', and by default,
    // that name is set to be the same name as the name of the method on the interface that implements it.
    // so that would be 'call(sayHello)' and will be available at 'http :9000/sayHello' => Hello World!
    //
    // The identifier can also be customized by giving it a name with the 'namedCall' method
    // available at 'http :9000/api/hello/foo' => Hello foo!

    // #########################
    // ## Path based identifiers
    // #########################
    // The path based identifier uses a URI path and query string to route calls.
    // The dynamic path parameters can optionally be extracted out.
    // Path based identifiers can be configured using the 'pathCall' method.
    //
    // Dynamic path parameters are extracted from the path by declaring dynamic parts in the path.
    // These dynamic parts are prefixed with a colon, for example, a path of '/order/:id'
    // has a dynamic part called 'id'. Lagom will extract this parameter from the path,
    // and pass it to the service call method. In order to convert the dynamic part (id)
    // to the type accepted by the method, Lagom will use an implicitly provided PathParamSerializer.
    // Lagom includes many PathParamSerializer’s out of the box, such as for String, Long, Int, Boolean and UUID.

    // When you use 'call', 'namedCall' or 'pathCall',
    // Lagom will make a best effort attempt to map it down to REST
    // in a semantic fashion. So for example, if there is a 'Request' message
    // it will use the POST method, whereas if there’s none it will use GET
    // and all examples here has a ServiceCall[NotUsed, String] so the Request is NotUsed and
    // so Lagom will map those calls to the GET REST method

    // ###################
    // ## REST identifiers
    // ###################
    //
    // The final type of identifier is a 'REST identifier'.
    // REST identifiers are designed to be used when creating semantic REST APIs.
    // They use both a path, as with the path based identifier,
    // and a request method, to identify them.
    // They can be configured using the 'restCall' method.

    descriptor.withCalls(
      // ##
      // ## Call Identifiers
      // ##
      // uses the name of the method and is available at: http :9000/sayHello
      call(sayHello),

      // using a customized name here 'hello' and is available at: http :9000/hello
      namedCall("hello", sayHello),
      // using a customized name here 'helloAuth' and is available at: http --auth foo:bar :9000/helloAuth
      namedCall("helloAuth", sayHelloAuth),
      // using JWT for authentication available at: http :9000/helloAuthJwt jw_token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoiZm9vIiwicGFzc3dvcmQiOiJiYXIifQ.36Px3s92nRp0sqZkRwS6nVmHZBRZhsGA0PR8IsEgsnU
      namedCall("helloAuthJwt", sayHelloAuthJwt),
      // creating a token available at: http :9000/createToken user=foo password=bar
      namedCall("createToken", createToken),

      // ##
      // ## Path based identifiers
      // ##
      // pathCall: Create a path service call descriptor, identified by the given path pattern.
      // Note that the 'sayHello' method does not actually invoke the call,
      // it simply gets a handle to the call, (ETA to a function)
      // which can then be invoked using the invoke method. The path contains a dynamic part
      // which is 'name' and Lagom will look at the method and determine that sayHelloWithName needs
      // a String so a String PathParamSerializer will be used.
      //
      // uses a path based identifier with a dynamic part and is availabel at 'http :9000/api/hello/foo'
      pathCall("/api/hello/:name", sayHelloWithName _),

      // available at: 'http post :9000/api/orders/1 name=this'
      pathCall("/api/orders/:id", addItem _),

      // Multiple parameters can be extracted out,
      // these will be passed to the service call method
      // in the order they are extracted from the URL
      // available at 'http :9000/api/hello/foo/42'
      pathCall("/api/hello/:name/:age", sayHelloWithNameAndAge _),

      // Query string parameters can be extracted from the path,
      // using a '&' separated list after a '?' at the end of the path.
      // available at: 'http :9000/api/hello/foo/42/page pageNo==1 pageSize==3'
      pathCall("/api/hello/:name/:age/page?pageNo&pageSize", sayHelloWithNameAndAgeAndPageNoAndPageSize _),

      // available at 'http :9000/api/produce/foo/123'
      pathCall("/api/produce/:msg/:key", produceMessage _),

      // ##
      // ## Rest Call
      // ##
      // available at: 'http :9000/api/hello-with-name/foo'
      restCall(Method.GET, "/api/hello-with-name/:name", sayHelloWithName _),
      // available at: 'http :9000/api/hello-with-name-and-age/foo/42'
      restCall(Method.GET, "/api/hello-with-name-and-age/:name/:age", sayHelloWithNameAndAge _),
      // available at: 'http post :9000/api/the-orders/1 name=that'
      restCall(Method.POST, "/api/the-orders/:id", addItem _)
    )
      // withAutoAcl: Whether this service call should automatically define an ACL
      // for the router to route external calls to it,
      // when true, the service is accessible for external calls
      .withAutoAcl(true)
  }
}

// so a service describes:
// - the name of the service
// - the calls that can be made
// - the mapping 'call-to-transport'

