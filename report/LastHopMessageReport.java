package report;

import java.util.*;
import core.*;

public class LastHopMessageReport extends Report implements MessageListener {
    public static String HEADER = "PESAN LASTHOP WAKTU PATH";

    public LastHopMessageReport() {
        init();
    }

    private String getPathString(Message m) {
        List<DTNHost> hops = m.getHops();
        String str = m.getFrom().toString();

        for (int i=1; i<hops.size(); i++) {
            str += "->" + hops.get(i);
        }

        return str;
    }

    @Override
    public void init() {
        super.init();
        write(HEADER);

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
        if (m.getTo() == to && firstDelivery) {
            List<DTNHost> hopNodes = m.getHops();
            Iterator it = hopNodes.iterator();



            DTNHost r = null;


            while (it.hasNext()) {
            DTNHost c = (DTNHost) it.next();
                if (c != m.getTo())
                    r = c;
            }

            write("" + m + '\t' + r.getAddress() + '\t' + SimClock.getTime()+'\t'+getPathString(m));

        }

    }


    @Override
    public void done() {
        super.done();
    }
}
