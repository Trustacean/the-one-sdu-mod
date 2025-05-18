/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.ConnectionListener;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Afra Rian
 */
public class MessageCopyCountReport extends Report implements ConnectionListener, MessageListener {

    public static final String totalContact_Interval = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int lastRecord;
    private int interval;
    private int totalContact;
    private double msgCreated;
//    private int totalHops;
//    private Map<Message, Integer> copyMsg;
    private Map<Message, CopyCountPerHopHelper> nrofHops;
    private Map<Integer, Double> nrofAverageHops;

    public MessageCopyCountReport() {
        init();
        Settings s = getSettings();
        if (s.contains(totalContact_Interval)) {
            interval = s.getInt(totalContact_Interval);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }

    }

    @Override
    public void init() {
        super.init();
//        this.totalHops = 0;
        this.interval = 0;
        this.msgCreated = 0;
        this.totalContact = 0;
        this.lastRecord = 0;
        this.nrofHops = new HashMap<>();
        this.nrofAverageHops = new HashMap<>();
//        this.copyMsg = new HashMap<Message, Integer>();
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        totalContact++;
        if (totalContact - lastRecord >= interval) {
            double totalCopy = 0 ;
            for (Map.Entry<Message, CopyCountPerHopHelper> entry : nrofHops.entrySet()) {
                CopyCountPerHopHelper value = entry.getValue();
                totalCopy += value.copyMsg;
            }
            nrofAverageHops.put(totalContact, totalCopy);
            lastRecord = totalContact;
//            write(totalContact + " ,total hops " + totalHops + " total M " + totalMsg);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

    @Override
    public void done() {
        String printLn = "Contact\tCopyMsg\n";
        for (Map.Entry<Integer, Double> entry : nrofAverageHops.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);

//        String printLn = "Contact\tAverageNrofHops\n";
//        for (Map.Entry<Message, Integer> entry : copyMsg.entrySet()) {
//            Message key = entry.getKey();
//            Integer value = entry.getValue();
//            printLn += key + "\t" + value + "\n";
//        }
//        write(printLn);
        super.done();
    }

    @Override
    public void newMessage(Message m) {
        this.msgCreated++;
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        CopyCountPerHopHelper cp = null;
        if (nrofHops.containsKey(m)) {
            cp = nrofHops.get(m);
            int hop = cp.hopCount;
            int copy = cp.copyMsg;
            nrofHops.put(m, new CopyCountPerHopHelper(hop + m.getHopCount(), copy + 1));
        } else {
            nrofHops.put(m, new CopyCountPerHopHelper(m.getHopCount(), 1));
        }
//        write(m + " has " + m.getHopCount() + " from " + from);
    }

}
