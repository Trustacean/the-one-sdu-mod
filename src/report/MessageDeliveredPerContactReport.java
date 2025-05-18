/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.ConnectionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;

/**
 * Show the total of message delivered
 * in the simulation per total contact
 * @author by Gregorius Bima, Sanata Dharma University
 */
public class MessageDeliveredPerContactReport extends Report implements MessageListener, ConnectionListener {

    public static final String totalContact_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int lastRecord;
    private int interval;
    private int totalContact;
    private int nrofDelivered;
    private Map<Integer, Integer> nrofDeliver;

    /** Constructor */
    public MessageDeliveredPerContactReport() {
        init();
        Settings s = getSettings();
        if (s.contains(totalContact_INTERVAL)) {
            interval = s.getInt(totalContact_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.nrofDelivered = 0;
        this.interval = 0;
        this.totalContact = 0;
        this.lastRecord = 0;
        this.nrofDeliver = new HashMap<>();
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            this.nrofDelivered++;
        }
    }

    public void newMessage(Message m) {}

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void done() {
        String statsText = "Contact\tNrofDelivered\n";
        for (Map.Entry<Integer, Integer> entry : nrofDeliver.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            statsText += key + "\t" + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        totalContact++;
        if (totalContact - lastRecord >= interval) {
            lastRecord = totalContact;
            nrofDeliver.put(lastRecord, nrofDelivered);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

}