/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.DTNHost;
import java.util.Map;

/**
 * For report purpose that reporting each node has utility 
 * for being selected as best message carrier 
 * @author Gregorius Bima, Sanata Dharma University
 */
public interface FuzzyCollectable {
    
    /**
     * Get the summary vector of Transfer of Utility
     * for each node in the network
     * @return 
     */
     public Map<DTNHost,Double> getFuzzyInfo();
}