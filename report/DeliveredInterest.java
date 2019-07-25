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
import routing.DecisionEngineRouter;
import routing.interest.*;
import routing.MessageRouter;

/**
 *
 * @author Junandus Sijabat
 */
public class DeliveredInterest extends Report implements MessageListener {

    private int nrofDeliveredInterest;
    private int nrofRelayed;
    public DeliveredInterest() {
        init();
    }

    @Override
    public void init() {
        super.init();
        this.nrofDeliveredInterest = 0;
        this.nrofRelayed = 0;
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
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        nrofRelayed++;
        MessageRouter otherRouter = to.getRouter();
        DecisionEngineRouter epic = (DecisionEngineRouter)otherRouter;
        if (m.getProperty("interest").equals(epic.getInterest())){ 
        nrofDeliveredInterest++;
        }
    }

    @Override
    public void done() {
        write("Delivered Interest Report");
        int total = this.nrofRelayed - this.nrofDeliveredInterest;
        String statsText = "Relayed Message = " + this.nrofRelayed
                + "\nDelivered Interest = " + this.nrofDeliveredInterest
                + "\nTotal Relayed = " + total;
        write(statsText);
        super.done();
    }
}
