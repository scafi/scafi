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

trait Stdlib_Processes {
  self: StandardLibrary.Subcomponent =>

  trait Spawn {
    self: FieldCalculusSyntax with StandardSensors with FieldUtils =>

    val TimeGC: Long = 20

    case class SpawnDef[T](pid: Int,
                           comp: () => T,
                           genCondition: () => Boolean,
                           limit: Double = Double.PositiveInfinity,
                           metric: () => Double = nbrRange,
                           timeGC: Long = TimeGC)

    case class SpawnData[T](value: Option[T] = None,
                            gen: Boolean = false,
                            counter: Option[Long] = None,
                            distance: Double = Double.PositiveInfinity,
                            staleValue: Long = 0)

    def spawn[T](p: SpawnDef[T]): SpawnData[T] = {
      val isGen = p.genCondition()
      def processComputation: Option[T] = align("process_computation"){ _ => Some(p.comp()) }
      align(p.pid){ _ =>  // enters the eval context for process of given pid
        rep(SpawnData[T](gen = isGen)){ data =>
          mux(isGen || data.gen){ // Generator node up to previous round
            SpawnData(
              value = if(isGen){ processComputation } else { None },
              gen = isGen,
              counter = if(isGen) Some(data.counter.map(_ + 1).getOrElse(0)) else None,
              distance = 0.0)
          }{ // Non-generator node
            excludingSelf.minHoodSelector[Double,(Double,Option[Long])]{ nbr(data.distance) }{
              (nbr(data.distance) + nbrRange, nbr(data.counter))
            }.map {
              case (newDist, newCount) if newCount.isDefined && newDist <= p.limit && data.staleValue < p.timeGC =>
                data.copy(
                  value = processComputation,
                  gen = false,
                  counter = newCount,
                  distance = newDist,
                  staleValue = if(newCount == data.counter) data.staleValue + 1 else 0)
              case (newDist, _) => SpawnData[T](distance = newDist) // Keep distance but resets other fields
            }.getOrElse { SpawnData[T]() }
          }
        }
      }
    }
  }
}
