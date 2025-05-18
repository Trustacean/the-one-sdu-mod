/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimError;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import routing.fuzzy.FuzzyBasedRouter;

/**
 *
 * @author Afra Rian
 */
public class SelfishWithCommunityRouter implements RoutingDecisionEngine {

    public static final String PATH_SETTING = "filePathKnowledge";

    private Scanner reader;
    private int interval = 3600;
    private Double lastRecord = Double.MIN_VALUE;
    private List<LinkedList<String>> communityGlobal;
    private String eeFilePath;
    private Map<DTNHost, Integer> nodeRank;

    public SelfishWithCommunityRouter(Settings s) {
        eeFilePath = s.valueFillString(s.getSetting(PATH_SETTING));
    }

    public SelfishWithCommunityRouter(SelfishWithCommunityRouter proto) {
        this.eeFilePath = proto.eeFilePath;
        this.nodeRank = new HashMap<>();
        this.communityGlobal = new LinkedList<>();
        readExternalCommunity();
    }

    private void readExternalCommunity() {
        Pattern skipPattern = Pattern.compile("(#.*)|(^\\s*$)");
        try {
            this.reader = new Scanner(new FileReader(eeFilePath));
        } catch (FileNotFoundException ex) {
            throw new SimError(ex.getMessage(), ex);
        }

        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            Scanner lineScan = new Scanner(line);
            if (skipPattern.matcher(line).matches()) {
                continue;
            }

            LinkedList<String> communityList = new LinkedList<>();
            while (lineScan.hasNext()) {
                String community = lineScan.next();
                communityList.add(community);
            }
            if (!communityGlobal.contains(communityList)) {
                communityGlobal.add(communityList);
            } else {
                continue;
            }
        }
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
//        System.out.println("Selfish thisHost : " +thisHost);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
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
        for (LinkedList<String> community : communityGlobal) {
            if (community.contains(thisHost.toString()) && community.contains(m.getTo().toString())) {
                return !thisHost.getRouter().hasMessage(m.getId());
            }
        }
        return false;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
//        for (LinkedList<String> community : communityGlobal) {
//            if (community.contains(thisHostNow.toString()) && community.contains(otherHost.toString())) {
//                return true;
//            }
//        }
        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SelfishWithCommunityRouter(this);
    }

    @Override
    public void update(DTNHost thisHost) {
    }
    
    private SelfishWithCommunityRouter getDecisionEngine(DTNHost h) {
        MessageRouter thisRouter = h.getRouter();
        assert thisRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SelfishWithCommunityRouter) ((DecisionEngineRouter) thisRouter).getDecisionEngine();
    }
}
