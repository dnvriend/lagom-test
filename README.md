# lagom-scala-test
A small study project on the [Lagom Scala](http://www.lagomframework.com/documentation/1.3.x/java/Home.html) microservice framework.

## Why Lagom?
Developers should be focused on solving their business problems, not on wiring services together.

## Disclaimer
This project uses some commercial features provided by [Lightbend](http://www.lightbend.com/) like
[Lightbend ConductR](https://conductr.lightbend.com/) and [Lightbend Monitoring (Cinnamon)](http://developer.lightbend.com/docs/monitoring/latest/home.html)
that are both __free to use for development purposes only__. Lightbend provides a free [ConductR sandbox environment](https://www.lightbend.com/product/conductr/developer)
that provides all the features that you need to use ConductR and Lightbend Monitoring. You'll have to create a __free__ [Lightbend developer account](https://www.lightbend.com/account)
if you don't have one and you'll have to create your [Reactive Platform Credentials](https://www.lightbend.com/product/lightbend-reactive-platform/credentials) and put
them in a directory as described by the [Reactive Platform Setup Documentation](https://developer.lightbend.com/docs/reactive-platform/2.0/setup/setup-sbt.html).

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
[ConductR](http://conductr.lightbend.com/) is a container orchestration tool with the main goal of delivering operational
productivity. ConductR is also designed to host your Lagom services with resilience. For a developer this means that
ConductR is free for development usage and comes with a 'sandbox' environment so that you can run ConductR locally
and test your services. A service doesn't have to be a Lagom service, it can be any Java application that you enhance with
the necessary metadata so that ConductR knows how to deploy and scale your application. The normal use case however is that
ConductR is often used with an SBT build that has been extended with some plugins and settings to deploy your Lagom, Play or Akka
application.

- An example how to use Lightbend ConductR Lightbend Monitoring (Cinnamon) with [Akka](https://github.com/dnvriend/conductr-test/tree/master/hello-akka)
- An example how to use Lightbend ConductR Lightbend Monitoring (Cinnamon) with [Play](https://github.com/dnvriend/play-cinnamon-test)

## ConductR and Lagom
Lagom can be deployed with [ConductR](http://conductr.lightbend.com/) as described in the [Lagom Documentation](http://www.lagomframework.com/documentation/1.3.x/scala/ConductR.html),
that you really should read, and is explained by Christopher Hunt on Youtube in this 12 minute video [Lagom ConductR Deployment](https://www.youtube.com/watch?v=0Z0UIZxW1aw).

## Overview Of ConductR Commands
The [sbt-conductr](https://github.com/typesafehub/sbt-conductr) plugin adds several commands to the sbt console:

Property                     | Description
-----------------------------|------------
bundle:dist                  | Produce a ConductR bundle for all projects that have the native packager enabled
configuration:dist           | Produce a bundle configuration for all projects that have the native packager enabled
sandbox help                 | Get usage information of the sandbox command
sandbox run                  | Start a local ConductR sandbox
sandbox stop                 | Stop the local ConductR sandbox
sandbox version              | Print the conductr-cli version
conduct help                 | Get usage information of the conduct command
conduct info                 | Gain information on the cluster
conduct load                 | Loads a bundle and an optional configuration to the ConductR
conduct run                  | Runs a bundle given a bundle id with an optional absolute scale value specified with --scale
conduct stop                 | Stops all executions of a bundle given a bundle id
conduct unload               | Unloads a bundle entirely (requires that the bundle has stopped executing everywhere)
conduct logs                 | Retrieves log messages of a given bundle
conduct events               | Retrieves events of a given bundle
conduct service-names        | Retrieves the service names available to the service locator
conduct version              | Print the conductr-cli version
install                      | Generates an installation script and then installs all of your projects to the local ConductR sandbox (expected to be running)
generateInstallationScript   | To produce a deployment script for all your services that you can then tailor

Each `sandbox` and `conduct` sub command has a help page particular for the sub command, e.g. `conduct run --help`.

## Most Important Commands
The most important [sbt-conductr](https://github.com/typesafehub/sbt-conductr) commands are:

Property                   | Description
---------------------------|------------
install                    | To introspect your project and deploy all services within the ConductR sandbox
generateInstallationScript | To produce a deployment script for all your services that you can then tailor
bundle:dist                | To produce individual ConductR packages for your services
configuration:dist         | To produce individual ConductR configurations for your services

## ConductR Network Address Aliases (NAA)
In order to run a ConductR cluster locally, we use network address aliases. These address aliases will allow ConductR
to bind to the required ports to run locally without port collisions. Since we will be starting 3 node cluster,
3 address aliases are required for each node respectively.

The address aliases are temporary. If you reboot, you'll need to run the above commands before running the sandbox again.

For OSX, execute the following commands to create the address aliases:

```bash
sudo sh -c "ifconfig lo0 alias 192.168.10.1 255.255.255.0 && \
ifconfig lo0 alias 192.168.10.2 255.255.255.0 && \
ifconfig lo0 alias 192.168.10.3 255.255.255.0"
```

## Sandbox cluster
To start a three node ConductR cluster on your local machine use:

```bash
sandbox run 2.0.2 --nr-of-containers 3
```

Note: A three node cluster uses a lot of memory, you should limit the number of nodes to one or two:

```bash
sandbox run 2.0.2 --nr-of-containers 1

// or
sandbox run 2.0.2 --nr-of-containers 2
```

## Sandbox Features
The sandbox contains handy features which can be optionally enabled during startup by specifying the `--feature` option, e.g.:

```bash
sandbox run 2.0.2 --feature visualization --feature monitoring --nr-of-containers 2
```

The following features are available:

 Name         | Description
--------------|-------------------------------------------------------------------------------------
visualization | Provides a web interface to visualize the ConductR cluster together with the deployed running bundles.
logging       | Out-of-the-box the sandbox starts a simple logging service called `eslite`. This service is automatically enabled without specifying the `logging` feature. This in-memory logging service only captures the log messages within one node. In case you want to retrieve log messages from multiple nodes use the `logging` feature. This will start an elasticsearch and kibana bundle. The elasticsearch bundle will capture the stdout and sterr output of your bundles. To display the log messages use either the `conduct logs` command or the Kibana UI on port 5601. Make sure that your VM has sufficient memory when using this feature.
monitoring    | Enables Lightbend Monitoring for your bundles

## Sandbox with monitoring
To run sandbox with monitoring:

```bash
sandbox run 2.0.2 --feature visualization --feature monitoring --nr-of-containers 2
```

After a while (a minute or so) you should see the following:

```bash
$ conduct info
ID               NAME                     #REP  #STR  #RUN
73595ec          visualizer                  1     0     1
bdfa43d-e5f3504  conductr-haproxy            1     0     1
06d370b          conductr-kibana             1     0     1
d4bdc6c          cinnamon-grafana-docker     1     0     1
85dd265          conductr-elasticsearch      1     0     1
```

## Sandbox with Cassandra
It is possible to run [Apache Cassandra](http://cassandra.apache.org/) in the sandbox environment. The project
[conductr-cassandra](https://github.com/typesafehub/conductr-cassandra) provides this feature. To run cassandra
in the sandbox environment you should do the following:

First, load the Cassandra on to ConductR:

```bash
> conduct load cassandra
```
To run the cassandra bundle execute:

```bash
> conduct run cassandra
```

## Sandbox with Zookeeper
It is possible to run [Apache Zookeeper](http://zookeeper.apache.org/) in the sandbox environment. The project
[conductr-zookeeper](https://github.com/typesafehub/conductr-zookeeper) provides this feature. To run zookeeper
in the sandbox environment you should do the following:

```bash
> conduct load zookeeper
```
To run the zookeeper bundle execute:

```bash
> conduct run zookeeper
```

## Deploy the application with ConductR
First you'll have to create a bundle. The easiest way is to use sbt for that:

```bash
[play-cinnamon-test] $ bundle:dist
...
[info] Done packaging.
[info] Bundle has been created: /Users/dennis/projects/play-cinnamon-test/target/bundle/play-cinnamon-test-v1-19c248a4ee742e34c644db236acf662ea6ac37e43747df86d5c8059bf28261d2.zip
[success] Total time: 11 s, completed 1-mrt-2017 12:43:35
```

Then ConductR needs to to load the bundle:

```bash
[play-cinnamon-test] $ conduct load /Users/dennis/projects/play-cinnamon-test/target/bundle/play-cinnamon-test-v1-19c248a4ee742e34c644db236acf662ea6ac37e43747df86d5c8059bf28261d2.zip
Retrieving bundle..
Retrieving file:///Users/dennis/projects/play-cinnamon-test/target/bundle/play-cinnamon-test-v1-19c248a4ee742e34c644db236acf662ea6ac37e43747df86d5c8059bf28261d2.zip
Loading bundle to ConductR..
Bundle 19c248a4ee742e34c644db236acf662e is installed
Bundle loaded.
Start bundle with: conduct run 19c248a
Unload bundle with: conduct unload 19c248a
Print ConductR info with: conduct info
```

Next we need to run the bundle:

```bash
[play-cinnamon-test] $ conduct run 19c248a
Bundle run request sent.
Bundle 19c248a4ee742e34c644db236acf662e waiting to reach expected scale 1
Bundle 19c248a4ee742e34c644db236acf662e expected scale 1 is met
Stop bundle with: conduct stop 19c248a
Print ConductR info with: conduct info
[success] Total time: 5 s, completed 1-mrt-2017 12:46:15
```

With a little luck we can see the bundle running in both the console and the [visualizer](http://192.168.10.1:9999):

```bash
[play-cinnamon-test] $ conduct info
ID               NAME                     #REP  #STR  #RUN
19c248a          play-cinnamon-test          1     0     1
73595ec          visualizer                  1     0     1
bdfa43d-e5f3504  conductr-haproxy            1     0     1
06d370b          conductr-kibana             1     0     1
d4bdc6c          cinnamon-grafana-docker     1     0     1
85dd265          conductr-elasticsearch      1     0     1
```

Lets find out the available service-names:

```bash
[play-cinnamon-test] $ conduct service-names
SERVICE NAME    BUNDLE ID  BUNDLE NAME              STATUS
elastic-search  85dd265    conductr-elasticsearch   Running
es-internal     85dd265    conductr-elasticsearch   Running
grafana         d4bdc6c    cinnamon-grafana-docker  Running
kibana          06d370b    conductr-kibana          Running
play            19c248a    play-cinnamon-test       Running
visualizer      73595ec    visualizer               Running
```

Alright, so our application is availabel at 'http://192.168.10.1:9000/play', so our services
are available at 'http://192.168.10.1:9000/play/api/actors' for example:

```bash
$ http 192.168.10.1:9000/play/api/actor
HTTP/1.1 200 OK
Content-Length: 13
Content-Type: text/plain; charset=utf-8
Date: Wed, 01 Mar 2017 11:48:58 GMT

Hello World!!
```



## Explore
As described in the [cinnamon user manual](https://developer.lightbend.com/docs/monitoring/latest/sandbox/explore.html):

- [Proxy port 9000](http://192.168.10.1:9000)
- [ConductR visualizer on port 9999](http://192.168.10.1:9999)
- [Kibana on port 5601](http://192.168.10.1:5601)
- [Grafana on port 3000](http://192.168.10.1:3000)

## Documentation
- [Lagom Blog](http://www.lagomframework.com/blog/)
- [LagomScala documentation](http://www.lagomframework.com/documentation/1.3.x/scala/Home.html)
- [lagom-sbt-plugin](https://bintray.com/lagom/sbt-plugin-releases/lagom-sbt-plugin)

## YouTube
- [(0'12 hr) Lagom ConductR Deployment - Christopher Hunt](https://www.youtube.com/watch?v=0Z0UIZxW1aw)
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
- [(0'21 hr) Building Better Microservices with Lagom - Tom Peck](https://www.youtube.com/watch?v=VX8PWjYZuQE)

## Lagom Scala Release History
- [Lagom Change Log - All Versions](http://www.lagomframework.com/changelog.html)
- [1.3.1](http://www.lagomframework.com/blog/lagom-1-3-1-and-1-2-3.html) (2017-03-06)
- [1.3.0](http://www.lagomframework.com/blog/lagom-1-3.html) (2017-02-22)
- [1.3.0-RC2](http://www.lagomframework.com/blog/lagom-1-3-RC2.html) (2017-02-16)
- [1.3.0-RC1](http://www.lagomframework.com/blog/lagom-1-3-RC1.html) (2017-01-24)
- [1.3.0-M1](http://www.lagomframework.com/blog/lagom-scala-api-preview.html) (2016-12-09)

Have fun!