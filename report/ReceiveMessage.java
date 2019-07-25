/*
 * Copyright (c) 2018. Improving SimBet algorithm forwarding mechanism
 * using Delegation Forwarding & Simulated Annealing.
 * using Delegation Forwarding.
 * author Rafelino Claudius Kelen, Sanata Dharma University.
*/

package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiveMessage extends Report implements MessageListener{


    private Map<DTNHost,Integer> receivedCounts;

    public ReceiveMessage(){ init(); }

    @Override
    public void init(){
        super.init();
        this.receivedCounts=new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {}

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        List<DTNHost> hopNodes=m.getHops();
        if(hopNodes.get(hopNodes.size()-1)!= m.getTo()){
            DTNHost receivedNode=hopNodes.get(hopNodes.size()-1);
            if(receivedCounts.containsKey(receivedNode)){
                receivedCounts.put(receivedNode,receivedCounts.get(receivedNode)+1);
            }else{
                receivedCounts.put(receivedNode, 1);
            }
        }
    }

    @Override
    public void done() {
        for(Map.Entry<DTNHost, Integer> entry : receivedCounts.entrySet())
        {
            DTNHost a=entry.getKey();
            Integer b=a.getAddress();
            write(b+"\t"+entry.getValue());
        }
        super.done();
    }
    public void messageSaveToBuffer(Message m, DTNHost to) { }
}
