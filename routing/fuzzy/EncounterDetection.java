/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.DTNHost;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public interface EncounterDetection {
    
    public void newConnection(DTNHost thisHost, DTNHost peer, 
            EncounterDetection peerED);
    
    public void connectionLost(DTNHost thisHost, DTNHost peer, 
            EncounterDetection peerED, List<Duration> connectionHistory);
    
    public boolean isHosthasBeenEncountered(DTNHost host);
    
    public Set<DTNHost> getEncounteredHost();
    
    public LinkedList<Double> getEncounterTime();
    
    public EncounterDetection replicate();
}
