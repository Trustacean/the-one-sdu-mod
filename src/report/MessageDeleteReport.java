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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

/**
 *
 * @author BramChandra
 */
public class MessageDeleteReport extends Report implements MessageListener {

    private Map<DTNHost, Integer> deleteMessage;
    private int nrofDropped;
    private int nrofRemoved;
//    private List<Integer> hopCounts;

    public MessageDeleteReport() {
        init();
    }

    public void init() {
        super.init();
        deleteMessage = new HashMap<DTNHost, Integer>();
        nrofDropped = 0;
        nrofRemoved = 0;
        //   this.hopCounts = new ArrayList<Integer>();
    }

    @Override
    public void newMessage(Message m) {

    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {

        if (deleteMessage.containsKey(where)) {
            deleteMessage.put(where, deleteMessage.get(where) + 1);
        } else {
            deleteMessage.put(where, 1);
        }
//        nrofRemoved++;

    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {

    }

    public void done() {
        write("Host\tPesan di delete");

        for (Map.Entry<DTNHost, Integer> entry : deleteMessage.entrySet()) {
            DTNHost key = entry.getKey();
            Integer value = entry.getValue();
            write(key + "\t" + value);
        }
        write("" + nrofRemoved);
        super.done();
    }

}
