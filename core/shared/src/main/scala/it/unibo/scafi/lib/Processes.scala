/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

// scalastyle:off number.of.methods number.of.types
trait StdLibProcesses {
  self: StandardLibrary.Subcomponent =>

  trait SpawnInterface {
    def spawn[A, B, C](process: A => B => (C, Boolean), params: Set[A], args: B): Map[A,C]
  }

  object SpawnInterface {
    trait Status

    case object ExternalStatus extends Status   // External to the bubble
    case object BubbleStatus extends Status     // Within the bubble
    case object OutputStatus extends Status     // Within the bubble and bubble output producer
    case object TerminatedStatus extends Status // Notifies the willingness to terminate the bubble
    final case class GeneratorStatus[K](newProcs: Set[K]) extends Status

    val External: Status = ExternalStatus
    val Bubble: Status = BubbleStatus
    val Output: Status = OutputStatus
    val Terminated: Status = TerminatedStatus
  }

  trait CustomSpawn extends SpawnInterface with FieldUtils {
    self: AggregateProgram =>

    import SpawnInterface._

    final case class ProcInstance[A, B, C](params: A)(val proc: A => B => C, val value: Option[C] = None)
    {
      def run(args: B): ProcInstance[A,B,C] =
        ProcInstance(params)(proc, { align(puid) { _ => Some(proc.apply(params)(args)) } })

      override def toString: String =
        s"{params:($params), val:($value)}"

      val puid: String = s"procInstance_${params.hashCode()}"
    }

    def exportConditionally[R](f: => (R, Boolean)): (R, Boolean) = {
      vm.newExportStack
      val result = f
      if(result._2) vm.mergeExport else vm.discardExport
      result
    }

    def runOnSharedKeysWithShare[K, A, R](process: K => (R, Boolean), params: Set[K]): Map[K,R] =
      share(Map.empty[K, R])((loc,nbr) => {
        (includingSelf.unionHoodSet(nbr().keySet ++ params))
          .mapToValues(x => exportConditionally(process.apply(x)))
          .collectValues[R] { case (r,true) => r }
      })

    def runOnSharedKeys[K, A, R](process: K => (R, Boolean), params: Set[K]): Map[K,R] =
      rep(Map.empty[K, R])(map => {
        (includingSelf.unionHoodSet(nbr{map}.keySet ++ params))
          .mapToValues(process.apply(_))
          .collectValues[R] { case (r,true) => r }
      })

    def spawn2[K, A, R](process: K => A => (R, Boolean), params: Set[K], args: A): Map[K,R] =
      runOnSharedKeysWithShare(align(_){process(_)(args)}, params)

    def spawn[K, A, R](process: K => A => (R, Boolean), params: Set[K], args: A): Map[K,R] = {
      rep(Map.empty[K, R]) { case map => {
        // 1. Take active process instances from my neighbours
        val nbrProcs = includingSelf.unionHoodSet(nbr{map}.keySet)

        // 2. New processes to be spawn, based on a generation condition
        val newProcs = params

        // 3. Collect all process instances to be executed, execute them and update their state
        (nbrProcs ++ newProcs)
          .mapToValues { k =>
            vm.newExportStack
            val result = align(k)(process(_)(args))
            if (result._2) vm.mergeExport else vm.discardExport
            result
          }.collect { case(pid,res) if res._2 => pid -> res._1 }.toMap
      } }
    }

    class Spawn[K,A,R](process: K => A => POut[R], generation: => Set[K], regulation: => A){
      def apply(): Map[K,R] = spawn(process, generation, regulation)
      def map[T](fm: POut[R] => POut[T]): Spawn[K,A,T] = new Spawn[K,A,T](k => a => fm(process(k)(a)), generation, regulation)
    }
    object Spawn {
      def apply[K,A,R](process: K => A => POut[R], generation: => Set[K], regulation: => A): Map[K,R] =
        new Spawn[K,A,R](process, generation, regulation).apply()
    }
    trait SpawnFilter[K,A,R] { self: Spawn[K,A,R] =>
      override def apply(): Map[K,R] = self.map(handleOutput).apply().collectValues { case Some(p) => p }
    }
    trait WithTermination[K,A,R] { self: Spawn[K,A,R] =>
      override def apply(): Map[K,R] = self.map(handleTermination).apply()
    }
    trait WithGeneration[K,A,R] { self: Spawn[K,A,R] =>
      override def apply(): Map[K,R] = rep(Set.empty[K], Map.empty[K,R]) { case (keys, res) =>
        val out = self.map {
          case POut(v, s: GeneratorStatus[K]) => POut[(R,Set[K])]((v, s.newProcs), s)
          case POut(v,s) => POut[(R,Set[K])]((v, Set.empty[K]),s)
        }.apply()
        (out.flatMap(_._2._2).toSet, out.mapValues(_._1).toMap)
      }._2
    }

    implicit class RichSet[K](val set: Set[K]){
      def mapToValues[V](f: K => V) : Map[K,V] =
        set.map(k => k -> f(k)).toMap

      def mapAndFilter[V](f: K => Option[V]): Map[K,V] =
        set.foldLeft(Map.empty[K,V]) { (m,key) =>
          f(key).map(v => m + (key -> v)).getOrElse(m)
        }
    }

    implicit class RichMap[K,V](val map: Map[K,V]){
      def collectValues[T](pf: PartialFunction[V,T]): Map[K,T] =
        map.collect { case (k,v) if pf.isDefinedAt(v) => (k,pf(v)) }

      def filterValues(pred: V => Boolean): Map[K,V] =
        map.filter { case (k,v) => pred(v) }

      def mapValuesStrict[U](mapLogic: V => U): Map[K,U] =
        map.map { case (k,v) => k -> mapLogic(v) }
    }

    final case class POut[T](result: T, status: Status)
    object POut {
      implicit def fromTuple[T](tp: (T,Status)): POut[T] = POut(tp._1, tp._2)
      implicit def toBasicSpawnTuple[T](pout: POut[T]): (T,Boolean) = (pout.result, pout.status!=External)
      implicit def toBasicSpawnLogic[K,A,R](proc: K => A => POut[R]): K => A => (R, Boolean) = k => a => toBasicSpawnTuple(proc(k)(a))
    }

    def handleTerminationWithRep[T](out: POut[T]): POut[T] = {
      rep[(Boolean,Int,POut[T])]((false,0,out)){
        case (terminated,k,res) =>
          val mustTerminate = out.status==Terminated | includingSelf.anyHood(nbr{terminated})
          val mustExit = includingSelf.everyHood(nbr{mustTerminate})
          (mustTerminate, 1, if(mustExit || (mustTerminate && k==0)) (out.result, External) else out)
      }._3
    }

    def handleTermination[T](out: POut[T]): POut[T] = {
      share[(Boolean,Int,POut[T])]((false,0,out)){
        case (loc,nbrd) =>
          val mustTerminate = out.status==Terminated | includingSelf.anyHood(nbrd()._1)
          val mustExit = includingSelf.everyHood(nbr{mustTerminate})
          (mustTerminate, 1, if(mustExit || (mustTerminate && loc._2==0)) POut(out.result, External) else out)
      }._3
    }

    def handleOutput[T](out: POut[T]): POut[Option[T]] = out match {
      case POut(res, Output) => POut(Some(res), Output)
      case POut(_, s) => POut(None, s)
    }

    implicit class ProcessLogic[K,A,R](proc: (K => A => POut[R])) {
      def map[T](f: POut[R] => POut[T]): (K => A => POut[T]) = k => a => f(proc(k)(a))
    }

    def sspawn[K, A, R](process: K => A => POut[R], params: Set[K], args: A): Map[K,R] =
      spawn2[K,A,Option[R]](k => a => handleOutput(handleTermination(process(k)(a))), params, args)
        .collectValues { case Some(p) => p }

    def sspawn2[K, A, R](process: K => A => POut[R], params: Set[K], args: A): Map[K,R] =
      spawn2[K,A,Option[R]](process.map(handleTermination).map(handleOutput), params, args)
        .collectValues { case Some(p) => p }

    def sspawnOld[A, B, C](process: A => B => (C, Status), params: Set[A], args: B): Map[A,C] = {
      spawn[A,B,Option[C]]((p: A) => (a: B) => {
        val (finished, result, status) = rep((false, none[C], false)) { case (finished, _, _) => {
          val (result, status) = process(p)(a)
          val newFinished = status == Terminated | includingSelf.anyHood(nbr{finished})
          val terminated = includingSelf.everyHood(nbr{newFinished})
          val (newResult, newStatus) = (result, status) match {
            case _ if terminated     => (None, false)
            case (_,     External)   => (None, false)
            case (_,     Terminated) => (None, true)
            case (value, Output)     => (Some(value), true)
            case (_,     Bubble)     => (None, true)
          }
          (newFinished, newResult, newStatus)
        } }
        (result, status)
      }, params, args).collect { case (k, Some(p)) => k -> p }
    }

    def processManager[K,A,R](process: K => A => R,
                              generation: () => Set[K],
                              termination: (K,A,R) => Boolean): A => Map[K,R] =
      spawn((k:K) => (a:A) => { val r = process(k)(a); (r,!termination(k,a,r)) }, generation(), _)

    object On {
      def apply[K](set: Set[K]): SpawnKeys[K] = new SpawnKeys(set)
    }
    class SpawnKeys[K](val keys: Set[K]) {
      def withArgs[Args](args: Args): SpawnContinuation[K,Args] = new SpawnContinuation(keys, args)
    }
    class SpawnContinuation[K,Args](val keys: Set[K], val args: Args){
      def spawn[R](proc: K => Args => (R,Status)): Map[K,R] =
        csspawn(proc, keys, args)
    }

    /**********************************************
      *************** COOMPACT SPAWN **************
      *********************************************/

    trait MapFilter[V] {
      def value: V
      def filter: Boolean
    }

    final case class SpawnReturn[C](value: C, status: Boolean) extends MapFilter[C] {
      override def filter: Boolean = status
    }

    // "Compact" spawn
    def cspawn[Key, Args, R](process: Key => Args => SpawnReturn[R], newProcesses: Set[Key], args: Args): Map[Key,R] =
      spreadKeys[Key,R](newProcesses){ key => process(key)(args) }

    def spreadKeys[K,R](newKeys: Set[K])(mapKey: K => MapFilter[R]): Map[K,R] =
      share(Map.empty[K,R]) { case (_, nbrMaps) =>
        (includingSelf.unionHoodSet(nbrMaps().keySet) ++ newKeys).mapAndFilter[R]{ (key: K) =>
          simplyReturn(alignedExecution(mapKey)(key)).filteringExport.iff(_.filter).map(_.value)
        }
      }

    def cuspawn[K, A, R](process: K => A => SpawnReturn[R], newKeys: Set[K], args: A): Map[K,R] =
      share(Map.empty[K,R]) { case (_, nbrMaps) =>
        (includingSelf.unionHoodSet(nbrMaps().keySet) ++ newKeys).mapAndFilter[R]{ (key: K) =>
          simplyReturn(alignedExecution(process(_:K)(args))(key)).filteringExport.iff(_.filter).map(_.value)
        }
      }

    def alignedExecution[K,V](p: K => V)(key: K): V =
      align(s"${p.getClass.getName}_${key.hashCode}"){ _ => p(key) }

    def run[A,B,C](proc: A => B => SpawnReturn[C], params: A, args: B): SpawnReturn[C] =
      align(s"process_${params.hashCode}") { _ => proc(params)(args) }

    class IffContinuation[T](expr: => T){
      var filterExport: Boolean = false

      def filteringExport: IffContinuation[T] =
        new IffContinuation[T](expr){
          filterExport = true
        }

      def iff(pred: T => Boolean): Option[T] = {
        if(filterExport) vm.newExportStack
        val result = expr
        if(pred(result)){
          if(filterExport) vm.mergeExport
          Some(result)
        } else {
          if(filterExport) vm.discardExport
          None
        }
      }
    }

    def simplyReturn[T](expr: => T): IffContinuation[T] = new IffContinuation[T](expr)

    // "Compact" "status" spawn
    def csspawn[A, B, C](process: A => B => (C, Status), params: Set[A], args: B): Map[A,C] = {
      cspawn[A,B,Option[C]]((p: A) => (a: B) => {
        val (finished, result, status) = rep((false, none[C], false)) { case (finished, _, _) => {
          val (result, status) = process(p)(a)
          val newFinished = status == Terminated | includingSelf.anyHood(nbr{finished})
          val terminated = includingSelf.everyHood(nbr{newFinished})
          val SpawnReturn(newResult, newStatus) = (result, status) match {
            case _ if terminated     => SpawnReturn(None, false)
            case (_,     External)   => SpawnReturn(None, false)
            case (_,     Terminated) => SpawnReturn(None, true)
            case (value, Output)     => SpawnReturn(Some(value), true)
            case (_,     Bubble)     => SpawnReturn(None, true)
          }
          (newFinished, newResult, newStatus)
        } }
        SpawnReturn(result, status)
      }, params, args).collect { case (k, Some(p)) => k -> p }
    }

    /**********************************************
      ******************* UTILS *******************
      *********************************************/

    private def none[T]: Option[T] = None
  }

  trait ReplicatedGossip extends CustomSpawn with FieldCalculusSyntax with StandardSensors with TimeUtils with StateManagement {
    self: AggregateProgram =>

    def replicated2[T, R](proc: T => R)(argument: T, period: Double, numReplicates: Int): Map[Long,R] = {
      val lastPid = sharedTimerWithDecay(period, deltaTime().length).toLong
      processManager[Long, T, R]((pid: Long) => proc(_),
        generation = () => if (captureChange(lastPid)) Set(lastPid) else Set.empty[Long],
        termination = (pid, arg, res) => pid < lastPid - numReplicates
      )(argument)
    }

    def replicated[T,R](proc: T => R)(argument: T, period: Double, numReplicates: Int): Map[Long,R] = {
      val lastPid = sharedTimerWithDecay(period, deltaTime().length).toLong
      val newProcs = Set(lastPid) // if(captureChange(lastPid)) Set(lastPid) else Set[Long]()
      sspawn[Long,T,R]((pid: Long) => (arg) => {
        (proc(arg), if(pid > lastPid - numReplicates){ SpawnInterface.Output } else { SpawnInterface.External })
      }, newProcs, argument)
    }
  }

  /**
    * Example:
    * spawn
    *   .where(mid == 2)
    *   .every(30, dt())
    *   .generateKeys[Long](k => List(k))
    *   .run[Int,Int]{
    *     rep(_)(_+1)
    *   }.withArgs(1000)
    */
  trait ProcessDSL {
    self: AggregateProgram with FieldUtils with CustomSpawn with TimeUtils with StateManagement with StandardSensors =>

    import SpawnInterface._

    def replicated[T,R](proc: T => R)(argument: T, period: Double, numReplicates: Int): Map[Long,R] = {
      val lastPid = sharedTimerWithDecay(period, deltaTime().length).toLong
      val newProcs = if(captureChange(lastPid)) Set(lastPid) else Set.empty[Long]
      sspawn[Long,T,R]((pid: Long) => (arg) => {
        (proc(arg), if(lastPid - pid < numReplicates){ Output } else { External })
      }, newProcs, argument)
    }

    trait GenerationInSpace {
      def where(pred: Boolean): GenerationInSpaceContinuation

      def inNode(id: ID): GenerationInSpaceContinuation = where(mid==id)
      def inNodes(ids: ID*): GenerationInSpaceContinuation = where(ids.contains(mid))
    }

    trait GenerationInTime {
      def when(pred: Boolean): GenerationInTimeContinuation

      def after(delay: Double, dt: Double): GenerationInTimeContinuation = when(T(delay, dt)==0)
      def every(period: Double, dt: Double): GenerationInTimeContinuation = when(cyclicTimerWithDecay(period, dt))

      def once: GenerationInTimeContinuation = after(0, 0)
    }

    trait KeyGenerator {
      def generateKeys[K](k: Long => List[K]): KeyGeneratorContinuation[K]
    }

    class GenerationInSpaceContinuation(val inSpace: Boolean) extends GenerationInTime {
      override def when(pred: Boolean): GenerationInTimeContinuation = new GenerationInTimeContinuation(inSpace & pred)
    }

    class GenerationInTimeContinuation(val inSpaceTime: Boolean) extends KeyGenerator {
      override def generateKeys[K](kgen: Long => List[K]): KeyGeneratorContinuation[K] =
        new KeyGeneratorContinuation[K](inSpaceTime, kgen(rep(0L){ k => if(inSpaceTime) k + 1 else k }))
    }

    trait ProcGenerator[K] {
      def run[A,R](proc: A => R): ProcContinuation[K,A,R]
    }

    class KeyGeneratorContinuation[K](inSpaceTime: Boolean, keys: List[K]) extends ProcGenerator[K] {
      override def run[A, R](proc: (A) => R): ProcContinuation[K, A, R] =
        new ProcContinuation[K,A,R](keys.toSet, proc)
    }

    object DoSpawn extends GenerationInSpace {
      override def where(pred: Boolean): GenerationInSpaceContinuation = new GenerationInSpaceContinuation(pred)
    }

    class ProcContinuation[K,A,R](keys: Set[K], proc: A => R) {
      def withArgs(args: A): Map[K,R] = spawn[K,A,R](k => a => (proc(a),true), keys, args)
    }
  }
}
