/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.DecisionEngineRouter;

/**
 * 
 * @author Afra Rian Yudianto, Sanata Dharma University
 */
public class FuzzyBasedRouter implements RoutingDecisionEngine{

    public static final String FCL_SIMILARITY = "fclSimilarity";
    public static final String CLOSENESS = "closeness";
    public static final String VARIANCE = "variance";
    public static final String TRANSFER_OF_UTILITY = "su";

    private FIS fclSimilarity;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
        
    public FuzzyBasedRouter(Settings s) {
        String fclString = s.getSetting(FCL_SIMILARITY);
        fclSimilarity = FIS.load(fclString);
    }

    public FuzzyBasedRouter(FuzzyBasedRouter t) {
        this.fclSimilarity = t.fclSimilarity;
        startTimestamps = new HashMap<>();
        connHistory = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list
        double getLastDisconnect = 0;
        if (startTimestamps.containsKey(peer)) {
            getLastDisconnect = startTimestamps.get(peer);
        }
        double currentTime = SimClock.getTime();

        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);

        } else {
            history = connHistory.get(peer);

        }

//         add this connection to the list
        if (currentTime - getLastDisconnect > 0) {
            history.add(new Duration(getLastDisconnect, currentTime));

        }
        connHistory.put(peer, history);
        this.startTimestamps.remove(peer);
        
        
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        this.startTimestamps.put(peer, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        return true;    
    }
    
    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        
        DTNHost dest = m.getTo();
        FuzzyBasedRouter de = getOtherDecisionEngine(otherHost);
        double me = this.Defuzzification(dest);
        double peer = de.Defuzzification(dest);
        return me > peer;
    }
    
    
    private double Defuzzification(DTNHost nodes) {
        double closenessValue = getClosenessOfNodes(nodes);
        double varianceValue = getNormalizedVarianceOfNodes(nodes);
        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock(null);

        functionBlock.setVariable(CLOSENESS, closenessValue);
        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    public double getVarianceOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += Math.pow((d.end - d.start) - mean, 2);
        }
        return temp / list.size();
    }

    public double getNormalizedVarianceOfNodes(DTNHost nodes) {
        double k = getList(nodes).size();
        double N = 0;
        double sigmf = 0;
        Iterator<Duration> iterator = getList(nodes).iterator();
        while (iterator.hasNext()) {
            Duration duration = iterator.next();
            double timeDuration = (duration.end - duration.start);
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        Double d = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));
        return d;
    }

    public List<Duration> getList(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    private double getClosenessOfNodes(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparationOfNodes(nodes);
        double variansi = getVarianceOfNodes(nodes);
        Double c = Math.exp(-(Math.pow(rataShortestSeparation, 2) / (2 * variansi)));
        return c;
    }

    public double getAverageShortestSeparationOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / list.size();
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new FuzzyBasedRouter(this);
    }

    private FuzzyBasedRouter getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FuzzyBasedRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
     
    @Override
    public void update(DTNHost thisHost){
    }    

}
