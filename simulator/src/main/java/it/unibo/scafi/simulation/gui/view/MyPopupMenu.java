package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class represent the SoimulationPanel pop menu
 * that contains all possible action and observation
 *
 * Created by chiara on 11/11/16.
 */
public class MyPopupMenu extends JPopupMenu {

    private final Controller controller = Controller.getIstance();
    private final JMenu observations = new JMenu("Observe");
    private final JMenu actions = new JMenu("Actions");

    public MyPopupMenu(){

        JMenuItem clear = new JMenuItem("Clear");
        clear.addActionListener(e->{
            controller.clearSimulation();
        });
        add(clear);
        addSeparator();
        add(observations);
        addSeparator();
        add(actions);
        observations.setEnabled(false);
        actions.setEnabled(false);
    }

    public void addAction(final String nameAction, final ActionListener actlist){
        JMenuItem action = new JMenuItem(nameAction);
        action.addActionListener(actlist);
        actions.add(action);
    }

    public void addObservation(final String nameObservation, final ActionListener actlist){
        JMenuItem observation = new JMenuItem(nameObservation);
        ActionListener actList1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actlist.actionPerformed(e);
                for(int i = 0; i<observations.getItemCount(); i++){
                    observations.getItem(i).setEnabled(true);
                }
                observation.setEnabled(false);
            }
        };
        if(nameObservation.equals("SENSOR")){
            observation.addActionListener(actlist);
        }else {
            observation.addActionListener(actList1);
        }
        observations.add(observation);
    }
}