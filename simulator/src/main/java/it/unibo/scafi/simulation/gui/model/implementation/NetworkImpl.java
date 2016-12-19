package it.unibo.scafi.simulation.gui.model.implementation;

import it.unibo.scafi.simulation.gui.model.Network;
import it.unibo.scafi.simulation.gui.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chiara on 14/11/16.
 */
public class NetworkImpl implements Network {

    private final Set<Node> nodes;
    private Object neighborhoodPolicy;

    public NetworkImpl(final Set<Node> nodes, final Object neighborhoodPolicy){
        this.nodes = new HashSet<>(nodes);
        this.neighborhoodPolicy = neighborhoodPolicy;
        calculateNeighbours();
    }

    public NetworkImpl(final Set<Node> nodes){
        this(nodes, new Object());
    }

    @Override
    public synchronized Set<Node> getNodes() {
        return new HashSet<>(this.nodes);
    }

    @Override
    public Map<Node, Set<Node>> getNeighborhood() {
        return calculateNeighbours();
    }

    @Override
    public void setNeighborhoodPolicy(Object policy) {
        this.neighborhoodPolicy = policy;
    }

    @Override
    public Object getPolicy(){
        return this.neighborhoodPolicy;
    }

    @Override
    public Set<String> getObservableValue() {
        //TODO calcola valori osservabili ci vorrebbe una enum?
        Set<String> res = new HashSet<>();
        res.add("Neighbours");
        res.add("Id");
        res.add("Export");
        res.add("A sensor");
        res.add("None");
        return res;
    }

    private Map<Node, Set<Node>> calculateNeighbours(){
        //TODO calcola vicinato

        Set<Node> neighbours = new HashSet<>();
        Map<Node, Set<Node>> res = new HashMap<>();
        double raggioVicinato = 0.2;    //valore di default
        if(neighborhoodPolicy instanceof Double){
            raggioVicinato = (Double) neighborhoodPolicy;
        }
        for(Node n : nodes){
            neighbours = new HashSet<>();
            n.removeAllNeghbours();
            for (Node n1 : nodes){

                double distance = Math.hypot(n.getPosition().getX()-n1.getPosition().getX(), n.getPosition().getY()-n1.getPosition().getY());

                if(distance <= raggioVicinato){
                    neighbours.add(n1);
                }
            }

            n.addAllNeighbours(neighbours);
            res.put(n, neighbours);
        }

        return res;
    }
}
