# lagom-test
A small study project on [Lagom]()

## Introduction
Lagom is a framework for creating microservice-based systems. It provides a [Service API](http://www.lagomframework.com/documentation/1.3.x/scala/ServiceDescriptors.html) 
and a [Persistence API](http://www.lagomframework.com/documentation/1.3.x/scala/PersistentEntity.html).

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
The Service API provides a way to declare and implement service interfaces, to be consumed by clients. 
For location transparency, clients discover services through a Service Locator. The Service API supports 
synchronous request-response calls as well as asynchronous streaming between services.

## Persistence API
The Persistence API provides event-sourced persistent entities for services that store data. Command Query 
Responsibility Segregation (CQRS) read-side support is provided as well. Lagom manages the distribution 
of persisted entities across a cluster of nodes, enabling sharding and horizontal scaling. Cassandra is provided 
as a database out-of-the-box.

## Video
- [Lightbend Lagom: Microservices Just Right - by Mirco Dotta](https://www.youtube.com/watch?v=fRlx_fxar-U)