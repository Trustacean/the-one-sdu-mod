package routing;

import java.util.*;

import core.*;

public class SprayAndWaitRouterUpdateForward extends ActiveRouter {
    /** SprayAndFocus Router settings name space ({@value}) */
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouter";
    /** Identifier for the initial number of copies setting ({@value}) */
    public static final String NROF_COPIES_S = "nrofCopies";
    /** Message property key */
    public static final String MSG_COUNT_PROP = SPRAYANDWAIT_NS + "." +
            "copies";

    /** delivery predictability initialization constant */
    public static final double P_INIT = 0.75;
    /** delivery predictability transitivity scaling constant default value */
    public static final double DEFAULT_BETA = 0.25;
    /** delivery predictability aging constant */
    public static final double GAMMA = 0.98;

    /**
     * Transitivity scaling constant (beta) -setting id ({@value}).
     * Default value for setting is {@link #DEFAULT_BETA}.
     */
    public static final String BETA_S = "beta";
    /**
     * Number of seconds in time unit -setting id ({@value}).
     * How many seconds one time unit is when calculating aging of
     * delivery predictions. Should be tweaked for the scenario.
     */
    public static final String SECONDS_IN_UNIT_S = "secondsInTimeUnit";
    protected int initialNrofCopies;
    private double lastAgeUpdate;
    private int secondsInTimeUnit;

    /** value of beta setting */
    private double beta;
    /** initial Structur Data */
    private Map<DTNHost, Double> preds;

    /**
     * Initializes predictability hash
     */
    private void initPreds() {
        this.preds = new HashMap<DTNHost, Double>();
    }

    /** Make the Constructor */
    public SprayAndWaitRouterUpdateForward(Settings s) {
        super(s);
        Settings snf = new Settings(SPRAYANDWAIT_NS);
        initialNrofCopies = snf.getInt(NROF_COPIES_S);
        secondsInTimeUnit = snf.getInt(SECONDS_IN_UNIT_S);

        if (snf.contains(BETA_S)) {
            beta = snf.getDouble(BETA_S);
        } else {
            beta = DEFAULT_BETA;
        }
        initPreds();
    }

    /** make the copy constructor */
    public SprayAndWaitRouterUpdateForward(SprayAndWaitRouterUpdateForward snf) {
        super(snf);
        this.initialNrofCopies = snf.initialNrofCopies;
        this.secondsInTimeUnit = snf.secondsInTimeUnit;
        this.beta = snf.beta;
        initPreds();
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);
        if (con.isUp()) {
            DTNHost otherHost = con.getOtherNode(getHost());
            updateDeliveryPredFor(otherHost);
            updateTransitivePreds(otherHost);
        }
    }

    /**
     * Updates delivery predictions for a host.
     * <CODE>P(a,b) = P(a,b)_old + (1 - P(a,b)_old) * P_INIT</CODE>
     * 
     * @param host The host we just met
     */
    private void updateDeliveryPredFor(DTNHost host) {
        double oldValue = getPredsFor(host);
        double newValue = oldValue + (1 - oldValue) * P_INIT;
        preds.put(host, newValue);
    }

    /**
     * Returns the current prediction (P) value for a host or 0 if entry for
     * the host doesn't exist.
     * 
     * @param host The host to look the P for
     * @return the current P value
     */
    public double getPredsFor(DTNHost host) {
        ageDeliveryPreds();
        if (preds.containsKey(host)) {
            return preds.get(host);
        } else {
            return 0;
        }
    }

    /**
     * Ages all entries in the delivery predictions.
     * <CODE>P(a,b) = P(a,b)_old * (GAMMA ^ k)</CODE>, where k is number of
     * time units that have elapsed since the last time the metric was aged.
     * 
     * @see #SECONDS_IN_UNIT_S
     */
    private void ageDeliveryPreds() {
        double timeDiff = (SimClock.getTime() - this.lastAgeUpdate) / secondsInTimeUnit;

        if (timeDiff == 0) {
            return;
        }

        double mult = Math.pow(GAMMA, timeDiff);
        for (Map.Entry<DTNHost, Double> e : preds.entrySet()) {
            e.setValue(e.getValue() * mult);
        }
        this.lastAgeUpdate = SimClock.getTime();
    }

    /**
     * Updates transitive (A->B->C) delivery predictions.
     * <CODE>P(a,c) = P(a,c)_old + (1 - P(a,c)_old) * P(a,b) * P(b,c) * BETA
     * </CODE>
     * 
     * @param host The B host who we just met
     */
    private void updateTransitivePreds(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof SprayAndWaitRouterUpdateForward
                : "SprayAndWait Only " + "With Other Router of same time";
        double pForHost = getPredsFor(host); // p (a,b)
        Map<DTNHost, Double> othersPreds = ((SprayAndWaitRouterUpdateForward) otherRouter).getDeliveryPreds();

        for (Map.Entry<DTNHost, Double> e : othersPreds.entrySet()) {
            if (e.getKey() == getHost()) {
                continue;
            }

            double pOld = getPredsFor(e.getKey()); // p (a,c)_old
            double pNew = pOld + (1 - pOld) * pForHost * e.getValue();
            preds.put(e.getKey(), pNew);
        }
    }

    /**
     * Returns a map of this router's delivery predictions
     * 
     * @return a map of this router's delivery predictions
     */
    private Map<DTNHost, Double> getDeliveryPreds() {
        ageDeliveryPreds(); // make sure the aging is done
        return this.preds;
    }

    @Override
    public boolean createNewMessage(Message m) {
        makeRoomForMessage(m.getSize());

        m.setTtl(this.msgTtl);
        m.addProperty(MSG_COUNT_PROP, new Integer(initialNrofCopies));
        addToMessages(m, true);
        return true;
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message msg = super.messageTransferred(id, from);
        Integer nrofCopies = (Integer) msg.getProperty(MSG_COUNT_PROP);
        SprayAndWaitRouterUpdateForward other = (SprayAndWaitRouterUpdateForward) from.getRouter();
        assert nrofCopies != null : "Not a SnW Message: " + msg;

        if (nrofCopies > 1) { // is binary

            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else { // if its 1 goes with forwarding prophet algorithm
            // check preds jika per lebih bagus maka kirim
            if (other.getPredsFor(msg.getTo()) > getPredsFor(msg.getTo())) {
                nrofCopies = 1;
            }
        }

        msg.updateProperty(MSG_COUNT_PROP, nrofCopies);
        return msg;
    }

    /**
     * Called just before a transfer is finalized (by
     * {@link ActiveRouter#update()}).
     * Reduces the number of copies we have left for a message.
     * In binary Spray and Wait, sending host is left with floor(n/2) copies,
     * 
     */
    @Override
    protected void transferDone(Connection con) {
        Integer nrofCopies;
        String msgId = con.getMessage().getId();
        /* get this router's copy of the message */
        Message msg = getMessage(msgId);

        if (msg == null) { // message has been dropped from the buffer after..
            return; // ..start of transfer -> no need to reduce amount of copies
        }

        /*
         * reduce the amount of copies left. If the number of copies was at 1 and
         * we apparently just transferred the msg (focus phase), then we should
         * delete it.
         */
        nrofCopies = (Integer) msg.getProperty(MSG_COUNT_PROP);
        if (nrofCopies > 1) { // jika lebih dari 1 (Binary Spray)
            nrofCopies = (int) Math.floor(nrofCopies/2.0); // Binary Spray
        } else {
            nrofCopies--;
        }
        msg.updateProperty(MSG_COUNT_PROP, nrofCopies);
    }

    /**
     * Creates and returns a list of messages this router is currently
     * carrying and still has copies left to distribute (nrof copies > 1).
     * 
     * @return A list of messages that have copies left
     */
    protected List<Message> getMessagesWithCopiesLeft() {
        List<Message> list = new ArrayList<Message>();

        for (Message m : getMessageCollection()) {
            Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROP);
            assert nrofCopies != null : "SnW message " + m + " didn't have " +
                    "nrof copies property!";
            if (nrofCopies > 1) {
                list.add(m);
            }
        }

        return list;
    }

    /**
     * Tries to send all other messages to all connected hosts ordered by
     * their delivery probability
     * 
     * @return The return value of {@link #tryMessagesForConnected(List)}
     */
    private Tuple<Message, Connection> tryOtherMessages() {
        List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>();

        Collection<Message> msgCollection = getMessageCollection();

        /*
         * for all connected hosts collect all messages that have a higher
         * probability of delivery by the other host
         */
        for (Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost());
            SprayAndWaitRouterUpdateForward othRouter = (SprayAndWaitRouterUpdateForward) other.getRouter();

            if (othRouter.isTransferring()) {
                continue; // skip hosts that are transferring
            }

            for (Message m : msgCollection) {
                if (othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }
                tryAllMessagesToAllConnections();
                if (othRouter.getPredsFor(m.getTo()) > getPredsFor(m.getTo())) {
                    // the other node has higher probability of delivery
                    messages.add(new Tuple<Message, Connection>(m, con));
                }
            }
        }

        if (messages.size() == 0) {
            return null;
        }
        // System.out.println(messages);
        // sort the message-connection tuples
        Collections.sort(messages, new TupleComparator());
        return tryMessagesForConnected(messages); // try to send messages
    }

    /**
     * Comparator for Message-Connection-Tuples that orders the tuples by
     * their delivery probability by the host on the other side of the
     * connection (GRTRMax)
     */
    private class TupleComparator implements Comparator<Tuple<Message, Connection>> {

        public int compare(Tuple<Message, Connection> tuple1,
                Tuple<Message, Connection> tuple2) {
            // delivery probability of tuple1's message with tuple1's connection
            double p1 = ((SprayAndWaitRouterUpdateForward) tuple1.getValue().getOtherNode(getHost()).getRouter())
                    .getPredsFor(
                            tuple1.getKey().getTo());
            // -"- tuple2...
            double p2 = ((SprayAndWaitRouterUpdateForward) tuple2.getValue().getOtherNode(getHost()).getRouter())
                    .getPredsFor(
                            tuple2.getKey().getTo());

            // bigger probability should come first
            if (p2 - p1 == 0) {
                /* equal probabilities -> let queue mode decide */
                return compareByQueueMode(tuple1.getKey(), tuple2.getKey());
            } else if (p2 - p1 < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    @Override
    public void update() {
        // Call the base class's update method
        super.update();

        // Check if the router can start a transfer and is not currently transferring
        if (!canStartTransfer() || isTransferring()) {
            return;
        }

        // Try to deliver messages directly to their final recipients
        if (exchangeDeliverableMessages() != null) {
            return;
        }

        // create a list of SAWMessages that have copies left to distribute */
        @SuppressWarnings(value = "unchecked")
        List<Message> copiesLeft = sortByQueueMode(getMessagesWithCopiesLeft());

        if (copiesLeft.size() > 1) {
            /* try to send those messages */
            this.tryMessagesToConnections(copiesLeft, getConnections());
        } else { // if copies left 1
            tryOtherMessages();
        }
    }

    @Override
    public RoutingInfo getRoutingInfo() {
        ageDeliveryPreds();
        RoutingInfo top = super.getRoutingInfo();
        RoutingInfo ri = new RoutingInfo(preds.size() +
                " delivery prediction(s)");

        for (Map.Entry<DTNHost, Double> e : preds.entrySet()) {
            DTNHost host = e.getKey();
            Double value = e.getValue();

            ri.addMoreInfo(new RoutingInfo(String.format("%s : %.6f",
                    host, value)));
        }

        top.addMoreInfo(ri);
        return top;
    }

    @Override
    public MessageRouter replicate() {
        return new SprayAndWaitRouterUpdateForward(this);
    }

}
