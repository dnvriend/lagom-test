# helloworld
A very simple service that exposes REST endpoints

## Services
The following services are available:

## SayHello
```
$ http :9000/sayHello
HTTP/1.1 200 OK
Content-Length: 12
Content-Type: text/plain
Date: Sat, 21 Jan 2017 05:23:30 GMT

Hello World!
```

## Hello
```
$ http :9000/hello
HTTP/1.1 200 OK
Content-Length: 12
Content-Type: text/plain
Date: Sat, 21 Jan 2017 05:25:07 GMT

Hello World!
```

## Hello :name
```
$ http :9000/api/hello/Dennis
HTTP/1.1 200 OK
Content-Length: 13
Content-Type: text/plain
Date: Sat, 21 Jan 2017 05:26:24 GMT

Hello Dennis!
```

## AddItem
```
$ http post :9000/api/orders/1 name=this
HTTP/1.1 200 OK
Content-Length: 0
Date: Sat, 21 Jan 2017 05:27:12 GMT
```

## SayHelloWithNameAndAge
```
$ http :9000/api/hello/foo/42
HTTP/1.1 200 OK
Content-Length: 26
Content-Type: text/plain
Date: Sat, 21 Jan 2017 05:28:17 GMT

Hello foo, you are 42 old.
```

## SayHelloWithNameAndAgeAndPageNoAndPageSize
```
$ http :9000/api/hello/foo/42/page pageNo==1 pageSize==3
HTTP/1.1 200 OK
Content-Length: 50
Content-Type: text/plain
Date: Sat, 21 Jan 2017 05:28:56 GMT

Hello foo, you are 42 old, pageNo=1 and pageSize=3
```