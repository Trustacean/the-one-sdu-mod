/*
 * @(#)PeopleRank.java
 *
 * Copyright 2024 by Bryan (HaiPigGi)
 * 
 */

package routing.community;

import java.util.*;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import core.Tuple;
import routing.util.*;;

/*
 * PeopleRank
 *
 * PeopleRank is a ranking-based routing decision engine that calculates the importance or centrality of nodes (hosts)
 * within a network by considering the social connections between them. It assigns each node a score (PeopleRank value)
 * based on its connections to other nodes, with higher scores indicating greater importance or influence.
 * This algorithm is particularly suitable for routing in mobile ad hoc networks (MANETs) and Delay Tolerant Networks (DTNs),
 * where traditional routing protocols may not be efficient due to frequent network disruptions and dynamic topology changes.
 *
 * As a ranking-based routing decision engine, PeopleRank utilizes the calculated PeopleRank values to make routing decisions.
 * Messages are routed to nodes with higher PeopleRank values, which are considered to be more central or influential in the network.
 * This helps improve message delivery efficiency, especially in scenarios where traditional routing strategies may not be effective.
 *
 * PeopleRank implementation consists of two main components:
 * 1. Calculation of PeopleRank values for each node in the network.
 * 2. Routing decision based on PeopleRank values to optimize message delivery.
 *
 * The PeopleRank algorithm iteratively computes PeopleRank values for each node by considering the influence
 * of neighboring nodes in the network. The process continues until convergence, where the change in PeopleRank
 * values between iterations falls below a predefined threshold.
 *
 * PeopleRank can be customized with parameters such as damping factor, maximum iterations, and convergence
 * threshold to control the algorithm's behavior and performance. These parameters allow users to fine-tune
 * PeopleRank for specific network characteristics and application requirements.
 *
 * PeopleRank is suitable for various applications, including opportunistic routing in intermittently connected
 * networks, social network analysis, and ranking-based decision making in online social networks.
 *
 * @author Bryan (HaiPigGi)
 * @since 2024
 * 
 */

public class PeopleRank implements RoutingDecisionEngine, RankingNodeValue {
    /** Initialitation variable Dumping Factor to employ -setting id */
    public static final String DUMPING_FACTOR_SETTING = "dumpingFactor";
    public static final String TRESHOLD_SETTING = "threshold";

    /**
     * Map to store the PeopleRank values for each host along with the total number
     * of friends
     */
    protected Map<DTNHost, TupleDe<Double, Integer>> per;
    protected Map<DTNHost, List<Duration>> connHistory; // Store connection history for each host
    protected Map<DTNHost, Double> startTimestamps; // Store the start timestamps for each connection
    protected Set<DTNHost> thisHostSet; // Set to store friends of this host

    // Community detection and damping factor
    protected double dumpingFactor; // Damping factor used in the PeopleRank algorithm
    protected double treshold; // Threshold for considering connections

    /**
     * Constructor for PeopleRank based on the specified settings.
     * 
     * @param s The settings object containing configuration parameters
     */
    public PeopleRank(Settings s) {
        if (s.contains(DUMPING_FACTOR_SETTING)) {
            dumpingFactor = s.getDouble(DUMPING_FACTOR_SETTING);
        } else {
            this.dumpingFactor = 0.85;
        }
        if (s.contains(TRESHOLD_SETTING)) {
            treshold = s.getDouble(TRESHOLD_SETTING);
        } else {
            this.treshold = 700;
        }
        connHistory = new HashMap<DTNHost, List<Duration>>();
        per = new HashMap<>();
        thisHostSet = new HashSet<DTNHost>();
    }

    /**
     * Copy constructor for PeopleRank.
     * 
     * @param r The PeopleRank object to replicate
     */
    public PeopleRank(PeopleRank r) {
        // Replicate damping factor
        this.dumpingFactor = r.dumpingFactor;
        this.treshold = r.treshold;
        startTimestamps = new HashMap<DTNHost, Double>();
        // Initialize a new connection history map
        this.connHistory = new HashMap<DTNHost, List<Duration>>();
        this.thisHostSet = new HashSet<DTNHost>();
        this.per = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        // Get the local host from the connection
        DTNHost myHost = con.getOtherNode(peer);
        // Get the PeopleRank decision engine of the remote host (peer)
        PeopleRank de = this.getOtherDecisionEngine(peer);

        // Update start timestamps for both hosts
        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        // Get the start time of the previous connection and the current time
        double time = getPreviousConnectionStartTime(thisHost, peer);
        double etime = SimClock.getTime();

        /**
         * Check The Total ConnHistory to Find or create the Total Of connection history
         * list
         * 
         */
        List<Duration> history;
        // Check if there is existing connection history for the peer
        if (!connHistory.containsKey(peer)) {
            // If not, create a new list for connection history
            history = new LinkedList<Duration>();
            // Put the new list into the connection history map for the peer
            connHistory.put(peer, history);
        } else {
            // If there is existing history, retrieve it
            history = connHistory.get(peer);
        }

        /**
         * Check if the connection duration is greater than or equal to the familiar
         * threshold
         * If yes, add this connection to the list
         */
        if (etime - time >= treshold) {
            history.add(new Duration(time, etime));
            // Add peer to the friend list of thisHost
            thisHostSet.add(peer);
        }

        /**
         * Update connHistory, Total FriendRank, and totalFriend and save it in per
         * every time connection Down
         */
        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            // Get the host for each entry
            DTNHost host = entry.getKey();

            // Calculate the friend rank for the host
            double friendRank = calculatePer(host);

            // Get the total number of friends for the host (total size of the connection
            // history)
            Set<DTNHost> Fj = new HashSet<>(connHistory.keySet());
            Fj.add(peer); // Add the peer to the set of hosts
            int totalFriends = Fj.size();

            // Create a new tuple with the friend rank and total number of friends
            TupleDe<Double, Integer> tuple = new TupleDe<>(friendRank, totalFriends);

            // Update the tuple in the per map for the host
            per.put(host, tuple);
        }
    }

    /**
     * Check if there is a previous connection start time recorded between this host
     * and a peer.
     * If such a record exists, return the start time of the previous connection,
     * otherwise return 0.
     * 
     * @param thisHost The local host.
     * @param peer     The peer host.
     * @return The start time of the previous connection, or 0 if no record exists.
     */
    public double getPreviousConnectionStartTime(DTNHost thisHost, DTNHost peer) {
        // Check if there is a previous connection start time recorded for this host and
        // peer
        if (startTimestamps.containsKey(thisHost)) {
            // If a record exists, return the start time of the previous connection
            return startTimestamps.get(peer);
        } else {
            // If no record exists, return 0
            return 0;
        }
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new PeopleRank(this);
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    /**
     * Determines whether a message should be sent from this host to another host
     * based on the PeopleRank routing algorithm.
     * 
     * @param m         The message to be sent.
     * @param thisHost  The current host from which the message originates.
     * @param otherHost The destination host to which the message should be sent.
     * @return True if the message should be sent to the other host, false
     *         otherwise.
     */
    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        // Check if the destination of the message is the other host
        if (m.getTo() == otherHost) {
            return true; // Message should be sent directly to the destination
        }
        // Calculate PeopleRank for this host and other host
        double perThisHost = calculatePer(thisHost);
        double perOtherHost = calculatePer(otherHost);

        // Initialize F(i) as the set of friends of i
        Set<DTNHost> Fi = new HashSet<>(connHistory.keySet());
        Fi.add(thisHost);

        // Check if this host is in contact with the other host or already friend
        if (connHistory.containsKey(otherHost) || thisHostSet.contains(otherHost)) {
            // while 1 do
            while (true) {
                // while i is in contact with j do
                for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
                    if (entry.getKey().equals(otherHost)) {
                        Iterator<DTNHost> iterator = Fi.iterator();
                        while (iterator.hasNext()) {
                            DTNHost check = iterator.next();
                            if (otherHost.equals(check)) { // if j ∈ F(i) then
                                // System.out.println("Check Fi : " + check + "other host : " + otherHost);
                                return true;
                            } else if (!check.equals(otherHost)) { // if j !∈ F(i) then
                                // System.out.println("false");
                                return false;
                            }
                        }
                    }
                    // while ∃ m ∈ buffer(i) do
                    Buffer messageBuffer = new Buffer(); // Instantiate Buffer with settings
                    int bufferSize = messageBuffer.getBufferSize(thisHost);
                    while (bufferSize > 0) {
                        if (perOtherHost >= perThisHost || otherHost.equals(m.getTo())) {
                            return true; // Condition met, Forward
                        }
                    }
                }

                // If the destination host is not in contact with the current host, check the
                // end while
            }
        }
        return false; // Otherwise, do not send the message to other host
    }

    /**
     * Calculates the PeopleRank for a given host based on the formula:
     * PeR(Ni) = (1 - d) + d * Σ PeR(Nj) / |F(Nj)|
     * 
     * Where:
     * - PeR(Ni) is the PeopleRank for the current host.
     * - d is the damping factor obtained from the setting. If not specified, it
     * defaults to 0.75.
     * - PeR(Nj) is the ranking of other connected nodes (friends).
     * - |F(Nj)| is the total number of friends of other nodes.
     * 
     * @param host The host for which to calculate the PeopleRank.
     * @return The PeopleRank for the specified host.
     */
    private double calculatePer(DTNHost host) {
        // Get damping factor from settings
        double dampingFactor = this.dumpingFactor;

        double sum = 0.0; // Initialize sum

        // Calculate total number of friends for other nodes
        int totalFriends = 0;
        for (TupleDe<Double, Integer> tuple : per.values()) {
            totalFriends += tuple.getSecond(); // Accumulate total friends
        }

        // Iterate over the connection history
        for (TupleDe<Double, Integer> tuple : per.values()) {
            if (!tuple.getFirst().equals(host)) { // Exclude the host itself
                // Calculate PeR(Nj) by summing up the rankings of its friends
                double friendRanking = tuple.getFirst(); // Ranking of the friend
                int friendsOfOtherHost = tuple.getSecond(); // Total friends of the friend
                if (friendsOfOtherHost > 0) {
                    sum += friendRanking / totalFriends; // Accumulate sum
                }
            }
        }

        // Calculate and return the PeopleRank value according to the formula
        return (1 - dampingFactor) + dampingFactor * sum;
    }

    private PeopleRank getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (PeopleRank) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    /**
     * its for reports
     */
    public Map<DTNHost, Double> getAllRankings() {
        Map<DTNHost, Double> rankings = new HashMap<>();

        // Iterate over the per map to extract rankings
        for (Map.Entry<DTNHost, TupleDe<Double, Integer>> entry : per.entrySet()) {
            DTNHost currentHost = entry.getKey();
            TupleDe<Double, Integer> tuple = entry.getValue();

            // double getRank = entry.getValue();

            // Add the host and its ranking to the map
            rankings.put(currentHost, tuple.getFirst());
        }

        return rankings;
    }

    @Override
    public int getTotalTeman(DTNHost host) {
        DecisionEngineRouter d = (DecisionEngineRouter) host.getRouter();
        PeopleRank othRouter = (PeopleRank) d.getDecisionEngine();
        return othRouter.per.size();
    }

}
