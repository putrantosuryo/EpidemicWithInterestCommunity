/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.DTNHost;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public interface IntermittentDetection {
    
    public double getIntermittentTime(Map<DTNHost, LinkedList<Duration>> connectionHistory, EncounterDetection ed);
    
    public IntermittentDetection replicate();
}
