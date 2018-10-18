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

trait StdLib_DynamicCode {
  self: StandardLibrary.Subcomponent =>

  trait DynamicCode {
    self: FieldCalculusSyntax =>

    /**
      * Mobile functions `fun` should be transferable (i.e., no closures etc), aggregate functions.
      * - If they do note use `aggregate`, they might try to align with functions of other versions,
      *   causing unexpected behaviours and errors.
      */
    case class Fun[T,R](ver: Int, fun: (T)=>R)

    type Injecter[T,R] = () => Fun[T,R]

    /**
      * Enacts a simple gossip process that supports spreading updated versions of functions.
      * NOTE: different functions running in different parts of the system means that
      * the system is partitioned (by the function identity).
      */
    def up[T,R](injecter: Injecter[T,R]): Fun[T,R] = rep(injecter()){ case f =>
      foldhood(injecter())((f1,f2) => if(f1.ver>=f2.ver) f1 else f2)(nbr{f})
    }
  }

}
