package routing.community;

import core.DTNHost;
import java.util.*;

/**
 * Author Bryan (HaiPigGi)
 */
public interface RankingNodeValue {

    /**
     * Get all rankings for all nodes with the latest ranking.
     * 
     * @return A map containing the rankings for all nodes.
     */
    public Map<DTNHost, Double> getAllRankings();

    public int getTotalTeman(DTNHost host);
}
