/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public class NENT implements IntermittentDetection{

    protected static int COMPUTE_INTERVAL = 1037;
    
    protected double intermittentTime;
    
    protected int  lastTimeConnection;
    
    public NENT(Settings s){
       
    }
    
    public NENT(NENT prototype){
        this.intermittentTime = -COMPUTE_INTERVAL;
    }
    
    @Override
    public double getIntermittentTime(Map<DTNHost, LinkedList<Duration>> connectionHistory, EncounterDetection ed) {
        if (SimClock.getIntTime() - this.lastTimeConnection < ed.getEncounterTime().getLast()) {
            return intermittentTime;
        }
        int epochCount = SimClock.getIntTime() / ed.getEncounterTime().getLast().intValue();
        int[] timeLapse = new int[epochCount];
        
        int epoch, timeNow = SimClock.getIntTime();
        Map<Integer, Set<DTNHost>> nodesCountedInEpoch = 
			new HashMap<Integer, Set<DTNHost>>();
        
        for (int i = 0; i < epochCount; i++) {
            nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
        }
        
        for (Map.Entry<DTNHost, LinkedList<Duration>> entry : connectionHistory.entrySet()) {
            DTNHost host = entry.getKey();
            for (Duration duration : entry.getValue()) {
                int timePassed = (int) (timeNow - duration.end);
                if (timePassed > COMPUTE_INTERVAL * epochCount) {
                    break;
                }
                epoch = timePassed/COMPUTE_INTERVAL;
                
                Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch);
				if(nodesAlreadyCounted.contains(host))
					continue;
                timeLapse[epoch]++;
                nodesAlreadyCounted.add(host);
            }
        }
        
        int sum = 0;
        for (int i = 0; i < epochCount; i++) {
            sum += timeLapse[i];
        }
        this.intermittentTime = sum / epochCount;
        this.lastTimeConnection = SimClock.getIntTime();
        return this.intermittentTime;
    }

    @Override
    public IntermittentDetection replicate() {
        return new NENT(this);
    }
    
}
