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

package examples.gui

import java.awt.{FlowLayout, BorderLayout}
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import akka.actor.{Props, Actor, ActorRef}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

import scala.concurrent.duration._
import scala.collection.mutable.{ Map => MMap }

import it.unibo.scafi.distrib.actor._

class ServerGUIActor(val I: BasicAbstractActorIncarnation,
                              private var tm: ActorRef) extends Actor with ActionListener {
  val width = 750
  val height = 400

  type ID = I.ID
  type LSNS = I.LSNS
  type EXPORT = I.EXPORT

  val interopId = I.interopID
  val interopLsns = I.interopLSNS

  tm ! MsgAddObserver(self)

  /* Local imports and variables */

  import scala.collection._
  import context.dispatcher

  // Provides the ExecutionContext
  protected val Log = akka.event.Logging(context.system, this)

  var registry: ActorRef = _

  /* GUI-related members */
  var frame: JFrame = _
  var bAddNbr: JButton = _
  var devices = new DefaultComboBoxModel[String]()
  var devsCombo: JComboBox[String] = _
  var blackboard = new JTextArea(20,50)

  BuildFrame()

  /* Behavior */

  // GUI will update at 100 ms interval
  context.system.scheduler.schedule(1.millis, 100.millis) { self ! GoOn }

  val map = MMap[ID,ActorRef]()
  val nrounds = MMap[ID,Int]()
  val neighborhoods = MMap[ID,Set[ID]]()
  val exports = MMap[ID,EXPORT]()
  val sensors = MMap[ID,MMap[LSNS,Any]]()

  def neighborhood(id: ID): Set[ID] = neighborhoods.getOrElse(id, Set())

  def inputManagementBehavior: Receive = {
    case I.MsgNeighbor(id,idn) => neighborhoods += id -> (neighborhood(id) + idn)
    case I.MsgNeighborhood(id,nbrs) => {
      neighborhoods.clear()
      neighborhoods += id -> (neighborhood(id) ++ nbrs)
    }
    case I.MsgExport(id,export) => {
      exports += id -> export
      nrounds += id -> (nrounds.getOrElse(id,0)+1)
    }
    case I.MsgExports(exps) => exports ++= exps
    case I.DevInfo(id, ref) => {
      println("\n\n\n\n\n\n\n")
      map += (id -> ref)
      devices.addElement(id.toString)
    }
    case I.MsgSensorValue(id,name,value) => {
      sensors += id -> (sensors.getOrElse(id,MMap()) + (name->value))
    }
  }

  def workingBehavior: Receive ={
    case GoOn => { UpdateBlackBoard(); frame.repaint(); frame.revalidate() }
  }

  def receive = workingBehavior.orElse(inputManagementBehavior)

  var toPause: Boolean = true
  override def actionPerformed(e: ActionEvent): Unit = {
    val s = e.getSource

    if(s == bAddNbr){
      val ids = JOptionPane.showInputDialog(
        frame,
        "FORMAT: 'ID:IDN'",
        "Add NBR",
        JOptionPane.PLAIN_MESSAGE).split(':');
      tm ! I.MsgNeighbor(interopId.fromString(ids(0)),interopId.fromString(ids(1)))
    }
  }

  def UpdateBlackBoard() = {
    val _selectedId = devsCombo.getSelectedItem
    if(_selectedId!=null) {
      val selectedId = _selectedId.toString
      val id = interopId.fromString(selectedId)
      blackboard.setText(
        s"""
             Nbrs: ${neighborhoods.get(id)}\n
             Export: ${exports.get(id)}\n
             Ref: ${map.get(id)}\n
             Sensors: ${sensors.get(id)}
           """.stripMargin)
    }
  }

  def BuildFrame(): Unit = {
    frame = new javax.swing.JFrame("TOPOLOGY MANAGER @ " + this.tm.path)
    frame.setSize(width, height)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val panel = new JPanel()
    frame.setContentPane(panel)
    panel.setLayout(new BorderLayout())

    val topPanel = new JPanel()

    devsCombo = new JComboBox[String](devices)
    devsCombo.addActionListener(this)

    topPanel.add(devsCombo)


    panel.add(topPanel, BorderLayout.NORTH)

    /*
    devices = new DefaultListModel[String]()
    val devicesList = new JList[String](devices)
    devicesList.setLayoutOrientation(JList.VERTICAL)
    devicesList.setVisibleRowCount(-1)
    val devicesListWrapper = new JScrollPane()
    devicesListWrapper.setViewportView(devicesList)
    devicesListWrapper.setBounds(0,0,200,400)
    panel.add(devicesListWrapper, BorderLayout.WEST)
    */

    panel.add(blackboard, BorderLayout.CENTER)

    val cmdPanel = new JPanel()
    cmdPanel.setLayout(new FlowLayout())
    bAddNbr = new JButton("AddNBR")
    bAddNbr.addActionListener(this)

    cmdPanel.add(bAddNbr)
    panel.add(cmdPanel, BorderLayout.SOUTH)

    //frame.pack()
    frame.setVisible(true)
  }
}

object ServerGUIActor {
  def props(inc: BasicAbstractActorIncarnation, server: ActorRef) =
    Props(classOf[ServerGUIActor], inc, server)
}
