package routing.interest;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * @author Junandus Sijabat
 * Sanata Dharma University
 */
public class EpidemicWithoutInterest implements routing.RoutingDecisionEngine {

    public static final String CONTENT_PROPERTY = "interest";

    public EpidemicWithoutInterest(Settings s) {}

    public EpidemicWithoutInterest(EpidemicWithoutInterest proto) {}

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {}

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer  ) {}

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer  ) {}

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
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost ) {
        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld ) {
        return true;
    }
    
    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicWithoutInterest(this);
    }
    private EpidemicWithoutInterest getOtherDecisionEngine(DTNHost h){
        MessageRouter otherRouter = h.getRouter();
		assert otherRouter instanceof DecisionEngineRouter : "This router only works " + 
		" with other routers of same type";
		
	return (EpidemicWithoutInterest) ((DecisionEngineRouter)otherRouter).getDecisionEngine();
    }
}