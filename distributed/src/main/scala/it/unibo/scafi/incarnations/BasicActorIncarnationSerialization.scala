/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.serialization.{AbstractJsonPlatformSerializer, CustomAkkaSerializer}
import it.unibo.scafi.space.Point2D
import play.api.libs.json._
import play.api.libs.json.Writes._

import scala.collection.mutable.{Map => MMap}

trait AbstractJsonIncarnationSerializer extends AbstractJsonPlatformSerializer { self: BasicAbstractActorIncarnation =>
  CustomAkkaSerializer.incarnationSerializer = Some(this)

  override def anyToJs: PartialFunction[Any, JsValue] = super.anyToJs orElse {
    case u:UID => JsObject(Seq("type" -> JsString("UID"), "val" -> JsNumber(u.asInstanceOf[BigDecimal])))
    case e:ComputationExport => Json.obj("type" -> "ComputationExport", "val" -> anyToJs(e.getMap))
    case p:Point2D => Json.obj("type" -> "Point2D", "x" -> anyToJs(p.x), "y" -> anyToJs(p.y))
    case p if p.isInstanceOf[Path] => Json.obj("type" -> "Path", "list" -> anyToJs(p.asInstanceOf[PathImpl].path))
    case s if s.isInstanceOf[Slot] => s match {
      case n if n.isInstanceOf[Nbr[Any]] =>
        Json.obj("type" -> "Slot", "slotType" -> "Nbr", "index" -> n.asInstanceOf[Nbr[Any]].index)
      case r if r.isInstanceOf[Rep[Any]] =>
        Json.obj("type" -> "Slot", "slotType" -> "Rep", "index" -> r.asInstanceOf[Rep[Any]].index)
      case f if f.isInstanceOf[FoldHood[Any]] =>
        Json.obj("type" -> "Slot", "slotType" -> "FoldHood", "index" -> f.asInstanceOf[FoldHood[Any]].index)
      case f if f.isInstanceOf[FunCall[Any]] => val fun = f.asInstanceOf[FunCall[Any]]
        Json.obj("type" -> "Slot", "slotType" -> "FunCall", "index" -> fun.index, "funId" -> anyToJs(fun.funId))
      case s if s.isInstanceOf[Scope[Any]] =>
        Json.obj("type" -> "Slot", "slotType" -> "Scope", "key" -> anyToJs(s.asInstanceOf[Scope[Any]].key))
    }
  }
  override def jsToAny: PartialFunction[JsValue, Any] = super.jsToAny orElse {
    case u if (u \ "type").as[String] == "UID" => (u \ "val").as[Int]
    case e if (e \ "type").as[String] == "ComputationExport" =>
      val export = new EngineFactory().emptyExport()
      val field = classOf[ExportImpl].getDeclaredField("map"); field.setAccessible(true)
      field.set(export, MMap(jsToAny((e \ "val").get).asInstanceOf[Map[Any,Any]].toSeq:_*)); field.setAccessible(false)
      adaptExport(export)
    case p if (p \ "type").as[String] == "Point2D" => new Point2D(jsToAny((p \ "x").get).asInstanceOf[Double],
      jsToAny((p \ "y").get).asInstanceOf[Double])
    case p if (p \ "type").as[String] == "Path" => new PathImpl(jsToAny((p \ "list").get).asInstanceOf[List[Slot]])
    case s if (s \ "type").as[String] == "Slot" => (s \ "slotType").as[String] match {
      case "Nbr" => Nbr((s \ "index").as[Int])
      case "Rep" => Rep((s \ "index").as[Int])
      case "FoldHood" => FoldHood((s \ "index").as[Int])
      case "FunCall" => FunCall((s \ "index").as[Int], jsToAny((s \ "funId").get))
      case "Scope" => Scope(jsToAny((s \ "key").get))
    }
    case c if (c \ "type").as[String] == "Class" => Class.forName(jsToAny((c \ "name").get).asInstanceOf[String])
  }
}
