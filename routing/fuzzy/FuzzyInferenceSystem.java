/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.DTNHost;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public interface FuzzyInferenceSystem {
    
    public void computeDefuzzificationFor();
    
    public double sumOfTheMiuFor();
}
