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

package it.unibo.scafi.renderer3d.util

import javafx.scene.shape.TriangleMesh

/**
 * Utility object to merge two TriangleMesh together. When calling getMesh, some Node types return Mesh but it can be
 * casted to TriangleMesh, for example [[org.fxyz3d.shapes.primitives.FrustumMesh]], so these Node types can also be
 * merged.
 *
 * How to use this to improve performance even more:
 * One could reduce the bottleneck of rendering the connections. The problem is that right now each connection is a Node
 * in the scene and JavaFx can't handle too many scene nodes. So the solution is to create another mesh (TriangleMesh)
 * for each NetworkNode: this mesh would contain some of the connections of the node, by merging their meshes into a
 * single mesh, so that there is a single scene node for all those connections.
 * To decide if a connection should be merged to the mesh related to node1 or node2: always choose the node with lower id.
 * -To add a connection: create it by calling [[it.unibo.scafi.renderer3d.util.Rendering3DUtils.createLine]], then obtain
 * the mesh from the line, cast it to TriangleMesh and merge it to the TriangleMesh of the other merged lines.
 * -To remove a connection: simply create a new TriangleMesh and merge each line to it, except the removed line. It's
 * possible to simply remove the vertices and faces of the line from the merged mesh but it's more difficult, because
 * the faces of the mesh depend on the number of vertices, so to remove vertices one should also update the faces.
 * -To update the connections of a moved node: simply recreate the merged mesh by recreating the lines and merging them
 * again. It's possible that more than one mesh needs to be updated, since some connections of the node could connect
 * it to a node with a lower id: so, recreate all the merged meshes that have to be updated. This can be a heavy
 * operation, so it might be useful to enable this optimization only if the nodes are known to remain in the same place.
 * */
object MeshMerger {

  /**
   * Merges the second mesh into the first. To show a mesh in the scene, put the mesh inside a MeshView and add that to
   * the scene.
   * @param mesh1 the first mesh, also the one that will contain the merged result
   * @param mesh2 the second mesh, which will be added to the first one
   * */
  def mergeToFirstMesh(mesh1: TriangleMesh, mesh2: TriangleMesh): Unit = {
    val pointsCount = mesh1.getPoints.size()/3
    // scalastyle:off magic.number
    val newFaces = getFaces(mesh2)
      .grouped(6)
      .flatMap(face =>
        List(pointsCount + face(0), face(1), pointsCount + face(2), face(3), pointsCount + face(4), face(5)))
      .toList
    mesh1.getFaces.addAll(newFaces :_*)
    mesh1.getPoints.addAll(mesh2.getPoints)
    mesh1.getTexCoords.addAll(mesh2.getTexCoords)
  }

  // scalastyle:off null
  private def getFaces(mesh: TriangleMesh): Array[Int] = mesh.getFaces.toArray(null) //null here is needed

}
