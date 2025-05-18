/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.ConnectionListener;
import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Afra Rian
 */
public class TotalContactReport extends Report implements ConnectionListener {

    private double lastRecord;
    private int interval = 3600;
    private Map<DTNHost, Integer> listHost;

    public TotalContactReport() {
        super.init();
        lastRecord = 0;
        listHost = new HashMap<>();
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        listHost.put(host1, !listHost.containsKey(host1) ? 1 : listHost.get(host1) + 1);
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
    }

    @Override
    public void done() {
        String print = "Host\tTotalContact\n";
        for (Map.Entry<DTNHost, Integer> entry : listHost.entrySet()) {
            DTNHost key = entry.getKey();
            Integer value = entry.getValue();
            print += key + "\t" + value + "\n";
        }
        write(print);
        super.done();
    }

}
