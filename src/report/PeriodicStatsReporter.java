package report;

import core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Reports the Delivery Ratio, Overhead Ratio, Average Latency, and Total Forwards following time series.
 * Updated with interval metric of Total Contact.
 */
@SuppressWarnings("DuplicatedCode")
public class PeriodicStatsReporter extends ContactTimesReport implements MessageListener {

	private static final String SEPARATOR = ";";
	public static final String UPDATE_INTERVAL_S = "updateInterval";
	public static final int DEFAULT_UPDATE_INTERVAL = 2000;

	private double lastRecord = Double.MIN_VALUE;
	private int interval;

	private Map<String, Double> creationTimes;
	private List<Double> latencies;
	private List<Integer> hopCounts;
	private List<Double> msgBufferTime;
	private List<Double> rtt; // round trip times

	private int nrofDropped;
	private int nrofRemoved;
	private int nrofStarted;
	private int nrofAborted;
	private int nrofRelayed;
	private int nrofCreated;
	private int nrofResponseReqCreated;
	private int nrofResponseDelivered;
	private int nrofDelivered;

	private int nrofForwards;

	public PeriodicStatsReporter() {
		init();
	}

	@Override
	protected void init() {
		super.init();

		Settings settings = getSettings();
		if (settings.contains(UPDATE_INTERVAL_S)) {
			interval = settings.getInt(UPDATE_INTERVAL_S);
		} else {
			interval = -1; /* not found; use default */
		}

		if (interval < 0) { /* not found or invalid value -> use default */
			interval = DEFAULT_UPDATE_INTERVAL;
		}

		this.creationTimes = new HashMap<String, Double>();
		this.latencies = new ArrayList<Double>();
		this.msgBufferTime = new ArrayList<Double>();
		this.hopCounts = new ArrayList<Integer>();
		this.rtt = new ArrayList<Double>();

		this.nrofDropped = 0;
		this.nrofRemoved = 0;
		this.nrofStarted = 0;
		this.nrofAborted = 0;
		this.nrofRelayed = 0;
		this.nrofCreated = 0;
		this.nrofResponseReqCreated = 0;
		this.nrofResponseDelivered = 0;
		this.nrofDelivered = 0;

		this.nrofForwards = 0;

		writeHeader();
	}

	private void writeHeader() {
		write("Sim Clock" +
			SEPARATOR + "Total Contact" +
			SEPARATOR + "Delivery Ratio" +
			SEPARATOR + "Overhead" +
			SEPARATOR + "Average Latency" +
			SEPARATOR + "Total Forwards");
	}

	@Override
	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}

		this.creationTimes.put(m.getId(), getSimTime());
		this.nrofCreated++;
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
		}
	}

	@Override
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofStarted++;
	}

	@Override
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId())) {
			return;
		}

		if (dropped) {
			this.nrofDropped++;
		} else {
			this.nrofRemoved++;
		}

		this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
	}

	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofAborted++;
	}

	@Override
	public void messageTransferred(Message m, DTNHost from, DTNHost to,
								   boolean finalTarget) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofRelayed++;
		if (finalTarget) {
			this.latencies.add(getSimTime() -
				this.creationTimes.get(m.getId()));
			this.nrofDelivered++;
			this.hopCounts.add(m.getHops().size() - 1);

			if (m.isResponse()) {
				this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
				this.nrofResponseDelivered++;
			}
		}
	}

	/**
	 * Used to trigger the processes with the defined intervals.
	 * Uses hostsConnected instead because of updated's method inconsistencies on large intervals.
	 * */
	@Override
	public void hostsConnected(DTNHost host1, DTNHost host2) {
		super.hostsConnected(host1, host2);

		/* INCREASE TOTAL CONTACTS */
		this.totalContacts += 1;
		if (totalContacts - lastRecord >= interval) {
			lastRecord = totalContacts;
			updateReportPerInterval();
		}
	}

	@Override
	public void done() {

	}

	/**
	 * Custom method that updates (adds) a row of the current interval's cumulative report.
	 */
	private void updateReportPerInterval() {

		double deliveryProb = 0; // delivery probability
		double overHead = Double.NaN;    // overhead ratio

		// calculates the delivery probability
		if (this.nrofCreated > 0) {
			deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
		}

		// calculates the overhead
		if (this.nrofDelivered > 0) {
			overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) /
				this.nrofDelivered;
		}

		// simclock, conncetions/total contacts, delivery prob, overheads, avg latency, total forwards
		String currentReport = String.format("%.2f;%d;%.4f;%.4f;%s;%d",
			SimClock.getTime(), totalContacts, deliveryProb, overHead, getAverage(this.latencies), nrofRelayed);

		write(currentReport);
		System.out.println(currentReport);
	}
}
