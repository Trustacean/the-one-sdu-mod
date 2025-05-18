/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

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
public class NumberOfHopsPerMsgPerTimeReport extends Report implements MessageListener, UpdateListener {

    public static final String totalContact_Interval = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 3600;
    private Double lastRecord;
    private int interval;
    private int totalContact;
    private Map<Message, Integer> nrofHops;
    private Map<Double, Double> nrofAverageHops;

    public NumberOfHopsPerMsgPerTimeReport() {
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
        this.interval = 0;
        this.lastRecord = Double.MIN_VALUE;
        this.totalContact = 0;
        this.nrofHops = new HashMap<>();
        this.nrofAverageHops = new HashMap<>();
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
            nrofHops.put(m, m.getHopCount());
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
//        String printLn = "";
        if (SimClock.getTime() - lastRecord >= interval) {
            double totalMsg = 0;
            double totalHopCounts = 0;
            for (Map.Entry<Message, Integer> entry : nrofHops.entrySet()) {
                totalMsg++;
                Integer value = entry.getValue();
                totalHopCounts += value;
            }
            double averagePerMsg = totalHopCounts / totalMsg;
//            printLn += SimClock.getTime() + "\t" + averagePerMsg + "\n";
            nrofAverageHops.put(SimClock.getTime(), averagePerMsg);
//            write(printLn);
            lastRecord = SimClock.getTime();
        }
    }

    public void done() {
        String printLn = "Contact\tAverageNrofHops\n";
        for (Map.Entry<Double, Double> entry : nrofAverageHops.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }

}
