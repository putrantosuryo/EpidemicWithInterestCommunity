package routing.interest;

import core.*;
import java.util.*;
import javax.print.attribute.HashAttributeSet;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.*;
import static routing.interest.EpidemicWithInterestCommunity.CONTENT_PROPERTY;

/**
 * @author Putranto Suryo Sanata Dharma University
 * Kelas ini digunakan untuk algoritma Epidemicwith interest community dengan penambahan centrality di dalam 
 * penyebaran pesan ketika node sudah berada dalam satu komunitas
 * pemberian interest tiap pesan berada di method newMessage
 * Generate interest berada di kelas MessageRouter
 * Penggunaan tombstone untuk menjaga tidak terjadinya flooding penyebaran pesan dalam komunitas
 */
public class EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality implements routing.RoutingDecisionEngine, CommunityDetectionEngine, Tombstone {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    public static final String CONTENT_PROPERTY = "interest";
    protected CommunityDetection community;
    protected Centrality centrality;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, String> ListInterest;
    private Set<String> tombstone;

    public EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }
        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new CWindowCentrality(s);
        }
        tombstone = new HashSet<>();
    }

    public EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality(EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality proto) {
        this.community = proto.community.replicate();
        this.centrality = proto.centrality.replicate();
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
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;
        community.connectionLost(thisHost, peer, peerCD, history);
        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality Epic = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        Epic.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, Epic.community);
    }

    @Override
    public boolean newMessage(Message m, String interest) {
        m.addProperty(CONTENT_PROPERTY, interest);
        this.tombstone.add(m.getId());
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Collection<Message> messageCollection = thisHost.getMessageCollection();
        for (Message message : messageCollection) {
            if (m.getId().equals(message.getId())) {
                return false;
            }
        }
        this.tombstone.add(m.getId());
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
        EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality epic = getOtherDecisionEngine(otherHost);

        /*
         */
        if (m.getProperty(CONTENT_PROPERTY).equals(peer.getInterest())) {
            return true;
        }

        Set<DTNHost> Ls = epic.getLocalCommunity();
        //jika node ada pada community yang sama

        if (Ls.contains(thisHost)) {
            for (DTNHost K : Ls) {
                if ((K.getAddress() != thisHost.getAddress() && K.getRouter().getInterest().equals(m.getProperty(CONTENT_PROPERTY)))) {
                    if (!epic.tombstone.contains(m)) {
                        return true;
                    }
                } else if (epic.getLocalCentrality() > this.getLocalCentrality()) {
                    if (!epic.tombstone.contains(m)) {
                        return true;
                    }
                }
            }
        }

        //jika node ada pada community yang berbeda
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        if (this.commumesWithHost(otherHost)) {
            String messageInterest = (String) m.getProperty(CONTENT_PROPERTY);
            String peerInterest = otherHost.getRouter().getInterest();
            if (messageInterest.equals(peerInterest)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
        return true;
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
        return new EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality(this);
    }

    private EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicWithInterestCommunityAndLocalCommunityImprovedCentrality) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Set<String> getTombstone() {
        return tombstone;
    }

}
