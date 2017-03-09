package com.github.dnvriend

import com.github.dnvriend.component.hello.SimpleService

class SimpleServiceTest extends TestSpec {
  it should "do something" in withService(SimpleService.Name) { implicit uri => client =>
    val result = client.withUrl("/api/simple/foo").get.map(_.body).futureValue
    result shouldBe "Hello"
  }
}
