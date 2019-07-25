package routing.interest;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author Junandus Sijabat 
 *  Sanata Dharma University
 */
public class EpidemicWithInterest implements routing.RoutingDecisionEngine {

    public static final String CONTENT_PROPERTY = "interest";

    public EpidemicWithInterest(Settings s) {}

    public EpidemicWithInterest(EpidemicWithInterest proto) {}

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {}

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {}

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {}

    @Override
    public boolean newMessage(Message m, String interest) {
        m.addProperty(CONTENT_PROPERTY, interest);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        DecisionEngineRouter peer = (DecisionEngineRouter) (otherHost.getRouter());
        return m.getProperty(CONTENT_PROPERTY).equals(peer.getInterest());
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    private EpidemicWithInterest getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicWithInterest) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicWithInterest(this);
    }
}
