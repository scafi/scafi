package it.unibo.scafi.simulation.gui.model;

import it.unibo.scafi.simulation.gui.Simulation;

/**
 * Created by chiara on 14/11/16.
 */
public interface SimulationManager {

    Simulation getSimulation();
    void setSimulation(Simulation simulation);
    void setPauseFire(Double pauseFire);
    void start();
    void resume();
    void stop();
    void pause();
    void step(int num_step);
}
