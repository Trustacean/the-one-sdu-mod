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
import core.SimError;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import report.NodeRankHelper;

/**
 *
 * @author Afra Rian
 */
public class EpidemicWithCommunityRouter implements RoutingDecisionEngine, NodeRankHelper {

    public static final String PATH_SETTING = "filePathKnowledge";

    public static final String NODE_SELFISH = "nodeAddress";

    private int numOfRnd = 0;

    private Scanner reader;
    private int interval = 3600;
    private Double lastRecord = Double.MIN_VALUE;
    private List<LinkedList<String>> communityGlobal;
    private String eeFilePath;
    private Map<String, Integer> nodeRank;
    private int[] nodeSelfish;
    private List<String> msgId;
    private LinkedList<Integer> nodeList;

    public EpidemicWithCommunityRouter(Settings s) {
        eeFilePath = s.valueFillString(s.getSetting(PATH_SETTING));
        if (s.contains(NODE_SELFISH)) {
            numOfRnd = 0;
            nodeSelfish = s.getCsvInts(NODE_SELFISH, 5);
        } else {
            numOfRnd = 1;
        }
    }

    public EpidemicWithCommunityRouter(EpidemicWithCommunityRouter proto) {
        this.eeFilePath = proto.eeFilePath;
        this.nodeRank = new HashMap<>();
        this.communityGlobal = new LinkedList<>();
        this.nodeSelfish = proto.nodeSelfish;
        this.msgId = new LinkedList<>();
        this.nodeList = proto.nodeList;
        this.numOfRnd = proto.numOfRnd;
//        readSelfishnes();
        readExternalCommunity();
    }

    private void readSelfishnes() {
        nodeList = new LinkedList<>();
        if (numOfRnd == 0) {
            for (int i = 0; i < nodeSelfish.length; i++) {
                if (!nodeList.contains(nodeSelfish[i])) {
                    nodeList.add(nodeSelfish[i]);
                } else {
                    return;
                }
            }
//            System.out.println("static"+nodeList);
        } else {
            //error while calling method 
            Random rnd = new Random();
            for (int i = 0; i < 5; i++) {
                if (nodeList.size() != 5) {
                    nodeList.add(rnd.nextInt(97));
                } else {
                    return;
                }
            }
//            System.out.println("random"+nodeList);
        }
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
                return;
            }
        }
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        nodeList = thisHost.getRouter().nodeList;
        System.out.println(this.nodeList);
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
        if (nodeList.contains(thisHost.getAddress())) {
            for (LinkedList<String> community : communityGlobal) {
                if (community.contains(thisHost.toString()) && community.contains(m.getTo().toString())) {
                    return m.getTo() != thisHost;
                }
            }
        } else {
            this.analysMsgOnBuffer(thisHost);
            return m.getTo() != thisHost;
        }
        return false;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
//        if (nodeList.contains(thisHost.getAddress())) {
//            for (LinkedList<String> community : communityGlobal) {
//                if (community.contains(thisHost.toString()) && community.contains(otherHost.toString())) {
//                    return true;
//                }
//            }
//        } else {
//            return true;
//        }
//        return false;
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
        return new EpidemicWithCommunityRouter(this);
    }

    private void analysMsgOnBuffer(DTNHost thisHost) {
        for (Message m : thisHost.getMessageCollection()) {
            if (!msgId.contains(m.getId())) {
                for (int i = 0; i < m.getHops().size(); i++) {
                    if (i + 1 < m.getHops().size()) {
                        if (i == 0) {
                            continue;
                        }
                        DTNHost host1 = m.getHops().get(i);
                        DTNHost host2 = m.getHops().get(i + 1);
                        boolean note = false;
                        for (LinkedList<String> community : communityGlobal) {
                            if (!community.contains(host1.toString()) && community.contains(host2.toString())) {
//                                nodeRank.put(host1.toString(), !nodeRank.containsKey(host1.toString()) ? 1 : nodeRank.get(host1.toString()) + 1);
                                note = true;
                            } else if (community.contains(host1.toString()) && !community.contains(host2.toString())) {
//                                nodeRank.put(host1.toString(), !nodeRank.containsKey(host1.toString()) ? 1 : nodeRank.get(host1.toString()) + 1);
                                note = true;
                            } else if (!community.contains(host1.toString()) && !community.contains(host2.toString())) {
                                note = false;
                            } else {
                                note = false;
                                break;
                            }
                        }
                        if (note == true) {
                            nodeRank.put(host1.toString(), !nodeRank.containsKey(host1.toString()) ? 1 : nodeRank.get(host1.toString()) + 1);
                            msgId.add(m.getId());
                        }
                    } else {
                        break;
                    }
                }
            } else {
                continue;
            }
        }
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    @Override
    public Map<String, Integer> getNodeRank() {
        return this.nodeRank;
    }

    @Override
    public List<Integer> getNodeSelfish() {
        return this.nodeList;
    }

}
