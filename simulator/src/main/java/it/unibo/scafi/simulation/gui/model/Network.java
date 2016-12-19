package it.unibo.scafi.simulation.gui.model;

import java.util.Map;
import java.util.Set;

/**
 * Created by chiara on 14/11/16.
 */
public interface Network {

    Set<Node> getNodes();
    Map<Node, Set<Node>> getNeighborhood();
    Object getPolicy();
    void setNeighborhoodPolicy(Object policy);
    Set<String> getObservableValue();

}
