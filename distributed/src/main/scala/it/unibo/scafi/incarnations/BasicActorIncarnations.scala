/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.p2p.{Platform => P2pActorPlatform}
import it.unibo.scafi.distrib.actor.server.{ServerPlatform => ServerBasedActorPlatform, SpatialPlatform => SpatialServerBasedActorPlatform}
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

  override val platformSerializer: PlatformSerializer = new PlatformSerializer {
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
          json.as[Map[Path,Any]](mapAnyFormat).toSeq:_*
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
    new ExportImpl(export.getMap) with ComputationExportContract

  implicit def adaptContext(ctx: CONTEXT): ComputationContext =
    new BaseContextImpl(ctx.selfId, ctx.exports()) with ComputationContextContract {
      override def sense[T](localSensorName: String): Option[T] = ctx.sense(localSensorName)
      override def nbrSense[T](nbrSensorName: String)(nbr: Int): Option[T] = ctx.nbrSense(nbrSensorName)(nbr)
    }

  override val dataFactory: DataFactoryContract = new DataFactoryContract {
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
