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

package serializer

import scala.xml.NodeSeq

trait XmlFormat[A] extends XmlMarshaller[A] with XmlUnmarshaller[A]
trait XmlMarshaller[A] {
  def marshal(value: A): NodeSeq
}
trait XmlUnmarshaller[A] {
  def unmarshal(xml: NodeSeq): A
}