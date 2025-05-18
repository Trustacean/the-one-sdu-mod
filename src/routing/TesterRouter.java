/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;

/**
 *
 * @author jarkom
 */
public class TesterRouter extends ActiveRouter {

    public static final String LABEL_PROPERTY = "label";
    private String label;

    public TesterRouter(Settings s) {
        super(s);
        if (s.contains(LABEL_PROPERTY)) {
            this.label = s.getSetting(LABEL_PROPERTY);
        } else {
            this.label = "antok";
        }
    }

    public TesterRouter(TesterRouter prototype) {
        super(prototype);
        this.label = prototype.label;
    }

    public boolean createNewMessage(Message m) {
        return super.createNewMessage(m);
        
    }
//

    public void changedConnection(Connection con) {
        super.changedConnection(con);
        DTNHost partner = con.getOtherNode(getHost());
        if (con.isUp()) {
            System.out.println("Koneksi terhubung dengan node" + partner);
        }
    }

    @Override
    public MessageRouter replicate() {
        return new TesterRouter(this);
    }

}
