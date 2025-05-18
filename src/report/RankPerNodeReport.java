/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Afra Rian
 */
public class RankPerNodeReport extends Report {

    private List<Integer> nodeListSelfish;
    private Map<String, Integer> nodeRank;

    public RankPerNodeReport() {
        super();
        this.nodeListSelfish = new LinkedList<>();
        this.nodeRank = new HashMap<>();
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
            if (!(de instanceof NodeRankHelper)) {
                continue;
            }
            NodeRankHelper nr = (NodeRankHelper) de;

            Map<String, Integer> nodeList = nr.getNodeRank();
            for (Map.Entry<String, Integer> entry : nodeList.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (nodeRank.containsKey(key)) {
                    nodeRank.replace(key, nodeRank.get(key) + value);
                } else {
                    nodeRank.put(key, value);
                }
            }

            List<Integer> nodeSelfish = nr.getNodeSelfish();
            nodeListSelfish = nodeSelfish;

        }
        String print = "";
        for (Map.Entry<String, Integer> entry : nodeRank.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            print += key + "\t" + value + "\n";
        }
        write(print);
        write("Node Selfish : " + nodeListSelfish);

        super.done();
    }

}
