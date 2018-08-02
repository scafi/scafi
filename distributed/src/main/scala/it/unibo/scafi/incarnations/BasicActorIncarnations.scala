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

package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.p2p.{Platform => P2pActorPlatform}
import it.unibo.scafi.distrib.actor.server.{Platform => ServerBasedActorPlatform, SpatialPlatform => SpatialServerBasedActorPlatform}
import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}

trait BasicAbstractActorIncarnation
  extends BasicAbstractDistributedIncarnation with BasicActorIncarnationSerializer
    with ActorPlatform {
  override type ComputationContext = CONTEXT with ComputationContextContract
  override type ComputationExport = EXPORT with ComputationExportContract
  override type UID = Int
  override type LSensorName = String
  override type NSensorName = String
  override type DataFactory = DataFactoryContract
  override type Program = AggregateProgram with ProgramContract
  override val interopUID = interopID
  override val linearUID = linearID

  implicit def adaptProgram(program: AggregateProgram): AggregateProgram with ProgramContract =
    new AggregateProgram with ProgramContract {
      override def round(ctx: ComputationContext): ComputationExport =
        program.round(ctx)

      override def main(): Any =
        program.main()
    }

  implicit def adaptExport(export: EXPORT): ComputationExport =
    new ExportImpl with ComputationExportContract {
      override def get[A](path: Path): Option[A] = export.get(path)
      override def put[A](path: Path, value: A): A = export.put(path, value)
      override def root[A](): A = export.root()
    }

  implicit def adaptContext(ctx: CONTEXT): ComputationContext =
    new BaseContextImpl(ctx.selfId, ctx.exports()) with ComputationContextContract {
      override def sense[T](lsns: String): Option[T] = ctx.sense(lsns)
      override def nbrSense[T](nsns: String)(nbr: Int): Option[T] = ctx.nbrSense(nsns)(nbr)
    }

  override val dataFactory = new DataFactoryContract {
    override def context(id: UID,
                         exports: Map[UID, ComputationExport],
                         lsns: Map[LSensorName, Any],
                         nsns: Map[NSensorName, Map[UID, Any]]): ComputationContext =
      factory.context(id, exports, lsns, nsns)
  }
}

object BasicActorP2P extends BasicAbstractActorIncarnation
  with P2pActorPlatform with Serializable

object BasicActorServerBased extends BasicAbstractActorIncarnation
  with ServerBasedActorPlatform with Serializable

object BasicActorSpatial extends BasicAbstractActorIncarnation
  with SpatialServerBasedActorPlatform with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D
}
