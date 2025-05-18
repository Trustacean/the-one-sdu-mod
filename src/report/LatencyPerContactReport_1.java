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


public class LatencyPerContactReport_1 extends Report implements MessageListener, ConnectionListener {

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int totalContact;
    private int lastRecord;
    private int interval;
    private Map<Integer, String> nrofLatency;
    private Map<String, Double> creationTimes;
    private List<Double> latencies;

    /**
     * Constructor.
     */
    public LatencyPerContactReport_1() {
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
        this.interval = 0;
        this.lastRecord = 0;
        this.totalContact = 0;
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.nrofLatency = new HashMap<>();
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            double latenciesValue = getSimTime()
                    - this.creationTimes.get(m.getId());
            this.latencies.add(latenciesValue);
        }
    }

    public void newMessage(Message m) {
        this.creationTimes.put(m.getId(), getSimTime());
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void done() {
        String statsText = "Contact\tLatencies\n";
        for (Map.Entry<Integer, String> entry : nrofLatency.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
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
            String latenciesValue = getAverage(latencies);
            nrofLatency.put(lastRecord, latenciesValue);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

}