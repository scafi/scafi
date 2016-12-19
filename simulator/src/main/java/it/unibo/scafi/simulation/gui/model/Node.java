package it.unibo.scafi.simulation.gui.model;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

/**
 * Created by chiara on 14/11/16.
 */
public interface Node {

    int getId();

    Object getExport();
    void setExport(Object export);

    Point2D getPosition();
    void setPosition(Point2D position);

    Set<Node> getNeighbours();
    boolean addNeighbour(Node neighbour);
    boolean addAllNeighbours(Set<Node> neighbours);
    boolean removeNeighbour(Node neighbour);
    void removeAllNeghbours();

    Set<Sensor> getSensors();
    Object getSensorValue(Sensor sensor);
    Object getSensorValue(String sensor);
    Map<Sensor, Object> getAllSensors();
    void setSensor(Sensor sensor, Object value);
    void setSensor(String sensor, Object value);

    void doAction(Action action);
}

