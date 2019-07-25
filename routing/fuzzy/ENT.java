/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import java.util.*;
import core.*;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public class ENT implements EncounterDetection {

    
    protected Set<DTNHost> encounteredHost;
    protected LinkedList<Double> encounterTime;

    
    public ENT(Settings s) {
        
    }

    public ENT(ENT prototype) {
        
        encounteredHost = new HashSet<DTNHost>();
        encounterTime = new LinkedList<>();
    }

    @Override
    public void newConnection(DTNHost thisHost, DTNHost peer, EncounterDetection peerED) {
        ENT encounterDetection = (ENT) peerED;

        this.encounteredHost.add(thisHost);
        encounterDetection.encounteredHost.add(peer);

        if (!this.encounteredHost.contains(peer)) {
            this.encounteredHost.add(peer);
        }

        if (!encounterDetection.encounteredHost.contains(thisHost)) {
            encounterDetection.encounteredHost.add(thisHost);
        }

    }

    @Override
    public void connectionLost(DTNHost thisHost, DTNHost peer, EncounterDetection peerED, List<Duration> connectionHistory) {
        Iterator<Duration> iterable = connectionHistory.iterator();

        double time = 0;
        while (iterable.hasNext()) {
            Duration d = iterable.next();
            time += d.end - d.start;
        }

        this.encounterTime.add(time);
    }

    @Override
    public boolean isHosthasBeenEncountered(DTNHost host) {
        return this.encounteredHost.contains(host);
    }

    @Override
    public Set<DTNHost> getEncounteredHost() {
        return this.encounteredHost;
    }

    public LinkedList<Double> getEncounterTime() {
        return this.encounterTime;
    }

    @Override
    public EncounterDetection replicate() {
        return new ENT(this);
    }

}
