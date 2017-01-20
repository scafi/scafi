package it.unibo.scafi.simulation.gui.controller;

import it.unibo.scafi.simulation.gui.Simulation;
import it.unibo.scafi.simulation.gui.model.SimulationManager;
import it.unibo.scafi.simulation.gui.model.implementation.NodeImpl;
import it.unibo.scafi.simulation.gui.model.implementation.SimulationImpl;
import it.unibo.scafi.simulation.gui.utility.Utils;
import it.unibo.scafi.simulation.gui.model.Node;
import it.unibo.scafi.simulation.gui.model.implementation.NetworkImpl;
import it.unibo.scafi.simulation.gui.view.GuiNode;
import it.unibo.scafi.simulation.gui.view.NodeInfoPanel;
import it.unibo.scafi.simulation.gui.view.SimulationPanel;
import it.unibo.scafi.simulation.gui.view.SimulatorUI;
import it.unibo.scafi.space.SpaceHelper;
import it.unibo.scafi.space.SpaceHelper$;

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;
import java.util.*;

/**
 * Created by chiara on 14/11/16.
 */
public class Controller {

    private static Controller SINGLETON;
    private SimulatorUI gui;
    protected SimulationManager simManager;
    protected final Map<Node, GuiNode> nodes = new HashMap<>();

    private String valueShowed = "EXPORT";
    private ControllerPrivate controllerUtility;

    public static Controller getIstance(){
        if(SINGLETON==null){
            SINGLETON = new Controller();
        }
        return SINGLETON;
    }

    private Controller(){}

    public void setGui(final SimulatorUI simulatorGui){
        this.gui = simulatorGui;
        this.controllerUtility = new ControllerPrivate(gui);

    }
    public void setSimManager(final SimulationManager simManager){ this.simManager = simManager; }

    public Map<Node, Set<Node>> getNeighborhood(){
        return this.simManager.getSimulation().getNetwork().getNeighborhood();
    }

    //metodi gestione simulazione
    public void startSimulation(final int numNodes, String topology, Object policyNeighborhood, Object runProgram, Double deltaRound, Object strategy) {
        for(int i = 0; i<numNodes; i++){
            GuiNode guiNode = new GuiNode();
            Node node = new NodeImpl(i, new Point2D.Double( new Random().nextDouble(), new Random().nextDouble()));
            guiNode.setLocation(Utils.calculatedGuiNodePosition(node.getPosition()));
            this.nodes.put(node, guiNode);
            gui.getSimulationPanel().add(guiNode, 0);
        }

        Simulation simulation = new SimulationImpl();   //creazione simulazione model
        simulation.setNetwork(new NetworkImpl(this.nodes.keySet(), policyNeighborhood));
        simulation.setDeltaRound(deltaRound);
        simulation.setRunProgram(runProgram);
        simulation.setStrategy(strategy);
        simManager.setSimulation(simulation);
        simManager.setPauseFire(deltaRound);    //pausa tra un round e un altro
        simManager.start(); //avvio della simulazione

        controllerUtility.addObservation();   //aggiungo le osservazioni
        controllerUtility.addAction();        //aggiungo le azioni
        controllerUtility.enableMenu(true);
        System.out.println("START");
    }

    public  void resumeSimulation(){
        System.out.println("RESUME");
        simManager.resume();
    }

    public void stopSimulation() {
        System.out.println("STOP");
        simManager.stop();
    }

    public void stepSimulation(final int n_step){
        simManager.step(n_step);
    }

    public void pauseSimulation(){ simManager.pause(); }

    public void clearSimulation(){
        simManager.stop();
        gui.setSimulationPanel(new SimulationPanel());
        controllerUtility.enableMenu(false);
        this.nodes.clear();
    }

    //mostra le informazioni del nodo selezionato
    public void showInfoPanel(final GuiNode node, final boolean showed){
        if(showed) {
            if (node.getInfoPanel() == null) {
                NodeInfoPanel info = new NodeInfoPanel(node);
                nodes.forEach((n, g) -> {
                    if (g == node) {
                        info.setId(n.getId());  //l'id è mantenuto solo ne model
                    }
                });
                node.setInfoPanel(info);
            }
            controllerUtility.calculatedInfo(node.getInfoPanel());
            gui.getSimulationPanel().add(node.getInfoPanel(), 0);

        } else {
            gui.getSimulationPanel().remove(node.getInfoPanel());
        }

        controllerUtility.revalidateSimulationPanel();
    }

    //mostra ll'immagine di sfondo della simulazione
    public void showImage(final Image img, final boolean showed) {
        if(showed) {
            gui.getSimulationPanel().setBackgroundImage(img);
        } else {
            gui.getSimulationPanel().setBackgroundImage(null);
        }
        controllerUtility.revalidateSimulationPanel();
    }

    //metodi per gestire i nodi: spostamento, aggiornamento valori, aggirnamento icona
    public void moveNode(GuiNode guiNode, Point position){
        controllerUtility.revalidateSimulationPanel();
        nodes.forEach((n,g)->{
            if(g.equals(guiNode)){
                n.setPosition(Utils.calculatedNodePosition(position));
                simManager.getSimulation().setPosition(n);
            }
        });
        if(guiNode.getInfoPanel()!=null){
            controllerUtility.calculatedInfo(guiNode.getInfoPanel());
        }
    }

    public void setShowValue(final String value){
        this.valueShowed = value;
    }

    public void updateValue(){
        switch (valueShowed){
            case "ID" : nodes.forEach((n,g) -> g.setValueToShow(n.getId()+""));
                break;
            case "EXPORT" : nodes.forEach((n,g) -> {
                String str = "";
                if(n.getExport() instanceof Double){
                    str = String.format("%5.2g", Double.parseDouble(n.getExport().toString()));
                } else{
                    str = n.getExport().toString();
                }
                g.setValueToShow(str);
            });
                break;
            case "NONE" : nodes.forEach((n,g) -> g.setValueToShow(""));
                break;
        }
    }

    public void selectNodes(final Rectangle area) {
        gui.getSimulationPanel().setRectSelection(area);
        Set<GuiNode> selectedNodes = new HashSet<>();
        nodes.forEach((n,g)-> {
            try { //rettangolo contiene il punto medio del GuiNode
                if (area.contains(new Point(g.getLocation().x + (g.getWidth() / 2), g.getLocation().y + (g.getHeight() / 2)))) {
                    g.setSelected(true);
                    selectedNodes.add(g);
                } else {
                    g.setSelected(false);
                }
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        });
    }

    public void moveNodeSelect(final Point p){
        nodes.forEach((n,g)-> {
            if (g.isSelected()) {
                Point pos = g.getLocation();
                g.setLocation(pos.x + p.x, pos.y + p.y);
                moveNode(g, g.getLocation());
            }
        });
    }

    //metodi per gestire i sensori: Settarli, vedere quale nodo ha un sensore con un certo valore...
    public void setSensor(final String sensorName, final Object value){
        controllerUtility.setSensor(sensorName, value);
    }

    public void checkSensor(final String sensor, final String operator, final String value){
        controllerUtility.checkSensor(sensor, operator, value);
    }

}
