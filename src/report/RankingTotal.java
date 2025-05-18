package report;

import java.util.*;
import core.*;
import routing.*;
import routing.community.PeopleRank;
import routing.community.RankingNodeValue;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Author Bryan (HaiPigGi)
 */
public class RankingTotal extends Report {
    Map<DTNHost, Integer> totalTeman = new HashMap<>();

    public RankingTotal() {
        init();
    }

    public void done() {
        // Get all hosts in the network
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        // Map<DTNHost, Double> allRankings = new HashMap<>();

        // Iterate through all hosts to calculate total ranking
        // for (DTNHost host : hosts) {
        // // Check if the host has a router and routing decision engine
        // if (host.getRouter() instanceof DecisionEngineRouter) {
        // RoutingDecisionEngine decisionEngine = ((DecisionEngineRouter)
        // host.getRouter()).getDecisionEngine();
        // if (decisionEngine instanceof PeopleRank) {
        // RankingNodeValue rankingNodeValue = (RankingNodeValue) decisionEngine;

        // Map<DTNHost, Double> getRankAll = rankingNodeValue.getAllRankings();

        // totalTeman.put(host, rankingNodeValue.getTotalTeman());

        // // Merge allRankings with the new rankings
        // allRankings.putAll(getRankAll);
        // }
        // }
        // }

        // // Write the total ranking to a CSV file
        // try (FileWriter writer = new FileWriter("ranking_value.csv")) {
        // // Write header
        // writer.append("Host,Last Ranking Value\n");

        // // Write data
        // for (Map.Entry<DTNHost, Double> entry : allRankings.entrySet()) {
        // DTNHost hostEntry = entry.getKey();
        // double rankingEntry = entry.getValue();
        // writer.append(String.format("%s,%.20f\n", hostEntry, rankingEntry));
        // }

        // System.out.println("CSV file has been created successfully.");
        // } catch (IOException e) {
        // System.err.println("Error writing CSV file: " + e.getMessage());
        // e.printStackTrace();
        // }

        // Write the total ranking to the output file
        // Iterate over the map entries to write all rankings to the output file
        write(String.format("%-20s || %s\n", "Host", "Total Friend"));
        write("--------------------------------------------------------\n");
        for (DTNHost host : hosts) {
            if (host.getRouter() instanceof DecisionEngineRouter) {
                RoutingDecisionEngine decisionEngine = ((DecisionEngineRouter) host.getRouter()).getDecisionEngine();
                if (decisionEngine instanceof PeopleRank) {
                    RankingNodeValue rankingNodeValue = (RankingNodeValue) decisionEngine;

                    int totalFriends = rankingNodeValue.getTotalTeman(host);

                    // Write the host and its total friends to the output file
                    write(String.format(" %-20s || %-10s\n", host, totalFriends));
                }
            }
        }
        // for (Map.Entry<DTNHost, Integer> entry : totalTeman.entrySet()) {
        // // DTNHost hostEntry = entry.getKey();
        // // double rankingEntry = entry.getValue();
        // DTNHost host = entry.getKey();
        // int friendList = entry.getValue();
        // // Integer friendList =
        // // Write the ranking to the output file
        // write(String.format(" %-20s || \n", host, friendList));
        // }

        super.done();
    }

}
