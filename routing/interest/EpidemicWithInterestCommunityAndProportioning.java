package routing.interest;

import core.*;
import java.util.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.*;
import static routing.interest.EpidemicWithInterestCommunity.CONTENT_PROPERTY;

/**
 * @author Junandus Sijabat Sanata Dharma University
 * @author Putranto Suryo Sanata Dharma University
 */
public class EpidemicWithInterestCommunityAndProportioning implements routing.RoutingDecisionEngine, CommunityDetectionEngine {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    public static final String CONTENT_PROPERTY = "interest";
    public static final String ALPHA = "alpha";
    protected CommunityDetection community;
    protected Centrality centrality;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, String> ListInterest;

    public EpidemicWithInterestCommunityAndProportioning(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }
        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new DegreeCentrality(s);
        }

    }

    public EpidemicWithInterestCommunityAndProportioning(EpidemicWithInterestCommunityAndProportioning proto) {
        this.community = proto.community.replicate();
        this.centrality = proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        ListInterest = new HashMap<DTNHost, String>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer  ) {
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
    public void doExchangeForNewConnection(Connection con, DTNHost peer ) {
        DTNHost myHost = con.getOtherNode(peer);
        EpidemicWithInterestCommunityAndProportioning Epic = this.getOtherDecisionEngine(peer);

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
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost ) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        DTNHost thisHost = null;
        double jumlah = 0;
        double receiver = 0;
        double sender = 0;
        double calccentrality = 0;
        double sendercentrality = 0;
        List<DTNHost> listHop = m.getHops();
        Iterator it = listHop.iterator();
        while (it.hasNext()) {
            thisHost = (DTNHost) it.next();
        }
        DecisionEngineRouter peer = (DecisionEngineRouter) (otherHost.getRouter());
        DecisionEngineRouter me = (DecisionEngineRouter) (thisHost.getRouter());
        EpidemicWithInterestCommunityAndProportioning epic = getOtherDecisionEngine(otherHost);
        /*
         */
        if (m.getProperty(CONTENT_PROPERTY).equals(peer.getInterest())) {
            return true;
        } else {
            Set<DTNHost> Ls = epic.getLocalCommunity();
            for (Iterator<DTNHost> hostIterator = Ls.iterator();
                    hostIterator.hasNext();) {
                DTNHost h = hostIterator.next();
                DecisionEngineRouter router = (DecisionEngineRouter) h.getRouter();

                //  System.out.println( router.getInterest());
                calccentrality = calccentrality + getLocalCentrality();
                jumlah++;
                /**
                 * calculate the same interest with peer in peer community
                 */

                if (router.getInterest().equals(peer.getInterest())) {
                    receiver++;
                } else if (router.getInterest().equals(m.getProperty(CONTENT_PROPERTY))) {
                    sendercentrality = sendercentrality + getLocalCentrality();
                    sender++;
                }

            }

            double cent = sendercentrality / calccentrality;
            //double proportion = (sender / (jumlah - receiver));
            double proportion = (sender / jumlah);
            double prosentase = proportion;
            double counting = Math.random();
            // System.out.println("probabilitas di terima " + prosentase + " centrality node " + cent + " proporsi jumlah node " + (samakita / (jumlah - samapeer)));
            if (counting <= prosentase) {
                return true;

            }

        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost ) {
        // DiBuBB allows a node to remove a message once it's forwarded it into the
        // local community of the destination
        EpidemicWithInterestCommunityAndProportioning de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld ) {
        EpidemicWithInterestCommunityAndProportioning de = this.getOtherDecisionEngine(hostReportingOld);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicWithInterestCommunityAndProportioning(this);
    }

    private EpidemicWithInterestCommunityAndProportioning getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicWithInterestCommunityAndProportioning) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
