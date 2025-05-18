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
import core.SimClock;
import core.UpdateListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Afra Rian
 */
public class NumberOfForwardReport extends Report implements MessageListener, UpdateListener {

    public static final String totalContact_Interval = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 3600;
    private double lastRecord;
    private int interval;
    private Map<DTNHost, Integer> forwardCounts;
    private Map<Double, Integer> totalForwardCount;

    public NumberOfForwardReport() {
        init();
        Settings s = getSettings();
        if (s.contains(totalContact_Interval)) {
            interval = s.getInt(totalContact_Interval);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }
    }

    public void init() {
        super.init();
        this.lastRecord = 0;
        this.interval = 0;
        forwardCounts = new HashMap<>();
        totalForwardCount = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {
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
        if (firstDelivery) {
            if (forwardCounts.containsKey(from)) {
                forwardCounts.put(from, forwardCounts.get(from) + 1);
            } else {
                forwardCounts.put(from, 1);
            }
        }
    }

    public void done() {
        String printLn = "Contact\tNumberOfForwards\n";
        for (Map.Entry<Double, Integer> entry : totalForwardCount.entrySet()) {
            Double key = entry.getKey();
            Integer value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            int totalCount = 0;
            for (Map.Entry<DTNHost, Integer> entry : forwardCounts.entrySet()) {
                Integer value = entry.getValue();
                totalCount += value;
            }
            totalForwardCount.put(lastRecord, totalCount);
            lastRecord = SimClock.getTime();
        }
    }

}
