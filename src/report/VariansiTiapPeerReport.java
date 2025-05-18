/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.SimScenario;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
//import routing.community.FrequencyDecisionEngine;
import routing.community.VarianceDecisionEngine;

/**
 *
 * @author jarkom
 */
public class VariansiTiapPeerReport extends Report{
    
    public static final String NODE_ID = "closenessToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Double>> encounterData;
    private Map<DTNHost, Double> avgEncounter;
    
    public VariansiTiapPeerReport(){
        super();
        Settings s = getSettings();
        if (s.contains(NODE_ID)) {
            nodeAddress = s.getInt(NODE_ID);
        } else {
            nodeAddress = 0;
        }
        encounterData = new HashMap<>();
    }
    
    @Override
    public void done(){
        
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();        
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
            if(!(de instanceof VarianceDecisionEngine)){
                continue;
            }
            
            VarianceDecisionEngine cd =(VarianceDecisionEngine)de;
            Map<DTNHost, List<Double>> nodeComm = cd.getVariance();
            
            if (h.getAddress() == nodeAddress) {
                encounterData.putAll(nodeComm);
            }
        }
        
        for (DTNHost node : nodes) {
            if (encounterData.containsKey(node)) {
                double avgCloseness = calculateAvgDuration(encounterData.get(node));
                avgEncounter.put(node, avgCloseness);
            }
        }
        
        double values = 0;
        for (Double avgEncounter : avgEncounter.values()) {
            values += avgEncounter;
        }
        
        double avgValues = values/avgEncounter.size();
        
        write("Encounter Time To " +nodeAddress);
        for (Map.Entry<DTNHost, Double> entry : avgEncounter.entrySet()) {
            DTNHost key = entry.getKey();
            Double value = entry.getValue();
            write(key+" " + ' ' +value);
        }
        write("Average " +avgValues);
        super.done();
    }
    
    private double calculateAvgDuration(List<Double> encounter) {
        Iterator<Double> i = encounter.iterator();
        double temp = 0;
        while (i.hasNext()) {
            Double next = i.next();
            temp += next;
        }

        double avgDuration = temp / encounter.size();
        return avgDuration;
    } 
}
