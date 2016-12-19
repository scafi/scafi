package it.unibo.scafi.simulation.gui.model.implementation;

import it.unibo.scafi.simulation.gui.model.Action;
import it.unibo.scafi.simulation.gui.model.Node;
import it.unibo.scafi.simulation.gui.model.Sensor;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by chiara on 14/11/16.
 */
public class NodeImpl implements Node {

    private final int id;
    private Point2D position;
    private volatile Object export;
    private final Set<Node> neighbours;
    private final Map<Sensor, Object> sensor;

    public NodeImpl(final int id, final Point2D position){

        this.id = id;
        this.position = position;
        this.export = new Object();
        this.neighbours = new HashSet<>();
        this.sensor = new HashMap<>();
        sensor.put(SensorEnum.TEMPERATURE, 0);
        sensor.put(SensorEnum.SOURCE, false);
        sensor.put(SensorEnum.OBSTACLE, false);
    }

    public NodeImpl(final int id){
        this(id, new Point2D.Double(new Random().nextDouble(), new Random().nextDouble()));
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public synchronized Object getExport() {return this.export;}//this.export;}

    @Override
    public synchronized void setExport(Object export) {
        this.export = export;
    }

    @Override
    public Point2D getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Point2D position) {
        this.position = position;
    }

    @Override
    public Set<Node> getNeighbours() {
        return new HashSet<>(this.neighbours);
    }

    @Override
    public boolean addNeighbour(Node neighbour) {
        return this.neighbours.add(neighbour);
    }

    @Override
    public boolean addAllNeighbours(Set<Node> neighbours) {
        return this.neighbours.addAll(neighbours);
    }

    @Override
    public boolean removeNeighbour(Node neighbour) {
        return this.neighbours.remove(neighbour);
    }

    @Override
    public void removeAllNeghbours() {
        this.neighbours.clear();
    }

    @Override
    public Set<Sensor> getSensors() {
        return this.sensor.keySet();
    }

    @Override
    public Object getSensorValue(String sensor){

        for(Sensor s : this.sensor.keySet()){
            if(s.getName().equals(sensor)){
                return this.sensor.get(s);
            }
        }
        return null;
    }

    @Override
    public Object getSensorValue(Sensor sensor) {
        this.sensor.forEach((s,v)->{
            if(s.getName().equals(sensor.getName())){
                sensor.setValue(v);
            }
        });
        return sensor.getValue();
    }

    @Override
    public Map<Sensor, Object> getAllSensors() {
        return new HashMap<>(this.sensor);
    }

    @Override
    public void setSensor(Sensor sensor, Object value) {
        //gestione consistenza
        this.sensor.keySet().forEach(s->{
            if(s.equals(sensor)){
                s.setValue(value);
            }
        });

        this.sensor.put(sensor,value);
    }

    @Override
    public void setSensor(String sensorName, Object value) {
        //gestione consistenza
        this.sensor.keySet().forEach(s->{
            if(s.getName().equals(sensorName)){
                s.setValue(value);
                this.sensor.put(s,value);
            }
        });
    }

    @Override
    public void doAction(Action action) {}
}
