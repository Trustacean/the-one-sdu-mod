/*
 *
 *
 */
package report;

import java.util.*;
//import java.util.List;
//import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

public class ClosenessNodeTiapWaktuReport extends Report {

    private Map<DTNHost, List<Double>> closenessCount;
    private Map<DTNHost, List<Double>> encounterData;
    private List<Double> listCloseness;

    public ClosenessNodeTiapWaktuReport() {
        super();
  
    }

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost ho : nodes) {
            MessageRouter r = ho.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof ClosenessDecisionEngine)) {
                continue;
            }
            ClosenessDecisionEngine cd = (ClosenessDecisionEngine) de;
            Map<DTNHost, List<Double>> closenessCounter = cd.getCloseness();
            
            for (Map.Entry<DTNHost, List<Double>> entry : closenessCounter.entrySet()) {
                String print = "";
                for (Double value : entry.getValue()) {
                    print = print + "\n" + value;
                }
                write(print);
            }
        }

//        for (DTNHost node : nodes) {


//            if (closenessCount.containsKey(node)) {
//                double avgCloseness = getAverageCloseness(closenessCount.get(node));
//                encounterData.put(node, avgCloseness);
//            }
//        }
//        System.out.println(closenessCount);
//        String print = "";
//        for (Map.Entry<DTNHost, List<Double>> entry : encounterData.entrySet()) {
//            DTNHost key = entry.getKey();
//            for (Double value : entry.getValue()) {
//                print = print + "\n" + value;
//            }
//        }
//        write(print);
        super.done();
    }

    private double getAverageCloseness(List<Double> closenessList) {
        Iterator<Double> i = closenessList.iterator();
        double sigm = 0;
        while (i.hasNext()) {
            Double next = i.next();
            sigm += next;
        }
        double avgCloseness = sigm / closenessList.size();
        return avgCloseness;
    }

}
