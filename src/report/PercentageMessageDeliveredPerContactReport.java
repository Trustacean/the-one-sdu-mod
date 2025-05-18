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
public class PercentageMessageDeliveredPerContactReport extends Report implements MessageListener, ConnectionListener {

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int lastRecord;
    private int interval;
    private int totalContact;
    private int nrofCreated;
    private int nrofDelivered;
    private Map<Integer, Double> nrofDeliver;

    /**
     * Constructor
     */
    public PercentageMessageDeliveredPerContactReport() {
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
        this.nrofDelivered = 0;
        this.lastRecord = 0;
        this.totalContact = 0;
        this.nrofDeliver = new HashMap<>();
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            this.nrofDelivered++;
        }
    }

    public void newMessage(Message m) {
        this.nrofCreated++;
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void done() {
        String statsText = "Contact\tNrofDelivered\n";
        for (Map.Entry<Integer, Double> entry : nrofDeliver.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
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
            double deliveryPercentage = ((1.0 * this.nrofDelivered) / this.nrofCreated) * 100;
            nrofDeliver.put(lastRecord, deliveryPercentage);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

    
}
