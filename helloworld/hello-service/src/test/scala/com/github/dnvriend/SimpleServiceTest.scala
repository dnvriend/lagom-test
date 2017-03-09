package com.github.dnvriend

import com.github.dnvriend.component.hello.SimpleService

class SimpleServiceTest extends TestSpec {
  it should "do something" in withService(SimpleService.Name) { uri => client =>
    val result = client.url(uri + "/api/simple/foo").get.map(_.body).futureValue
    result shouldBe "Hello"
  }
}
