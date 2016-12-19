package it.unibo.scafi.simulation.gui.model.implementation;

import it.unibo.scafi.simulation.gui.Simulation;
import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.model.SimulationManager;
import it.unibo.scafi.core.Core;
import scala.Tuple2;

/**
 * Created by chiara on 14/11/16.
 */
public class SimulationManagerImpl implements SimulationManager {

    private Simulation simulation;
    private int step_num = Integer.MAX_VALUE;
    private int i = 0;
    private boolean stop = false;
    private Double pauseFire = 100.0;
    private Thread simulationThread;

    @Override
    public Simulation getSimulation() {
        return this.simulation;
    }

    @Override
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public void setPauseFire(Double pauseFire) {
        this.pauseFire = pauseFire;
    }

    @Override
    public void start() {
        simulationThread = getMyThread();
        stop = false;
        this.simulationThread.start();
    }

    @Override
    public void resume() {
        i = 0;
        this.step_num = Integer.MAX_VALUE;
        simulationThread = getMyThread();
        simulationThread.start();
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public void pause() {
        this.step_num = 0;
    }

    @Override
    public synchronized void step(int num_step) {
        i = 0;
        this.step_num = num_step;
        simulationThread = getMyThread();
        simulationThread.start();
    }

    private Thread getMyThread(){
        return new Thread(){

            //ogni giro computa un nodo solo
            @Override
            public void run(){
                while(i<step_num && !stop) {
                    // Core logic
                    runSingleSimulationStep();
                    try {
                        Thread.sleep((new Double(pauseFire)).longValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                this.interrupt();
            }
        };
    }

    private void runSingleSimulationStep(){
        Tuple2<Object, Core.Export> exp = simulation.getRunProgram().apply();
        System.out.println(exp);
        simulation.getNetwork().getNodes().forEach(n -> {
            if (n.getId() == (Integer) exp._1()) n.setExport(exp._2().root());
        });
        Controller.getIstance().updateValue();
    }
}
