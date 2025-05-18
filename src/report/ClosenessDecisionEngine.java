/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public interface ClosenessDecisionEngine {

    public Map<DTNHost, List<Double>> getCloseness();


}
