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
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author BramChandra
 */
public class TotalContactListener extends Report implements UpdateListener, ConnectionListener, MessageListener {
    
    
    private Map<String, Double> creationTimes;
    private Map<Integer, Double> connected;
//    private Map<Integer, List<DTNHost>> update;

    public static final String WAKTU_INTERVAL = "updateInterval";
    private int nrofCreated;
    private int interval;
    private int Jinterval;
    private int nrofContact;
    private int jumlahPesanDalamInterval;
    private List<Double> latencies;
    private List<DTNHost> host;
    
    public TotalContactListener() {
        init();
    }

    protected void init() {
        super.init();
        
        this.creationTimes = new HashMap<String, Double>();
        this.connected = new HashMap<Integer, Double>();
        this.nrofCreated = 0;       
        this.Jinterval = 0;       
        this.nrofContact = 0;
        this.jumlahPesanDalamInterval = 0;
        this.latencies = new ArrayList<Double>();
        this.host = new ArrayList<DTNHost>();
    }

    @Override
    public void updated(List<DTNHost> hosts) {        
//        Settings setting = getSettings();
//        interval = setting.getInt(WAKTU_INTERVAL);        
//        Jinterval=Jinterval+interval;        
//        this.update.put(Jinterval, hosts);
//        for (Map.Entry<Integer, List<DTNHost>> entry : update.entrySet()) {
//            Integer key = entry.getKey();
//            List<DTNHost> value = entry.getValue();
//            host=update.get(key);
//            for (int i = 0; i < host.size(); i++) {
//                jumlahPesanDalamIntervalhost.get(i).getNrofMessages();
//                
//            }
//        }
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
          nrofContact++;          
          
          connected.put(nrofContact, 0.0);
    }   

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {

    }

    @Override
    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }
        nrofCreated++;

        this.creationTimes.put(m.getId(), getSimTime());

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
      
     connected.put(nrofContact, (getSimTime()-this.creationTimes.get(m.getId())));
        

    }

    public void done() {
        write("Total Kontak : " + nrofContact);
        write("Kontak ke\tLatency");
        
        for (Map.Entry<Integer, Double> entry : connected.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            if(key%100==0){
                write(key+"\t\t"+value.toString());
            }
        }
        
        super.done();
    }

}
