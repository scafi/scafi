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

package it.unibo.scafi.simulation.old.gui

object SettingsSpace {
  object NbrHoodPolicies{
    val Euclidean = "Euclidean"
  }
  object ExecStrategies {
    val Random = "Random"
  }
  object Topologies {
    val Random = "Random"
    val Grid = "Grid"
    val Grid_LoVar = "Grid LoVar"
    val Grid_MedVar = "Grid MedVar"
    val Grid_HighVar = "Grid HiVar"
  }
  object ToStrings {
    val Default_Double = (a:Any)=> a match {
      case d: Double if d==Double.MaxValue => "inf"
      case d: Double if d==Double.MinValue => "-inf"
      case d: Double => f"$d%5.3f";
      case _ => "???"}
  }
}
