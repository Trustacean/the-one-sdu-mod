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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jarkom
 */
public class OverheadPerContactReport_1 extends Report implements MessageListener, ConnectionListener {

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int lastRecord;
    private int interval;
    private int totalContact;
    private int nrofRelayed;
    private int nrofDelivered;
    private Map<Integer, Double> nrofOverhead;

    /** Constructor */
    public OverheadPerContactReport_1() {
        init();
        Settings s = getSettings();
        if (s.contains(TOTAL_CONTACT_INTERVAL)) {
            interval = s.getInt(TOTAL_CONTACT_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.lastRecord = 0;
        this.totalContact = 0;
        this.nrofRelayed = 0;
        this.nrofDelivered = 0;
        this.nrofOverhead = new HashMap<>();
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {

        this.nrofRelayed++;
        if (finalTarget) {
            this.nrofDelivered++;
        }
    }

    public void newMessage(Message m) {
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void done() {
        String statsText = "Overhead/Contact\n";
        for (Map.Entry<Integer, Double> entry : nrofOverhead.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            statsText += key + " " + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        totalContact++;
        if (totalContact - lastRecord >= interval) {
            lastRecord = totalContact;
            double overHead = Double.NaN;	// overhead ratio

            if (this.nrofDelivered > 0) {
                overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered))
                        / this.nrofDelivered;
            }
            nrofOverhead.put(lastRecord, overHead);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

}