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

import it.unibo.scafi.distrib.actor.serialization.{AbstractJsonPlatformSerializer, CustomSerializer}
import it.unibo.scafi.space.{Point3D, Point2D, Point1D}
import play.api.libs.json._

import scala.collection.mutable.{Map => MMap}

trait AbstractJsonIncarnationSerializer extends AbstractJsonPlatformSerializer { self: BasicAbstractActorIncarnation =>
  CustomSerializer.incarnation = Some(this)

  override def anyToJs: PartialFunction[Any, JsValue] = super.anyToJs orElse {
    case u:UID => Json.obj("type" -> "UID", "val" -> u.asInstanceOf[Int])
    case e:ComputationExport => Json.obj("type" -> "ComputationExport", "val" -> anyToJs(e.getMap))
    case p:Point2D => Json.obj("type" -> "Point2D", "x" -> anyToJs(p.x), "y" -> anyToJs(p.y))
    case p:Path => Json.obj("type" -> "Path", "list" -> anyToJs(p.asInstanceOf[PathImpl].path))
    case s:Slot => s match {
      case Nbr(index) => Json.obj("type" -> "Slot", "slotType" -> "Nbr", "index" -> index)
      case Rep(index) => Json.obj("type" -> "Slot", "slotType" -> "Rep", "index" -> index)
      case FoldHood(index) => Json.obj("type" -> "Slot", "slotType" -> "FoldHood", "index" -> index)
      case FunCall(index, funId) => Json.obj("type" -> "Slot", "slotType" -> "FunCall", "index" -> index, "funId" -> anyToJs(funId))
      case Scope(key) => Json.obj("type" -> "Slot", "slotType" -> "Scope", "key" -> anyToJs(key))
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
  }
}
