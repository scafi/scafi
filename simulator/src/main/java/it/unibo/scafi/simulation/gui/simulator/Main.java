package it.unibo.scafi.simulation.gui.simulator;

import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.model.implementation.SimulationManagerImpl;
import it.unibo.scafi.simulation.gui.view.SimulatorUI;

import javax.swing.*;

/**
 * Created by chiara on 19/10/16.
 */
public class Main {

    public static void main(String[] args){

        //TODO creare qui il SimulatorManager

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Controller.getIstance().setGui(new SimulatorUI());
                Controller.getIstance().setSimManager(new SimulationManagerImpl());
            }
        });
    }
}