# lagom-scala-test
A small study project on the [Lagom Scala](http://www.lagomframework.com/documentation/1.3.x/java/Home.html) microservice framework.

## Why Lagom?
Developers should be focused on solving their business problems, not on wiring services together.

## Introduction
Lagom is a framework for creating microservice-based 'systems'.
It provides a [Service API](http://www.lagomframework.com/documentation/1.3.x/scala/ServiceDescriptors.html),
a [Persistence API](http://www.lagomframework.com/documentation/1.3.x/scala/PersistentEntity.html).

## sbt tasks
- runAll: starts kafka, cassandra, service locator, service gateway and all defined [Service](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.api.Service)s

```
> runAll
[info] Starting Kafka
[info] Starting Cassandra
.......
[info] Cassandra server running at 127.0.0.1:4000
[info] Service locator is running at http://localhost:8000
[info] Service gateway is running at http://localhost:9000
[info] Service lagom-scala-stream-impl listening for HTTP on 0:0:0:0:0:0:0:0:50007
[info] Service lagom-scala-impl listening for HTTP on 0:0:0:0:0:0:0:0:63812
[info] (Services started, press enter to stop and go back to the console...)
```

## Service API
The [Service API](http://www.lagomframework.com/documentation/1.3.x/scala/ServiceDescriptors.html)
provides a way to declare and implement service interfaces, to be consumed by clients.
For location transparency, clients discover services through a Service Locator. The Service API supports 
synchronous request-response calls as well as asynchronous streaming between services.

## Persistence API
The [Persistence API](http://www.lagomframework.com/documentation/1.3.x/scala/PersistentEntity.html)
provides event-sourced persistent entities for services that store data. Command Query
Responsibility Segregation (CQRS) read-side support is provided as well. Lagom manages the distribution 
of persisted entities across a cluster of nodes, enabling sharding and horizontal scaling. Cassandra is provided 
as a database out-of-the-box.

## ServiceLocator API
The service locator API is responsible for two things, one is locating services according to the passed in name and
service call information, the other is to implement circuit breaking functionality when
'doWithService' is invoked.

The reason circuit breaking is a service locator concern is that generally, the service locator will want to be aware
of when a circuit breaker is open, and respond accordingly. For example, it may decide to pull that node from its
routing pool, or it may decide to notify some up stream service registry that that node is no longer responding.

The 'com.lightbend.lagom.scaladsl.api.ServiceLocator' can be injected with @Inject

## PersistentEntityRegistry
At system startup all
[PersistentEntity](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.persistence.PersistentEntity)
classes must be registered at the [PersistentEntityRegistry](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry)
with 'PersistentEntityRegistry.register'.

Later, a com.lightbend.lagom.scaladsl.persistence.PersistentEntityRef can be retrieved with 'PersistentEntityRegistry.refFor'.

Commands are sent to a 'com.lightbend.lagom.scaladsl.persistence.PersistentEntity' using the retrieved 'PersistentEntityRef'.

The 'com.lightbend.lagom.scaladsl.persistence.PersistentEntity' can be injected with @Inject

## Compile-time DI
For a very complete description of compile-time-dependency injection please read [Play's Compile-time dependency injection](https://www.playframework.com/documentation/2.5.x/ScalaCompileTimeDependencyInjection#Compile-Time-Dependency-Injection).

Lagom is built on top of the playframework, and the playframework has support for compile-time injection but also for
runtime-dependency injection, that is dependency injection where dependencies aren't wired until runtime. With compile-time
dependency injection the dependencies for an object graph are wired at compile-time which has some advantages in that
you can be sure that in compile-time the object graph can be wired.

Of course, creating an object graph can be done is many ways but the constructor-based/manual wiring is maybe the most simplest
approach. Out of the box, a default play project uses runtime-dependency injection but with a little bit of work play can use
compile-time dependency injection.

### Components
Play consists of modules and components that all have support for compile-time dependency injection using constructor arguments.
These components are grouped in traits (modules) that all end with the naming convention `Components`

These traits follow a naming convention of ending the trait name with `Components` like for example here is an (incomplete)
list of play and lagom components:

- play.api.i18n.I18nComponents
- play.api.libs.concurrent.AkkaComponents
- play.api.libs.ws.ahc.AhcWSComponents
- play.api.BuiltInComponents
- play.core.server.NettyServerComponents
- play.api.libs.openid.OpenIDComponents
- com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
- com.lightbend.lagom.scaladsl.persistence.cassandra.WriteSideCassandraPersistenceComponents
- com.lightbend.lagom.scaladsl.persistence.cassandra.ReadSideCassandraPersistenceComponents
- com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
- com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
- com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
- com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
- com.lightbend.lagom.scaladsl.client.CircuitBreakerComponents
- com.lightbend.lagom.scaladsl.client.StaticServiceLocatorComponents
- com.lightbend.lagom.scaladsl.client.RoundRobinServiceLocatorComponents
- com.lightbend.lagom.scaladsl.cluster.ClusterComponents
- com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
- com.lightbend.lagom.scaladsl.devmode.LagomDevModeServiceLocatorComponents
- com.lightbend.lagom.scaladsl.server.LagomServerComponents
- com.lightbend.lagom.scaladsl.server.status.MetricsServiceComponents

### ApplicationLoader
Play and also Lagom has a very nice developer experience in that, you launch the application and while you
are developing, the changes are automatically compiled and the application is restarted. For this to work the
JVM and the HTTP server must be kept running so the whole reloading experience remains snappy!

For this to work, Play provides an [play.api.ApplicationLoader](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.ApplicationLoader) trait
that we must implement. The application loader is constructed and invoked every time the application is reloaded, to load the application.

A Play application is launched, like with any Java application, using a main() class. Because Play (and Lagom) is a very developer friendly
framework for creating reactive applications, we usually don't have to bother with configuring the main class, because we just type `sbt:dist` and
or even better, `bundle:dist` and we have a complete deployable bundle available complete with scripts to launch the application.

Play provides two main classes to launch the application which are [play.core.server.ProdServerStart](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.core.server.ProdServerStart$)
which is used to start servers in 'prod' mode, the mode that is used in production which means that the application is loaded and started immediately.
There is also the [play.core.server.DevServerStart](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.core.server.DevServerStart$)
which is used to start servers in 'dev' mode, which means a mode where the application is reloaded whenever its source changes. The `DevServerStart` is
used when you type `sbt:run` in SBT when developing a Play application. These two classes, `ProdServerStart` and `DevServerStart`
use the `play.api.ApplicationLoader` to bootstrap (wire-configure-and-launch) the application.

So application loaders are used by these 'main' classes to wire-and-configure the application. Application loaders are
therefor expected to instantiate all parts of an application, wiring everything together.

Out of the box Play provides the [play.api.inject.guice.GuiceApplicationLoader](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.inject.guice.GuiceApplicationLoader)
which is an `ApplicationLoader` that uses [Google Guice](https://github.com/google/guice), which is a runtime dependency injection
framework to bootstrap the application. The `GuiceApplicationLoader` is wired in the `reference.conf` of play
so when your main() class launches your Play application, the configuration will point to:

```
play.application.loader = "play.api.inject.guice.GuiceApplicationLoader"
```

A custom application loader can be configured using the `play.application.loader` configuration property.

To recap, the `play.api.ApplicationLoader` is constructed and invoked every time the application is reloaded to load the application
and is used by the Play provided main() classes.

### The Context
The ApplicationLoader trait contains a load method that takes an `Context` as an argument. The Context contains
all the components required by a Play application that outlive the application itself and cannot be constructed by
the application itself when it restarts. A number of these components exist specifically for the purposes of providing
functionality in dev mode, for example, the source mapper allows the Play error handlers to render the source code of the
place that an exception was thrown.

```scala
trait ApplicationLoader {
  def load(context: ApplicationLoader.Context): Application
}
```

### LagomApplicationLoader
Lagom, which is based on Play, provides the [com.lightbend.lagom.scaladsl.server.LagomApplicationLoader](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.server.LagomApplicationLoader).
The LagomApplicationLoader is an abstraction over Play's application loader and provides Lagom specific functionality and of course bootstraps the application.
Any Lagom application should provide a subclass of the LagomApplicationLoader and configure it in `application.conf` so that it will be used by play to run your application.
The LagomApplicationLoader will contain a [com.lightbend.lagom.scaladsl.server.LagomApplication](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.server.LagomApplication)
which must be subclassed to actually wire together a Lagom application. In the next example the `HelloApplication` class is a LagomApplication that wires
the Lagom application object graph.

For example:

```scala
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents

class HelloApplicationLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) =
    new HelloApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) =
    new HelloApplication(context) {
      override def serviceLocator = ServiceLocator.NoServiceLocator
    }
}
```

You will see the class `HelloApplication`, which is our actual Lagom application that will contain all the service
bindings and wires together a Lagom application.

We need to tell Play about the application loader. We can do that by adding the following configuration to
`application.conf`:

```
play.application.loader = com.example.HelloLoader
```

### LagomApplication
The [com.lightbend.lagom.scaladsl.server.LagomApplication](http://www.lagomframework.com/documentation/1.3.x/scala/api/index.html#com.lightbend.lagom.scaladsl.server.LagomApplication)
wires together a Lagom application. This includes the Lagom server components (which builds and provides the Lagom router) as well as the Lagom service client components
(which allows implementing Lagom service clients from Lagom service descriptors). Any Lagom application must subclass the LagomApplication and provide the necessary wiring.

Two methods must be implemented `def lagomServer: LagomServer` and `def serviceLocator: ServiceLocator`.
Typically, the `lagomServer` will be implemented by subclassing the LagomApplication, and will bind all the services that
this Lagom application provides. Meanwhile, the `serviceLocator` member will be provided by mixing in a service locator
components trait in `LagomApplicationLoader`, which trait is mixed in will vary depending on whether the application
is being loaded for production or for development.

For example:

```scala
abstract class LagomscalaApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer: LagomServer = LagomServer.forServices(
    bindService[LagomscalaService].to(wire[LagomscalaServiceImpl])
  )

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = LagomscalaSerializerRegistry

  // Register the lagom-scala persistent entity
  persistentEntityRegistry.register(wire[LagomscalaEntity])
}
```

The ApplicationLoader (which must be registered in `application.conf`) can have the following implementation:

```scala
class LagomscalaLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomscalaApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomscalaApplication(context) with LagomDevModeComponents
}
```

Here, the required `ServiceLocator` will be mixed in or implemented depending on the
'dev' or 'prod' mode how the application is launched.

For an indepth discussion on how to wire a Lagom application please read:
[Wiring together a Lagom application](http://www.lagomframework.com/documentation/1.3.x/scala/ServiceImplementation.html#Wiring-together-a-Lagom-application).

## ConductR
see: http://www.lagomframework.com/documentation/1.3.x/scala/ConductR.html

## Documentation
- [Lagom Blog](http://www.lagomframework.com/blog/)
- [LagomScala documentation](http://www.lagomframework.com/documentation/1.3.x/scala/Home.html)
- [lagom-sbt-plugin](https://bintray.com/lagom/sbt-plugin-releases/lagom-sbt-plugin)

## YouTube
- [(2'36 hr) Reactive Programming - Venkat Subramaniam](https://www.youtube.com/watch?v=weWSYIUdX6c)
- [(0'52 hr) Modular monoliths - Simon Brown](https://www.youtube.com/watch?v=kbKxmEeuvc4)
- [(0'56 hr) Principles Of Microservices - Sam Newman](https://www.youtube.com/watch?v=PFQnNFe27kU)
- [(0'51 hr) The Future of Services: Building Asynchronous, Resilient and Elastic Systems - Jamie Allen](https://www.youtube.com/watch?v=3wMokfiUuzk)
- [(0'13 hr) Introduction to Lagom - James Roper](https://www.youtube.com/watch?v=D9y2Ex3NN34)
- [(0'42 hr) Microservices Just Right - by Mirco Dotta](https://www.youtube.com/watch?v=fRlx_fxar-U)
- [(1'00 hr) Managing Microservices in Production with Lagom - Chrisopher Hunt](https://www.youtube.com/watch?v=PT4H70SNyUo)
- [(0'48 hr) Lagom in Practice - Yannick De Turck](https://www.youtube.com/watch?v=JOGlZzY6ycI)
- [(0'44 hr) Taking the friction out of microservice frameworks with Lagom - Markus Eisele](https://www.youtube.com/watch?v=7LSpgem2VZc)
- [(0'53 hr) Lightbend Lagom: Microservices “Just Right" - Brendan McAdams](https://www.youtube.com/watch?v=nMcSQM4N56Y)

## Lagom Scala Release History

- [1.3.0-RC1](http://www.lagomframework.com/blog/lagom-1-3-RC1.html) (2017-01-24)
- [1.3.0-M1](http://www.lagomframework.com/blog/lagom-scala-api-preview.html) (2016-12-09)

Have fun!