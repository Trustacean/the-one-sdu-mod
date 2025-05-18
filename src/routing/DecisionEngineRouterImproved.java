/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import java.util.*;

import core.*;
import static routing.MessageRouter.DENIED_DELIVERED;
import static routing.MessageRouter.DENIED_OLD;
import static routing.MessageRouter.RCV_OK;
import static routing.MessageRouter.TRY_LATER_BUSY;

/**
 *
 * @author Jarkom
 */
public class DecisionEngineRouterImproved extends ActiveRouter {

    public static final String PUBSUB_NS = "DecisionEngineRouterImproved";
    public static final String ENGINE_SETTING = "decisionEngineImproved";
    public static final String TOMBSTONE_SETTING = "tombstones";
    public static final String CONNECTION_STATE_SETTING = "";

    protected boolean tombstoning;
    protected RoutingDecisionEngineImproved decider;
    protected List<Tuple<Message, Connection>> outgoingMessages;

    protected Set<String> tombstones;

    protected Map<Connection, Integer> conStates;

    public DecisionEngineRouterImproved(Settings s) {
        super(s);

        Settings routeSettings = new Settings(PUBSUB_NS);

        outgoingMessages = new LinkedList<Tuple<Message, Connection>>();

        decider = (RoutingDecisionEngineImproved) routeSettings.createIntializedObject(
                "routing." + routeSettings.getSetting(ENGINE_SETTING));

        if (routeSettings.contains(TOMBSTONE_SETTING)) {
            tombstoning = routeSettings.getBoolean(TOMBSTONE_SETTING);
        } else {
            tombstoning = false;
        }

        if (tombstoning) {
            tombstones = new HashSet<String>(10);
        }
        conStates = new HashMap<Connection, Integer>(4);
    }

    public DecisionEngineRouterImproved(DecisionEngineRouterImproved r) {
        super(r);
        outgoingMessages = new LinkedList<Tuple<Message, Connection>>();
        decider = r.decider.replicate();
        tombstoning = r.tombstoning;

        if (this.tombstoning) {
            tombstones = new HashSet<String>(10);
        }
        conStates = new HashMap<Connection, Integer>(4);
    }

    @Override
    public MessageRouter replicate() {
        return new DecisionEngineRouterImproved(this);
    }

    @Override
    public boolean createNewMessage(Message m) {
        if (decider.newMessage(m)) {
            makeRoomForNewMessage(m.getSize());
            m.setTtl(this.msgTtl);
            addToMessages(m, true);

            findConnectionsForNewMessage(m, getHost());
            return true;
        }
        return false;
    }

    @Override
    public void changedConnection(Connection con) {
        DTNHost myHost = getHost();
        DTNHost otherNode = con.getOtherNode(myHost);
        DecisionEngineRouterImproved otherRouter = (DecisionEngineRouterImproved) otherNode.getRouter();
        if (con.isUp()) {
            decider.connectionUp(myHost, otherNode);

            if (shouldNotifyPeer(con)) {
                this.doExchange(con, otherNode);
                otherRouter.didExchange(con);
            }

            Collection<Message> msgs = getMessageCollection();
            for (Message m : msgs) {
                if (decider.shouldSendMessageToHost(m, otherNode)) {
                    outgoingMessages.add(new Tuple<Message, Connection>(m, con));
                }
            }
        } else {
            decider.connectionDown(myHost, otherNode);

            conStates.remove(con);

            for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                    i.hasNext();) {
                Tuple<Message, Connection> t = i.next();
                if (t.getValue() == con) {
                    i.remove();
                }
            }
        }
    }

    protected void doExchange(Connection con, DTNHost otherHost) {
        conStates.put(con, 1);
        decider.doExchangeForNewConnection(con, otherHost);
    }

    protected void didExchange(Connection con) {
        conStates.put(con, 1);
    }

    @Override
    protected int startTransfer(Message m, Connection con) {
        int retVal;

        if (!con.isReadyForTransfer()) {
            return TRY_LATER_BUSY;
        }

        retVal = con.startTransfer(getHost(), m);

        if (retVal == RCV_OK) { // started transfer
            addToSendingConnections(con);
        } else if (tombstoning && retVal == DENIED_DELIVERED) {
            this.deleteMessage(m.getId(), false);
            tombstones.add(m.getId());
        } else if (deleteDelivered && (retVal == DENIED_OLD || retVal == DENIED_DELIVERED)
                && decider.shouldDeleteOldMessage(m, con.getOtherNode(getHost()))) {
            this.deleteMessage(m.getId(), false);
        }

        return retVal;
    }

    @Override
    public int receiveMessage(Message m, DTNHost from) {
        if (isDeliveredMessage(m) || (tombstoning && tombstones.contains(m.getId()))) {
            return DENIED_DELIVERED;
        }

        return super.receiveMessage(m, from);
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message incoming = removeFromIncomingBuffer(id, from);

        if (incoming == null) {
            throw new SimError("No message with ID " + id + " in the incoming "
                    + "buffer of " + getHost());
        }

        incoming.setReceiveTime(SimClock.getTime());

        Message outgoing = incoming;
        for (Application app : getApplications(incoming.getAppID())) {
            outgoing = app.handle(outgoing, getHost());
            if (outgoing == null) {
                break; // Some app wanted to drop the message
            }
        }

        Message aMessage = (outgoing == null) ? (incoming) : (outgoing);

        boolean isFinalRecipient = decider.isFinalDest(aMessage, getHost());
        boolean isFirstDelivery = isFinalRecipient
                && !isDeliveredMessage(aMessage);

        if (outgoing != null && decider.shouldSaveReceivedMessage(aMessage, getHost())) {
// not the final recipient and app doesn't want to drop the message
// -> put to buffer
            addToMessages(aMessage, false);

// Determine any other connections to which to forward a message
            findConnectionsForNewMessage(aMessage, from);
        }

        if (isFirstDelivery) {
            this.deliveredMessages.put(id, aMessage);
        }

        for (MessageListener ml : this.mListeners) {
            ml.messageTransferred(aMessage, from, getHost(),
                    isFirstDelivery);
        }

        return aMessage;
    }

    @Override
    protected void transferDone(Connection con) {
        Message transferred = this.getMessage(con.getMessage().getId());

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (t.getKey().getId().equals(transferred.getId())
                    && t.getValue().equals(con)) {
                i.remove();
                break;
            }
        }

        if (decider.shouldDeleteSentMessage(transferred, con.getOtherNode(getHost()))) {
            this.deleteMessage(transferred.getId(), false);

        }
    }

    @Override
    public void deleteMessage(String id, boolean drop) {
        super.deleteMessage(id, drop);

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (t.getKey().getId().equals(id)) {
                i.remove();
            }
        }
    }

    @Override
    public void update() {
        super.update();

        this.decider.update(this.getHost());
        if (!canStartTransfer() || isTransferring()) {
            return; // nothing to transfer or is currently transferring
        }

        tryMessagesForConnected(outgoingMessages);

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (!this.hasMessage(t.getKey().getId())) {
                i.remove();
            }
        }
    }

    public RoutingDecisionEngineImproved getDecisionEngine() {
        return this.decider;
    }

    protected boolean shouldNotifyPeer(Connection con) {
        Integer i = conStates.get(con);
        return i == null || i < 1;
    }

    protected void findConnectionsForNewMessage(Message m, DTNHost from) {
        for (Connection c : getConnections()) {
            DTNHost other = c.getOtherNode(getHost());
            if (other != from && decider.shouldSendMessageToHost(m, other)) {
                outgoingMessages.add(new Tuple<Message, Connection>(m, c));
            }
        }
    }
}
