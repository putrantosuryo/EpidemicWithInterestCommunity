package report;

import java.util.*;
import core.*;

public class CarriedMessageToDestinationReport extends Report implements MessageListener{
    protected Map<DTNHost, Integer> carried = new HashMap<>();

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (m.getTo() == to && firstDelivery) {
            List<DTNHost> hopNodes = m.getHops();
            Iterator it = hopNodes.iterator();

            DTNHost r = null;
            while (it.hasNext()) {
                DTNHost c = (DTNHost) it.next();
                if (c != m.getTo())
                    r = c;

            }


            if (carried.containsKey(r))
                carried.replace(r,carried.get(r)+ 1);
            else
                carried.put(r,1);



        }

    }


    @Override
    public void done()
    {
        write("Node"+'\t'+"Value");
        for(Map.Entry<DTNHost, Integer> entry : carried.entrySet())
        {
            DTNHost a=entry.getKey();
            Integer b=a.getAddress();

            write("" + b + '\t' + entry.getValue());
        }
        super.done();
    }

    public void newMessage(Message m) {}
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
}
