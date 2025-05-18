/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Afra Rian
 */
public class TransmiteRecieveCountReport extends Report implements MessageListener {
    
    private Map<DTNHost, Integer> receiveTime;
    private Map<DTNHost, Integer> transmitTime;
    
    public TransmiteRecieveCountReport() {
        init();
    }
    
    @Override
    protected void init() {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        receiveTime = new HashMap<>();
        transmitTime = new HashMap<>();
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
        receiveTime.put(to, !receiveTime.containsKey(to) ? 1 : receiveTime.get(to) + 1);
        transmitTime.put(from, !transmitTime.containsKey(from) ? 1 : transmitTime.get(from) + 1);
    }
    
    @Override
    public void done() {
        write("Node\tReceive\n");
        for (Map.Entry<DTNHost, Integer> entry : receiveTime.entrySet()) {
            DTNHost key = entry.getKey();
            Integer value = entry.getValue();
            write(key + "\t" + value);
        }
        
        write("Node\tTransmit\n");
        for (Map.Entry<DTNHost, Integer> entry : transmitTime.entrySet()) {
            DTNHost key = entry.getKey();
            Integer value = entry.getValue();
            write(key + "\t" + value);
        }
        super.done(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
