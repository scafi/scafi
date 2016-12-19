package it.unibo.scafi.simulation.gui.model;

/**
 * Created by chiara on 14/11/16.
 */
public interface Sensor {

    String getName();
    Object getValue();
    void setValue(Object value);
}
