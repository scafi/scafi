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

package it.unibo.scafi.renderer3d.util.rendering

import javafx.scene.shape.{MeshView, TriangleMesh}

/**
 * Simple implementation of a 3d Pyramid.
 * Code from: https://stackoverflow.com/questions/31110292/why-is-the-diffuse-map-not-applied-to-my-meshview
 * */
class PyramidMesh(radius: Float, height: Float) extends MeshView{

  val pyramidMesh = new TriangleMesh
  pyramidMesh.getTexCoords.addAll(1, 1, 1, 0, 0, 1, 0, 0)
  pyramidMesh.getPoints.addAll(0, 0, 0, // Point 0 - Top
    0, height, -radius / 2, // Point 1 - Front
    -radius / 2, height, 0, // Point 2 - Left
    radius / 2, height, 0, // Point 3 - Back
    0, height, radius / 2 // Point 4 - Right
  )
  pyramidMesh.getFaces.addAll(0, 0, 2, 0, 1, 0, // Front left face
    0, 0, 1, 0, 3, 0, // Front right face
    0, 0, 3, 0, 4, 0, // Back right face
    0, 0, 4, 0, 2, 0, // Back left face
    4, 0, 1, 0, 2, 0, // Bottom rear face
    4, 0, 3, 0, 1, 0 // Bottom front face
  )

  this.setMesh(pyramidMesh)
}

