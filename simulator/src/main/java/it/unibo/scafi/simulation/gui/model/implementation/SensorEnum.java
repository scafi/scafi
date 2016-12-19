package it.unibo.scafi.simulation.gui.model.implementation;

import it.unibo.scafi.simulation.gui.model.Sensor;

/**
 * Created by chiara on 14/11/16.
 */
public enum SensorEnum implements Sensor {

    SOURCE("Source", false),
    TEMPERATURE("Temperature", 20.00),
    OBSTACLE("Obstacle", false);

    private final String name;
    private Object value;

    private SensorEnum(final String name, final Object value){
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }
}