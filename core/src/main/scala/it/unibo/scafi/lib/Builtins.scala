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

trait StdLib_Builtins{
  self: StandardLibrary.Subcomponent =>
}

trait Builtins {
  this: Incarnation#Constructs =>

  def branch[A](cond: => Boolean)(th: => A)(el: => A): A =
    mux(cond)(() => aggregate{ th })(() => aggregate{ el })()

  def mux[A](cond: Boolean)(th: A)(el: A): A = if (cond) th else el

  def foldhoodPlus[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
    foldhood(init)(aggr)(mux(mid()==nbr(mid())){init}{expr})

  def minHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.top)((x, y) => of.min(x, y)){expr}
  def maxHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.bottom)((x, y) => of.max(x, y)){expr}

  def minHoodLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
    foldhood[A](default)((x, y) => if(poglb.equiv(x, y)) poglb.gle(x,y) else if(poglb.lt(x,y)) x else y){expr}

  def minHoodPlusLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
    foldhoodPlus[A](default)((x, y) => if(poglb.equiv(x, y)) poglb.gle(x,y) else if(poglb.lt(x,y)) x else y){expr}

  def minHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.top)((x, y) => of.min(x, y)){expr}
  def maxHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.bottom)((x, y) => of.max(x, y)){expr}
}