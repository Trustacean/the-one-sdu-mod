/*
 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import java.util.*;

import core.*;
import routing.*;
import routing.community.CentralityDetectionEngine;

/**
 * <p>Reports the local communities at each node whenever the done() method is 
 * called. Only those nodes whose router is a DecisionEngineRouter and whose
 * RoutingDecisionEngine implements the 
 * routing.community.CommunityDetectionEngine are reported. In this way, the
 * report is able to output the result of any of the community detection
 * algorithms.</p>
 * 
 * @author PJ Dillon, University of Pittsburgh
 */
public class CentralityDetectionReport extends Report
{
	public CentralityDetectionReport()
	{
		init();
	}

	@Override
	public void done()
	{
		List<DTNHost> nodes = SimScenario.getInstance().getHosts();
//		List<Set<DTNHost>> centrality = new LinkedList<Set<DTNHost>>();
		
		for(DTNHost h : nodes)
		{
			MessageRouter r = h.getRouter();
			if(!(r instanceof DecisionEngineRouter) )
				continue;
			RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
			if(!(de instanceof CentralityDetectionEngine))
				continue;
			CentralityDetectionEngine cd = (CentralityDetectionEngine)de;
			
			double nilaiGlobal = cd.getGlobalDegreeCentrality();
                        double nilaiLocal = cd.getLocalDegreeCentrality();
                        
                        write("Node "+h+" Nilai Global: "+nilaiGlobal+"\tNilai Local: "+nilaiLocal);
		}
		super.done();
	}

	
}