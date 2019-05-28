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
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait BasicAbstractActorIncarnation
  extends BasicAbstractDistributedIncarnation with AbstractJsonIncarnationSerializer
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

  trait CustomType

  override val platformSerializer = new PlatformSerializer {
    import it.unibo.scafi.distrib.actor.serialization.BasicSerializers._

    override implicit val readsUid: Reads[UID] = (JsPath \ "device-uid").read[String](Reads.StringReads).map(str => interopUID.fromString(str))
    override implicit val writesUid: Writes[UID] = (JsPath \ "device-uid").write[String](Writes.StringWrites).contramap(uid => uid.toString)
    override val readsLsns: Reads[LSensorName] = (JsPath \ "lsns").read[String](Reads.StringReads)
    override val writesLsns: Writes[LSensorName] = (JsPath \ "lsns").write[String](Writes.StringWrites)
    override val readsNsns: Reads[NSensorName] = (JsPath \ "nsns").read[String](Reads.StringReads)
    override val writesNsns: Writes[NSensorName] = (JsPath \ "lsns").write[String](Writes.StringWrites)

    implicit val formatSlot: Format[Slot] = new Format[Slot] {
      override def writes(o: Slot): JsValue = o match {
        case Nbr(i) => JsObject(Map("type" -> JsString("nbr"), "index" -> JsNumber(i)))
        case Rep(i) => JsObject(Map("type" -> JsString("rep"), "index" -> JsNumber(i)))
        case FunCall(i, funId) => JsObject(Map("type" -> JsString("funcall"), "index" -> JsNumber(i), "funId" -> anyToJs(funId)))
        case FoldHood(i) => JsObject(Map("type" -> JsString("foldhood"), "index" -> JsNumber(i)))
        case Scope(key) => JsObject(Map("type" -> JsString("scope"), "key" -> anyToJs(key)))
      }

      override def reads(json: JsValue): JsResult[Slot] = JsSuccess {
        json match {
          case jo@JsObject(underlying) if underlying.contains("type") => jo.value("type") match {
            case JsString("nbr") => Nbr(jo.value("index").as[BigDecimal].toInt)
            case JsString("rep") => Rep(jo.value("index").as[BigDecimal].toInt)
            case JsString("funcall") => FunCall(jo.value("index").as[BigDecimal].toInt, jo.value("funId").as[String])
            case JsString("foldhood") => FoldHood(jo.value("index").as[BigDecimal].toInt)
            case JsString("scope") => Scope(jo.value("key").as[String])
          }
        }
      }
    }

    implicit val formatPath: Format[Path] = new Format[Path] {
      override def writes(p: Path): JsValue = JsArray(p.path.map(s => formatSlot.writes(s)))
      override def reads(json: JsValue): JsResult[Path] =
        JsSuccess(factory.path(json.validate[List[Slot]].get:_*))
    }
    import it.unibo.scafi.distrib.actor.serialization.BasicSerializers._
    implicit def formatExportMap[T:Format]: Format[Map[Path,T]] = mapFormat[Path,T]

    override implicit val readsExp: Reads[ComputationExport] = new Reads[ComputationExport] {
      override def reads(json: JsValue): JsResult[ComputationExport] = JsSuccess(
        adaptExport(factory.export(
          json.as[Map[Path,Any]].toSeq:_*
        ))
      )
    }
    implicit val writesExp: Writes[ComputationExport] = new Writes[ComputationExport] {
      override def writes(o: ComputationExport): JsValue = mapAnyWrites[Path].writes(o.paths)
    }

  }

  type ProgramType = AggregateProgram
  override implicit def adaptAggregateProgram(program: ProgramType): ProgramContract =
    new AggregateProgram with ProgramContract {
      override def round(ctx: ComputationContext): ComputationExport = program.round(ctx)
      override def main(): Any = program.main()
    }

  implicit def adaptExport(export: EXPORT): ComputationExport =
    new ExportImpl with ComputationExportContract {
      override def get[A](path: Path): Option[A] = export.get(path)
      override def put[A](path: Path, value: A): A = export.put(path, value)
      override def root[A](): A = export.root()
      override def paths: Map[Path, Any] = export.paths
      override def toString: String = export.toString
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
