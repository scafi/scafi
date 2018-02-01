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

  /**
    * Process identifier
    * @param pid
    */
  case class PID(pid: String)

  /**
    * Process instance identifier
    * @param puid
    */
  case class PUID(puid: String)

  trait Spawn {
    self: FieldCalculusSyntax with StandardSensors with FieldUtils =>

    val TimeGC: Long = 20

    /**
      * Process definition.
      * A process has:
      * @param comp a behaviour (process logic)
      * @param genCondition a condition by which the process can be generated
      * @param limit a maximum spatial extension
      * @param metric a metric used to calculate its spatial extension
      * @param timeGC a time value for garbage-collection
      * @tparam T the type of the output of the process computation
      */
    case class ProcessDef[+T](pid: PID,
                              comp: () => T,
                              genCondition: () => Boolean,
                              stopCondition: () => Boolean = () => false,
                              limit: Double = Double.PositiveInfinity,
                              metric: () => Double = nbrRange,
                              timeGC: Long = TimeGC)

    /**
      * Process data
      * @param value Value resulting from the process computation
      * @param gen Flag indicating if the data belongs to the generator of the process instance
      * @param stop Flag indicating if the process is being stopped
      * @param counter Counter indicating progress in process instance execution
      * @param distance Distance from the source (i.e., the generator) of the process instance
      * @param staleValue Values representing how many rounds/time have passed without seeing a new counter value
      * @tparam T the type of the output of the process computation
      */
    case class ProcessData[+T](value: Option[T] = None,
                              gen: Boolean = false,
                              stop: Boolean = false,
                              counter: Option[Long] = None,
                              distance: Double = Double.PositiveInfinity,
                              staleValue: Long = 0)

    /**
      * Process instance
      * @param pid UID for the process instance
      * @param process The process from which this instance comes from
      * @param data Data associated with this process instance
      * @tparam T the type of the output of the process computation
      */
    case class ProcessInstance[T](pid: PUID,
                                  process: ProcessDef[T],
                                  data: ProcessData[T] = ProcessData[T]()){
      def passToNeighbour = updateData(this.data.copy(gen = false))
      def updateData(pdata: ProcessData[T]) = this.copy(data = pdata)
    }

    def nextGeneratedProcessNum: Long = align("round_counting"){ _ => rep(0L)(_ + 1) }
    def generatePUID(p: ProcessDef[_]): PUID = PUID(s"pid_${mid}_${p.pid.pid}_${nextGeneratedProcessNum}")
    def processConditionScope(pid: PID): String = s"pcond_${pid}"
    def processInstanceScope(pid: PUID): String = s"pexec_${pid}"
    val processComputationScope = "process_computation"
    def toBeGenerated(p: ProcessDef[_]): Boolean =
      align(processConditionScope(p.pid)){ _ => p.genCondition() }

    def chooseByMin[T,V:Ordering](projection: T => V): (T,T) => T =
      (t1,t2) => if(implicitly[Ordering[V]].lt(projection(t1), projection(t2))) t1 else t2

    def nbrProcessWithinLimits(p: ProcessInstance[_]): Boolean =
      p.data.distance + p.process.metric() <= p.process.limit

    def processManagement[T](processDefs: Set[ProcessDef[T]]): Map[PUID,ProcessInstance[T]] = {
      rep(Map[PUID,ProcessInstance[T]]())(currProcs => {
        // 1. Select process instances extended up to me from neighbours;
        //    when more neighbours run the same process, priority is given to the one closer to the process source
        val nbrProcs = excludingSelf.mergeHood {
          nbr(currProcs).filter{ case (_,p) => nbrProcessWithinLimits(p) }
        }(overwritePolicy = chooseByMin(_.data.distance)).mapValues(_.passToNeighbour)

        // 2. New processes to be spawn, based on a generation condition
        val newProcs = processDefs.iterator.filter(toBeGenerated(_))
          .map(pd => ProcessInstance(generatePUID(pd), pd, ProcessData(gen = true))).map(pi => pi.pid -> pi).toMap

        // 3. Collect all process instances to be executed, execute them and update their state
        (nbrProcs ++ currProcs ++ newProcs).mapValues(p => p.updateData(runProcessInstance(p).data))
      })
    }

    def processExecution[T](processDefs: Set[ProcessDef[T]]): Map[PUID,T] =
      processManagement(processDefs)
        .collect { case (pid, ProcessInstance(_,_,ProcessData(Some(v),_,_,_,_,_))) => pid -> v }

    def runProcessInstance[T](p: ProcessInstance[T]): ProcessInstance[T] = {
      def processComputation: Option[T] = align(processComputationScope){ _ => Some(p.process.comp()) }
      val stop = align(s"stop_${p.pid}"){ _ => p.process.stopCondition() }
      ProcessInstance(p.pid, p.process, align(processInstanceScope(p.pid)){ _ =>  // enters the eval context for process of given pid
        rep(p.data){ data =>
          mux(data.gen){ // Generator node up to previous round
            ProcessData(
              value = if(data.gen && !data.stop){ processComputation } else { None },
              gen = data.gen,
              stop = data.stop || stop,
              counter = if(data.gen && !data.stop) Some(data.counter.map(_ + 1).getOrElse(0)) else None,
              distance = 0.0)
          }{ // Non-generator node
            excludingSelf.minHoodSelector[Double,(Double,Option[Long])]{ nbr(data.distance) }{
              (nbr(data.distance) + nbrRange, nbr(data.counter))
            }.map {
              case (newDist, newCount) if newCount.isDefined && newDist <= p.process.limit && data.staleValue < p.process.timeGC =>
                data.copy(
                  value = processComputation,
                  gen = false,
                  counter = newCount,
                  distance = newDist,
                  staleValue = if(newCount == data.counter) data.staleValue + 1 else 0)
              case (newDist, _) => ProcessData[T](distance = newDist) // Keep distance but resets other fields
            }.getOrElse { ProcessData[T]() }
          }
        }
      })
    }
  }
}
