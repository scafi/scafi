/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib

import it.unibo.utils.{Interop, Linearizable}
import play.api.libs.json.{Reads, Writes}

trait BasePlatform {
  type UID
  type LSensorName
  type NSensorName
  type ComputationContext <: ComputationContextContract
  type ComputationExport <: ComputationExportContract
  type DataFactory <: DataFactoryContract
  type Program <: ProgramContract

  implicit val dataFactory: DataFactory

  implicit val linearUID: Linearizable[UID]
  implicit val interopUID: Interop[UID]

  val platformSerializer: PlatformSerializer

  trait PlatformSerializer {
    implicit val readsUid: Reads[UID]
    implicit val writesUid: Writes[UID]
    implicit val readsLsns: Reads[LSensorName]
    implicit val writesLsns: Writes[LSensorName]
    implicit val readsNsns: Reads[NSensorName]
    implicit val writesNsns: Writes[NSensorName]
    implicit val readsExp: Reads[ComputationExport]
    implicit val writesExp: Writes[ComputationExport]
  }

  trait ComputationContextContract {
    /*
    def id: UID
    def export(nbr: UID): ComputationExport
    def localSense(name: LSensorName): Any
    def aggregateSense(name: NSensorName)(nbr: UID): Any
    */
  }

  trait ComputationExportContract {
    def root[T](): T
  }

  trait DataFactoryContract {
    def context(id: UID,
                exports: Map[UID,ComputationExport],
                lsns: Map[LSensorName,Any],
                nsns: Map[NSensorName,Map[UID,Any]]): ComputationContext
  }

  trait ProgramContract {
    def round(ctx: ComputationContext): ComputationExport
  }
}

/**
 * This component defines a distributed platform and in particular:
 *   - A Façade API for the configuration, setup, and execution of distributed systems
 *   - A corpus of settings and defaults for distributed aggregate systems
 *   - Some general, utility members (string representations for types such as ID, LSNS,..),
 *     used for example in the command-line parser.
 */

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformSettings

object Platform {
  type Subcomponent = Platform
}
