/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.community.FrequencyDecisionEngine;

/**
 *
 * @author jarkom
 */
public class FrequencyEncounterPerNode extends Report{
    public FrequencyEncounterPerNode(){
        init();
    }
    public void done(){
        
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();        
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
            if(!(de instanceof FrequencyDecisionEngine)){
                continue;
            }
            FrequencyDecisionEngine cd =(FrequencyDecisionEngine)de;
            Map<DTNHost, List<Duration>> nodeComm = cd.getFrequency();
            write(h+" "+nodeComm);
        }
        super.done();
    }
}
