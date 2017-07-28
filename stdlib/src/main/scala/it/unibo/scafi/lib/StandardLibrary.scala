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

import it.unibo.scafi.incarnations.Incarnation

trait StandardLibrary extends
         Stdlib_BlockG
    with Stdlib_BlockC
    with Stdlib_BlockT
    with Stdlib_BlockT2
    with Stdlib_BlockS
    with Stdlib_BlocksWithGC
    with Stdlib_BuildingBlocks
    with FieldUtils
    with Stdlib_TypeClasses { self: Incarnation => }

object StandardLibrary {
  type Subcomponent = StandardLibrary with Incarnation
}
