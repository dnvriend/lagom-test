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

package com.github.dnvriend.adapters.services

import akka.NotUsed
import com.github.dnvriend.api.AclService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class AclServiceImpl extends AclService {
  override def access: ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful("access!!")
  }

  override def noaccess: ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful("noaccess!!")
  }
}
