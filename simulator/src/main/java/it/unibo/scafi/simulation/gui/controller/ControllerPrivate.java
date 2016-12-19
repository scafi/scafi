package it.unibo.scafi.simulation.gui.controller;

import it.unibo.scafi.simulation.gui.model.Node;
import it.unibo.scafi.simulation.gui.model.Sensor;
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum;
import it.unibo.scafi.simulation.gui.view.GuiNode;
import it.unibo.scafi.simulation.gui.view.NodeInfoPanel;
import it.unibo.scafi.simulation.gui.view.SensorOptionPane;
import it.unibo.scafi.simulation.gui.view.SimulatorUI;

import java.util.HashSet;
import java.util.Set;

/**
 * this class is a wrapper for
 * all private Controller method
 * Created by chiara on 23/11/16.
 */
public class ControllerPrivate {

    private final SimulatorUI gui;
    private final Controller controller = Controller.getIstance();

    ControllerPrivate(final SimulatorUI gui){
        this.gui = gui;
    }

    //metodi per gestire i sensori: Settarli, vedere quale nodo ha un sensore con un certo valore...
    public void setSensor(final String sensorName, final Object value){
        boolean applyAll = true;
        for(Node n : controller.nodes.keySet() ){
            GuiNode g = controller.nodes.get(n);
            if(g.isSelected()){
                applyAll = false;
                n.setSensor(sensorName , value);
                Set<Node> set = new HashSet<>();
                set.add(n);
                controller.simManager.getSimulation().setSensor(sensorName, set, Boolean.valueOf(value.toString()));
                setImage(sensorName, value, g);
            }
        }

        if(applyAll) {
            controller.nodes.forEach((n, g) -> {
                n.setSensor(sensorName, value);
                setImage(sensorName, value, g);
            });
            controller.simManager.getSimulation().setSensor(sensorName, controller.nodes.keySet(), Boolean.valueOf(value.toString()));
        }
    }

    public void checkSensor(final String sensor, final String operator, final String value){
        controller.nodes.forEach((n,g)->{
            switch(operator){
                case "=":
                    if (n.getSensorValue(sensor).toString().equals(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
                case ">":
                    if (Integer.valueOf(n.getSensorValue(sensor).toString()) > Integer.valueOf(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
                case ">=":
                    if (Integer.valueOf(n.getSensorValue(sensor).toString()) >= Integer.valueOf(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
                case "<":
                    if (Integer.valueOf(n.getSensorValue(sensor).toString()) < Integer.valueOf(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
                case "<=":
                    if (Integer.valueOf(n.getSensorValue(sensor).toString()) <= Integer.valueOf(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
                case "!=":
                    if (Integer.valueOf(n.getSensorValue(sensor).toString()) != Integer.valueOf(value)) {
                        g.setImageButton("sensorOk.png");
                    }else{
                        g.setImageButton("node.png");
                    }
                    break;
            }
        });
    }

    public void calculatedInfo(NodeInfoPanel infoPanel){
        controller.nodes.forEach((n,g) ->{
            if(n.getId()==infoPanel.getId()){
                infoPanel.addInfo("position", "x: " + String.format("%.02f", n.getPosition().getX()*100)    // * MyDimension.getFrameDimension().getWidth() se volessi vedere le posizioni int dei frame
                        + " \t y: "+ String.format("%.02f",n.getPosition().getY()*100));
                infoPanel.addInfo("Export: ", n.getExport().toString());
                n.getAllSensors().forEach((s,v) -> {
                    infoPanel.addInfo(s.getName(), s.getValue().toString());
                });
                controller.simManager.getSimulation().getNetwork().getNeighborhood();  //ricalcola i vicini
                StringBuffer idNeighbour = new StringBuffer("[");
                n.getNeighbours().forEach(nn->idNeighbour.append(" " + nn.getId() +" "));
                idNeighbour.append("]");
                infoPanel.addInfo("Neighbours", idNeighbour.toString());
            }
        });
    }

    public void enableMenu(final boolean enabled){
        gui.getSimulationPanel().getPopUpMenu().getSubElements()[1].getComponent().setEnabled(enabled); //menu Observation
        gui.getSimulationPanel().getPopUpMenu().getSubElements()[2].getComponent().setEnabled(enabled); //menu Action
        gui.getJMenuBar().getMenu(1).setEnabled(enabled);  //Simulation
        gui.getJMenuBar().getMenu(1).getItem(0).getComponent().setEnabled(enabled);  //Simulation
        gui.getJMenuBar().getMenu(1).getItem(1).getComponent().setEnabled(!enabled);  //Simulation
        gui.getMenuBarNorth().getMenu(0).getSubElements()[0].getSubElements()[0].getComponent().setEnabled(!enabled);   //new Simulation

    }

    public void addObservation(){
        //network.getObservableValue().forEach( s -> gui.getSimulationPanel().getPopUpMenu().addObservation(s, e->{}));
        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Show Neighbours", e->gui.getSimulationPanel().showNeighbours(true));
        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Hide Neighbours", e-> gui.getSimulationPanel().showNeighbours(false));
        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Id", e-> controller.setShowValue("ID"));
        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Export", e-> controller.setShowValue("EXPORT"));

        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Nothing", e->controller.setShowValue("NONE"));
        this.gui.getSimulationPanel().getPopUpMenu().addObservation("Sensor", e->{
            SensorOptionPane sensPane = new SensorOptionPane("Observe Sensor");
            sensPane.addOperator("=");
            sensPane.addOperator(">");
            sensPane.addOperator(">=");
            sensPane.addOperator("<");
            sensPane.addOperator("<=");
            sensPane.addOperator("!=");
            for(Sensor s : SensorEnum.values()){
                sensPane.addSensor(s.getName());
            }
        });
    }

    public void addAction(){
       /* for(Action a :ActionEnum.values()){
            gui.getSimulationPanel().getPopUpMenu().addAction(a.getName(), e->{});
        }*/
        this.gui.getSimulationPanel().getPopUpMenu().addAction("Source", e->{

            setSensor(SensorEnum.SOURCE.getName(), true);
        });
        this.gui.getSimulationPanel().getPopUpMenu().addAction("Obstacle", e->{
            setSensor(SensorEnum.OBSTACLE.getName(), true);
        });
        this.gui.getSimulationPanel().getPopUpMenu().addAction("Not Source", e->{
           setSensor(SensorEnum.SOURCE.getName(), false);
        });
        this.gui.getSimulationPanel().getPopUpMenu().addAction("Not Obstacle", e->{
            setSensor(SensorEnum.OBSTACLE.getName(), false);
        });
        this.gui.getSimulationPanel().getPopUpMenu().addAction("Set Sensor", e->{
            SensorOptionPane sensPane = new SensorOptionPane("Set Sensor");
            sensPane.addOperator("=");
            for(Sensor s :SensorEnum.values()){
                sensPane.addSensor(s.getName());
            }
        });
    }

    public void revalidateSimulationPanel(){
        gui.getSimulationPanel().revalidate();
        gui.getSimulationPanel().repaint();
    }

    public void setImage(final String sensorName, final Object value, final GuiNode g){
        if(sensorName.equals(SensorEnum.OBSTACLE.getName()) && value.toString().equals("true")) {
            g.setImageButton("sensorOkSelect.png");
        } else if(sensorName.equals(SensorEnum.SOURCE.getName()) && value.toString().equals("true")) {
            g.setImageButton("sourceSelect.png");
        }  else{
            g.setImageButton("nodeSelect.png");
        }
    }
}
