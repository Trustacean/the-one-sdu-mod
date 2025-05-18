/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimScenario;
import core.UpdateListener;
import routing.ActiveRouter;
import routing.CCRouting;
import routing.MessageRouter;
import routing.NodeConn;
import routing.community.Duration;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P>
 * <strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class TestingReport extends Report {
	// private Map<String, Double> creationTimes;
	// private List<Double> latencies;
	// private List<Integer> hopCounts;
	// private List<Double> msgBufferTime;
	// private List<Double> rtt; // round trip times

	private Map<DTNHost, Integer> totalReceivePerNode;
	private Map<DTNHost, Integer> totalTransferPerNode;

	/**
	 * Constructor.
	 */
	public TestingReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();

		this.totalReceivePerNode = new HashMap<DTNHost, Integer>();
		this.totalTransferPerNode = new HashMap<DTNHost, Integer>();
	}

	@Override
	public void done() {
		write("Message stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		write("Testing untuk node p0");
		
		List<DTNHost> nodes = SimScenario.getInstance().getHosts();
		write("Total nodes = " + nodes.size());

		StringBuilder tes = new StringBuilder();
		for (DTNHost d : nodes) {
			// tes += d + " connected with " + d.setNode.size() + " " + d.setNode.toString()
			// + "\n";
			// tes += d + " : " + d.intervals.size() + " " + d.getNodeIntervals() + "\n";
			// tes += d + " : " + d.getBufferOccupancy() + "\n";
			// tes += d + ", " + d.dataReceivedInDuration.size() + "\n";
			// tes += d + ", " + d.msgReceived + "\n";
			// tes += d + ", " + d.msgTransferred + "\n";
			// MessageRouter r = d.getRouter();
			// ActiveRouter ac = (ActiveRouter) r;
			// CCRouting router = (CCRouting) ac;
			// ActiveRouter ar = (ActiveRouter) r;
			// tes.append(d + ", " + router.getMsgReceived() + "\n");
			
				CCRouting router = (CCRouting) d.getRouter();
				// NodeConn in = (NodeConn) router;
			// if(d.toString().equals("p18")) {
			// 	CCRouting router = (CCRouting) d.getRouter();
			// 	Map<DTNHost, List<Double>> conTime = router.getNodeConn();
			// 	tes.append(d + " with ");
			// 	for (Map.Entry<DTNHost, List<Double>> entry : conTime.entrySet()) {
			// 		tes.append(entry.getKey() + " | ");
			// 	}
			// 	tes.append("\n");
			// }
			// tes.append(d + ", " + router.getNodeConn() + "\n");
			// tes.append(d + ", " + router.getMsgReceived() + "\n");

			tes.append(d + ", " + d.dummyForReward + "\n");
			// if (d.toString().equals("p0")) {
			// 	// Map<DTNHost, List<Duration>> dur = d.listDurPerNode;
			// 	Map<DTNHost, List<Duration>> dur = router.getTesDur();
			// 	for (Map.Entry<DTNHost, List<Duration>> entry : dur.entrySet()) {
			// 		tes.append(entry.getKey() + " ");
			// 		for (Duration dura : entry.getValue()) {
			// 			tes.append("[" +dura.start + ", " + dura.end + "], ");
			// 		}
			// 		tes.append("\n");
			// 	}
			// }
		}

		/** total receive per node */
		// for (Map.Entry<DTNHost, Integer> entry : totalReceivePerNode.entrySet()) {
		// 	// tes += entry.getKey() + ", " + entry.getValue() + "\n";
		// 	tes.append(entry.getKey() + ", " + entry.getValue() + "\n");
		// }
		
		/** total transfer per node */
		// for (Map.Entry<DTNHost, Integer> entry : totalTransferPerNode.entrySet()) {
		// 	tes += entry.getKey() + ", " + entry.getValue() + "\n";
		// }

		write(tes.toString());
		// write(statsText);
		super.done();
	}

}
