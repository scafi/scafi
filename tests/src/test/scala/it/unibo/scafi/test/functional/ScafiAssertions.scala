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

package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest.Matchers

object ScafiAssertions extends Matchers {

  def assertForAllNodes[T](f: (ID,T) => Boolean, okWhenNotComputed: Boolean = false)
                          (implicit net: Network): Unit ={
    withClue("Actual network:\n" + net + "\n\n Sample exports:\n" + net.export(0) + "\n" + net.export(1)) {
      net.exports.forall {
        case (id, Some(e)) => f(id, e.root[T]())
        case (id, None) => okWhenNotComputed
      } shouldBe true
    }
  }

  /**
   * Asserts the value of only a subset of the nodes of the network.
   */
  def assertKnownNetworkValues[T](vals: Map[ID,Option[T]])(implicit net: Network): Unit ={
    vals.keys.forall(id => {
      val actualExport = net.export(id)
      var expected = vals(id)
      (actualExport, expected) match {
        case (Some(e), Some(v)) => e.root[T]() == v
        case (None, None) => true
        case _ => false
      }
    }) shouldBe true
  }

  /**
   * Asserts the value of all the nodes of the network.
   */
  def assertNetworkValues[T](vals: Map[ID, T],
                             customEq:Option[(T,T)=>Boolean] = None)
                            (implicit net: Network): Unit ={
    withClue("Actual network: " + net + "\n\n Sample exports:\n" + net.export(0) + "\n" + net.export(1) + "\n\nExpected values:"+vals) {
      net.ids.forall(id => {
        val actualExport = net.export(id)
        var expected = vals.get(id)
        (actualExport, expected) match {
          case (Some(e), Some(v)) => if(customEq.isDefined) customEq.get(e.root[T](), v)
            else e.root[T]() == v
          case (None, None) => true
          case (None, _) => false
          case _ => false
        }
      }) shouldBe true
    }
  }

  def assertNetworkValuesWithPredicate[T](pred: (ID,T)=>Boolean)
                                         (passNotComputed: Boolean = true)
                                         (implicit net: Network): Unit ={
    withClue("Actual network: " + net) {
      net.ids.forall(id => {
        val actualExport = net.export(id)
        actualExport match {
          case Some(v) => pred(id,v.root[T]())
          case None => passNotComputed
        }
      }) shouldBe true
    }
  }
}
