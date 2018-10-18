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

trait StdLib_Processes {
  self: StandardLibrary.Subcomponent =>

  /**
    * Process (kind) identifier
    * @param pid
    */
  case class PID(pid: String){
    override def toString: String = s"pid$pid"
  }

  /**
    * Process instance identifier
    * @param puid
    */
  case class PUID(puid: String){
    override def toString: String = s"puid$puid"
  }

  trait HFCSpawn extends FieldUtils {
    self: FieldCalculusSyntax =>

    trait Status
    case object External extends Status // External to the bubble
    case object Bubble extends Status   // Within the bubble
    case object Output extends Status   // Within the bubble and bubble output producer

    type Proc[A,B,C] = A => B => (C,Status)

    case class ProcInstance[A,B,C](puid: PUID)(val params: A, val proc: Proc[A,B,C], val value: Option[(C,Status)] = None){
      def run(args: B) = ProcInstance(puid)(params, proc, align(puid){ _ => Some(proc.apply(params)(args)) })
    }

    def spawn[A,B,C](process: Proc[A,B,C], params: List[A], args: B): Iterable[C] = {
      rep((0,Map[PUID,ProcInstance[A,B,C]]())){ case (k, currProcs) => {
        // 1. Take previous processes (from me and from my neighbours)
        val nbrProcs = excludingSelf.mergeHoodFirst( nbr(currProcs) )

        // 2. New processes to be spawn, based on a generation condition
        val newProcs = params.zipWithIndex.map { case (arg, i) => {
          val id = PUID(s"${mid}_${k + i}")
          val newProc = ProcInstance(id)(arg, process)
          id -> newProc
        }}.toMap

        // 3. Collect all process instances to be executed, execute them and update their state
        (k + params.length, (currProcs ++ nbrProcs ++ newProcs)
          .mapValuesStrict(p => p.run(args))
          .filterValues(_.value.get._2 != External))
      }}._2.collect { case (_,p) if p.value.get._2==Output => p.value.get._1 }
    }
  }

  trait Processes {
    self: FieldCalculusSyntax with StandardSensors with FieldUtils =>
    import excludingSelf._ // Here, fold operations by default only look at neighbours (i.e., not myself)

    val TimeGC: Long = 20

    case class ProcessGenerator[T](trigger: () => Boolean,
                                   generator: () => ProcessDef[T]){

      private var k = 0
      def nextGeneratedProcessNum: Int = { k += 1; k }

      def checkTrigger: Boolean = align("check_trigger_" + hashCode()){ _ => trigger() }
      def generate: ProcessInstance[T] = {
        val pdef = generator()
        val puid = PUID(s"${mid}_${pdef.pid.pid}_${nextGeneratedProcessNum}")
        align("generator_" + hashCode()){ _ => ProcessInstance[T](puid, pdef) }
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
    case class ProcessDef[+T](pid: PID,
                              comp: () => T,
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
      * @param puid UID for the process instance
      * @param process The process from which this instance comes from
      * @param data Data associated with this process instance
      * @tparam T the type of the output of the process computation
      */
    case class ProcessInstance[T](puid: PUID,
                                  process: ProcessDef[T],
                                  data: ProcessData[T] = ProcessData[T]()){
      def forNonGenerator: ProcessInstance[T] = updateData(this.data.copy(gen = false))
      def forGenerator: ProcessInstance[T] = updateData(this.data.copy(gen = true))
      def updateData(pdata: ProcessData[T]): ProcessInstance[T] = this.copy(data = pdata)

      def compute: Option[T] = align(s"pcomp"){ _ => Some(process.comp()) }
      def evaluateStopCondition: Boolean = align(s"pstopeval_${puid}"){ _ => process.stopCondition() }

      def run: ProcessInstance[T] = updateData(align(s"pexec_${puid}"){ _ =>  // enters the eval context for process of given pid
        rep(data){ data =>
          mux(data.gen){ // Generator node up to previous round
            ProcessData(
              value = if(data.gen && !data.stop){ compute } else { None },
              gen = data.gen,
              stop = data.stop || evaluateStopCondition,
              counter = if(data.gen && !data.stop) Some(data.counter.map(_ + 1).getOrElse(0)) else None,
              distance = 0.0)
          }{ // Non-generator node
            minHoodSelector[Double,(Double,Option[Long])]{ nbr(data.distance) }{
              (nbr(data.distance) + nbrRange, nbr(data.counter))
            }.map {
              case (newDist, newCount) if newCount.isDefined && newDist <= process.limit && data.staleValue < process.timeGC =>
                data.copy(
                  value = compute,
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

    def processExecution[T](generators: ProcessGenerator[T]*): Map[PUID,T] =
      processExecution(generators.toSet)

    def processExecution[T](generators: Set[ProcessGenerator[T]]): Map[PUID,T] =
      processManagement(generators)
        .collect { case (pid, ProcessInstance(_,_,ProcessData(Some(v),_,_,_,_,_))) => pid -> v }

    def processManagement[T](generators: Set[ProcessGenerator[T]]): Map[PUID,ProcessInstance[T]] = {
      rep(Map[PUID,ProcessInstance[T]]())(currProcs => {
        // 1. Select process instances extended up to me from neighbours;
        //    when more neighbours run the same process, priority is given to the one closer to the process source
        val nbrProcs = mergeHoodFirst { nbr(currProcs).filterValues(processWithinLimits(_)) }.mapValues(_.forNonGenerator)

        // 2. New processes to be spawn, based on a generation condition
        val newProcs = generators.view.filter(_.checkTrigger).map(_.generate).map(pi => pi.puid -> pi.forGenerator)

        // 3. Collect all process instances to be executed, execute them and update their state
        (nbrProcs ++ currProcs ++ newProcs).mapValuesStrict(_.run)
      })
    }

    private def chooseByMin[T,V:Ordering](projection: T => V): (T,T) => T =
      (t1,t2) => if(implicitly[Ordering[V]].lt(projection(t1), projection(t2))) t1 else t2

    private def processWithinLimits(p: ProcessInstance[_]): Boolean =
      p.data.distance + p.process.metric() <= p.process.limit
  }

  private implicit class RichMap[K,V](val m: Map[K,V]){
    def filterValues(pred: V => Boolean): Map[K,V] =
      m.filter { case (k,v) => pred(v) }

    def mapValuesStrict[U](mapLogic: V => U): Map[K,U] =
      m.map { case (k,v) => k -> mapLogic(v) }
  }
}
