/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.lib

trait StdLib_StateManagement{
  self: StandardLibrary.Subcomponent =>

  trait StateManagement {
    self: FieldCalculusSyntax with StandardSensors =>

    def remember[T](value: T): T =
      rep(value)(identity)

    def captureChange[T](x: T): Boolean = rep((x,false)) { case (value, _) =>
      (x, value != x)
    }._2

    /**
      * @return true when the given parameter goes from false to true (starting from false); false otherwise
      */
    def goesUp(value: Boolean): Boolean = rep((false, false)) { case (old, trigger) =>
      (value, value && value != old)
    }._2

    /**
      * @return true when the given parameter goes from true to false (starting from false); false otherwise
      */
    def goesDown(value: Boolean): Boolean = rep((false, false)) { case (old, trigger) =>
      (value, !value && value != old)
    }._2
  }
}
