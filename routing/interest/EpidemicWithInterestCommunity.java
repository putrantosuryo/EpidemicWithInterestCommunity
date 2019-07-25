package routing.interest;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.*;

/**
 * @author Junandus Sijabat Sanata Dharma University
 */
public class EpidemicWithInterestCommunity implements routing.RoutingDecisionEngine, CommunityDetectionEngine, Tombstone {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CONTENT_PROPERTY = "interest";
    protected CommunityDetection community;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, String> ListInterest;
    private Set<String> tombstone;

    public EpidemicWithInterestCommunity(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }
        tombstone = new HashSet<>();

    }

    public EpidemicWithInterestCommunity(EpidemicWithInterestCommunity proto) {
        this.community = proto.community.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        ListInterest = new HashMap<DTNHost, String>();
        tombstone = new HashSet<>();

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        DecisionEngineRouter otherHost = (DecisionEngineRouter) (peer.getRouter());
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();

        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        if (etime - time > 0) {
            history.add(new Duration(time, etime));
            if (!ListInterest.containsKey(peer)) {
                ListInterest.put(peer, otherHost.getInterest());
            }
            //System.out.println(ListInterest);
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;
        community.connectionLost(thisHost, peer, peerCD, history);
        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        EpidemicWithInterestCommunity Epic = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        Epic.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, Epic.community);
    }

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
        DTNHost thisHost = null;
        List<DTNHost> listHop = m.getHops();
        Iterator it = listHop.iterator();
        while (it.hasNext()) {
            thisHost = (DTNHost) it.next();
        }

        DecisionEngineRouter peer = (DecisionEngineRouter) (otherHost.getRouter());
        DecisionEngineRouter me = (DecisionEngineRouter) (thisHost.getRouter());
        EpidemicWithInterestCommunity epic = getOtherDecisionEngine(otherHost);
        /*
         */
        if (m.getProperty(CONTENT_PROPERTY).equals(peer.getInterest())) {
            return true;
        }
        Set<DTNHost> Ls = epic.getLocalCommunity();
        for (DTNHost L : Ls) {
            if ((L.getAddress() != thisHost.getAddress() && L.getRouter().getInterest().equals(m.getProperty(CONTENT_PROPERTY)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicWithInterestCommunity(this);
    }

    private EpidemicWithInterestCommunity getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicWithInterestCommunity) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Set<String> getTombstone() {
        return tombstone;
    }
}
