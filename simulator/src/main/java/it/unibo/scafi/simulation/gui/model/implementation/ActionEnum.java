package it.unibo.scafi.simulation.gui.model.implementation;

import it.unibo.scafi.simulation.gui.model.Action;

/**
 * This class represents an Node's ActionImpl
 * Created by chiara on 14/11/16.
 */
public enum ActionEnum implements Action {

    SET_SENSOR("Set Sensor", new Object()),
    CALCULATED_EXPORT("Calculated Export", new Object());

    private final String name;
    private final Object action;

    private ActionEnum(final String name, final Object action){
        this.name = name;
        this.action = action;
    }

    public String getName(){
        return this.name;
    }

    public Object getAction(){
        return this.action;
    }
}
