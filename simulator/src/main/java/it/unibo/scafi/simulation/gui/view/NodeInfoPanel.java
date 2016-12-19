package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This represent a JPanel
 * which show all information
 * about its node
 * Created by chiara on 03/06/16.
 */
public class NodeInfoPanel extends JInternalFrame {

    private final GuiNode node;
    private final JLabel idJl = new JLabel();
    private final Map<String, String> sensors = new HashMap<>();
    private final JPanel listSensorPanel;
    private final Controller controller = Controller.getIstance();

    public NodeInfoPanel(final GuiNode node){

        this.node = node;
        setSize(Utils.getGuiNodeInfoPanelDim());
        setResizable(true);
        setBorder(null);
        setLayout(new BorderLayout());

        //north
        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.add(this.idJl, BorderLayout.CENTER);

        int closeIconDim = getWidth()/10;
        JButton close = new JButton(Utils.getScaledImage("close.png",closeIconDim,closeIconDim));
        close.setBorderPainted(false);
        close.addActionListener(e->{
            controller.showInfoPanel(node, false);
        });

        idPanel.add(close, BorderLayout.WEST);
        idPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.black));
        add(idPanel, BorderLayout.NORTH);

        //center
        listSensorPanel = new JPanel();
        listSensorPanel.setLayout(new BoxLayout(listSensorPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listSensorPanel);
        add(scroll, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * make a new JPanel with both key and value and
     * add this to a InfoPanel
     * @param key
     * @param value
     */
    public void addInfo(final String key, final String value){
        if(this.sensors.containsKey(key)){
            this.sensors.replace(key, value);
            listSensorPanel.removeAll();       //Riaggiungo tutti i sensori
            sensors.forEach((s,v)->{
                JPanel senPanel = new JPanel(new BorderLayout());
                senPanel.add(new JLabel(s +" : "+v), BorderLayout.WEST);
                listSensorPanel.add(senPanel);
            });

        } else {
            this.sensors.put(key, value);
            JPanel senPanel = new JPanel(new BorderLayout()); //Aggiungo la label solo del nuovo sensore
            senPanel.add(new JLabel(key +" : "+value), BorderLayout.WEST);
            listSensorPanel.add(senPanel);
        }

        this.revalidate();
        this.repaint();
    }

    public void setId(final int id){
        this.idJl.setText(""+id);
    }

    public int getId(){
        return Integer.parseInt(this.idJl.getText());
    }
}