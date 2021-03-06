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

package com.github.dnvriend

import com.github.dnvriend.api.SimpleService

class SimpleServiceTest extends TestSpec {
  it should "do something" in withService(SimpleService.Name) { implicit uri => client =>
    val result = client.withUrl("/api/simple/foo").get.map(_.body).futureValue
    result shouldBe "Hello"
  }
}
