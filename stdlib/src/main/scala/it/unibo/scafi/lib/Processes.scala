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
    * Process instance identifier
    * @param puid
    */
  case class PUID(puid: String)

  trait Spawn {
    self: FieldCalculusSyntax with StandardSensors with FieldUtils =>
    import excludingSelf._ // Here, fold operations by default only look at neighbours (i.e., not myself)

    val TimeGC: Long = 20

    case class ProcessGenerator[T](id: Int,
                                   trigger: () => Boolean,
                                   generator: (PUID) => ProcessInstance[T]){
      def checkTrigger: Boolean = align("check_trigger_" + id){ _ => trigger() }
      def generate: ProcessInstance[T] = {
        val puid = generatePUID(id)
        align("generator_" + id){ _ => generator(puid) }
      }
    }

    /**
      * Process definition.
      * A process has:
      * @param comp a behaviour (process logic)
      * @param limit a maximum spatial extension
      * @param metric a metric used to calculate its spatial extension
      * @param timeGC a time value for garbage-collection
      * @tparam T the type of the output of the process computation
      */
    case class ProcessDef[+T](comp: () => T,
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
      def forNonGenerator = updateData(this.data.copy(gen = false))
      def forGenerator = updateData(this.data.copy(gen = true))
      def updateData(pdata: ProcessData[T]) = this.copy(data = pdata)

      def compute: Option[T] = align(s"pcomp"){ _ => Some(process.comp()) }
      def evaluateStopCondition: Boolean = align(s"pstopeval_${pid.puid}"){ _ => process.stopCondition() }
    }

    def nextGeneratedProcessNum: Long = align("round_counting"){ _ => rep(0L)(_ + 1) }
    def generatePUID(pid: Int): PUID = PUID(s"pid_${mid}_${pid}_${nextGeneratedProcessNum}")

    def chooseByMin[T,V:Ordering](projection: T => V): (T,T) => T =
      (t1,t2) => if(implicitly[Ordering[V]].lt(projection(t1), projection(t2))) t1 else t2

    def processWithinLimits(p: ProcessInstance[_]): Boolean =
      p.data.distance + p.process.metric() <= p.process.limit

    def processManagement[T](generators: Set[ProcessGenerator[T]]): Map[PUID,ProcessInstance[T]] = {
      rep(Map[PUID,ProcessInstance[T]]())(currProcs => {
        // 1. Select process instances extended up to me from neighbours;
        //    when more neighbours run the same process, priority is given to the one closer to the process source
        val nbrProcs = mergeHoodFirst {
          nbr(currProcs).filterValues(processWithinLimits(_))
        }.mapValues(_.forNonGenerator)

        // 2. New processes to be spawn, based on a generation condition
        val newProcs = generators.view.filter(_.checkTrigger).map(_.generate).map(pi => pi.pid -> pi.forGenerator)

        // 3. Collect all process instances to be executed, execute them and update their state
        (nbrProcs ++ currProcs ++ newProcs).map{ case (pid,p) => pid -> p.updateData(runProcessInstance(p).data) }
      })
    }

    def processExecution[T](generators: Set[ProcessGenerator[T]]): Map[PUID,T] =
      processManagement(generators)
        .collect { case (pid, ProcessInstance(_,_,ProcessData(Some(v),_,_,_,_,_))) => pid -> v }

    def runProcessInstance[T](p: ProcessInstance[T]): ProcessInstance[T] = {
      ProcessInstance(p.pid, p.process, align(s"pexec_${p.pid.puid}"){ _ =>  // enters the eval context for process of given pid
        rep(p.data){ data =>
          mux(data.gen){ // Generator node up to previous round
            ProcessData(
              value = if(data.gen && !data.stop){ p.compute } else { None },
              gen = data.gen,
              stop = data.stop || p.evaluateStopCondition,
              counter = if(data.gen && !data.stop) Some(data.counter.map(_ + 1).getOrElse(0)) else None,
              distance = 0.0)
          }{ // Non-generator node
            minHoodSelector[Double,(Double,Option[Long])]{ nbr(data.distance) }{
              (nbr(data.distance) + nbrRange, nbr(data.counter))
            }.map {
              case (newDist, newCount) if newCount.isDefined && newDist <= p.process.limit && data.staleValue < p.process.timeGC =>
                data.copy(
                  value = p.compute,
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

    private implicit class RichMap[K,V](val m: Map[K,V]){
      def filterValues(pred: V => Boolean): Map[K,V] =
        m.filter{ case (k:K,v:V) => pred(v) }
    }
  }
}
