/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.*;

/**
 *
 * @author Jarkom
 */
public interface RoutingDecisionEngineImproved {

    public void connectionUp(DTNHost thisHost, DTNHost peer);

    public void connectionDown(DTNHost thisHost, DTNHost peer);

    public void doExchangeForNewConnection(Connection con, DTNHost peer);

    public boolean newMessage(Message m);

    public boolean isFinalDest(Message m, DTNHost aHost);

    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost);

    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost);

    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost);

    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld);
    
    public void update(DTNHost thisHost);

    public RoutingDecisionEngineImproved replicate();
}
