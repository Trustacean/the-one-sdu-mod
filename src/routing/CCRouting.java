package routing;

import java.util.*;
import core.*;
import routing.community.Duration;
// import reinforcement.qlearn.QLearner;

public class CCRouting extends ActiveRouter {
	// private QLearner tessss;
	private Map<DTNHost, Map<List<Duration>, Integer>> amountDataPerDuration;

	protected Map<DTNHost, Double> startTimestamps;
	protected Map<DTNHost, List<Duration>> connHistory;
	// private Map<Duration, Double> congestionRate;
	// private static Set<DTNHost> tesSet;
	// private double congestionRatio;
	// private List<Duration> listD;
	private int msgReceived = 0;
	private int msgTransferred = 0;
	private int dataReceived = 0;
	private int dataTransferred = 0;
	private double lastUpdateTime  = 0;
	private double updateInterval = 1200;
	public double totalContactTime = 0;
	// private List<Duration> interval;
	// private Map<Double, Integer> cr;
	int numberTes = 1;

	private Map<DTNHost, Map<Double, Integer>> dataPerInterval;
	private Map<DTNHost, Double> connWithOther;
	private double startTime;
	private Set<DTNHost> setTes;

	private Map<DTNHost, List<Duration>> tesDurPerNode;
	private List<Double> cr;
	private List<Double> dataInContact;

	public static final double SMOOTHING_FACTOR = 0.5;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public CCRouting(Settings s) {
		super(s);
		// tesSet = new HashSet<DTNHost>();
		startTimestamps = new HashMap<DTNHost, Double>();
		connHistory = new HashMap<DTNHost, List<Duration>>();
		connWithOther = new HashMap<DTNHost, Double>();
		setTes = new HashSet<DTNHost>();
		tesDurPerNode = new HashMap<>();
		cr = new ArrayList<Double>();
		dataInContact = new ArrayList<Double>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param r
	 */
	protected CCRouting(CCRouting r) {
		super(r);
		amountDataPerDuration = new HashMap<DTNHost, Map<List<Duration>, Integer>>();
		startTimestamps = r.startTimestamps;
		connHistory = r.connHistory;
		connWithOther = r.connWithOther;
		setTes = r.setTes;
		tesDurPerNode = r.tesDurPerNode;
		cr = r.cr;
		dataInContact = r.dataInContact;
	}

	@Override
	public void changedConnection(Connection con) {
		// DTNHost peer = con.getOtherNode(getHost());
		if (con.isUp()) {
			
		} else {
			this.totalContactTime += SimClock.getTime();
		}
	}

	// @Override
	public Message messageTransferred(String id, DTNHost from) {
		Message m = super.messageTransferred(id, from);

		/**
		 * N.B. With application support the following if-block
		 * becomes obsolete, and the response size should be configured
		 * to zero.
		 */
		// check if msg was for this host and a response was requested
		if (m.getTo() == getHost() && m.getResponseSize() > 0) {
			// generate a response message
			Message res = new Message(this.getHost(), m.getFrom(),
					RESPONSE_PREFIX + m.getId(), m.getResponseSize());
			this.createNewMessage(res);
			this.getMessage(RESPONSE_PREFIX + m.getId()).setRequest(m);
		}

		getHost().msgReceived++; // increment jumlah message diterima
		this.msgReceived++;
		this.dataReceived += m.getSize();

		return m;
	}

	@Override
	protected void transferDone(Connection con) {
		getHost().msgTransferred++;
		this.msgTransferred++;
		this.dataTransferred += con.getMessage().getSize();
	}

	@Override
	public void update() {
		super.update();
		
		countCongestionRatio(); // hitung CR
		
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}

		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}

	@Override
	public CCRouting replicate() {
		return new CCRouting(this);
	}

	public int getTotalDataRcv() {
		return this.dataReceived;
	}
	public int getTotalDataTrf() {
		return this.dataTransferred;
	}
	public int getMsgReceived() {
		return this.msgReceived;
	}

	public int getMsgTransferred() {
		return this.msgTransferred;
	}

	public void countCongestionRatio() {
		if ((SimClock.getTime() - lastUpdateTime) > updateInterval) {
			double dataContact = 0;
			dataContact  = (this.dataReceived + this.dataTransferred) / totalContactTime;

			// this.dataInContact.add(dataContact);
			// this.cr.add(this.avgCr());

			getHost().dataInContact.add(dataContact);
			getHost().congestionRatio.add(this.avgList(getHost().dataInContact));
			int lastCr = getHost().congestionRatio.size()-1;
			double oLast = getHost().congestionRatio.get(lastCr);
			this.countEma(oLast);
			
			lastUpdateTime = SimClock.getTime();

			this.dataReceived = 0;
			this.dataTransferred = 0;

			testingCountEma();
		}
	}

	public double avgList(List<Double> lists) {
		if(lists.size() == 0) {
			return 0;
		}

		double value = 0;

		for (double i : lists) {
			value += i;
		}

		return value / lists.size();
	}

	public void countEma(double oLast) {
		int emaPrev = getHost().ema.size()-1; // last index dari ema
		
		double value = (getHost().ema.size() > 0)
			? oLast * SMOOTHING_FACTOR + getHost().ema.get(emaPrev) * (1 - SMOOTHING_FACTOR)
			: oLast * SMOOTHING_FACTOR + 0 * (1 - SMOOTHING_FACTOR);

		getHost().ema.add(value);
	}

	public void testingDummyReward(double r) {
		getHost().dummyForReward.add(r);
	}

	public void testingCountEma() {
		double value = 0;
		for(double i : getHost().ema) {
			value += i;
		}

		testingDummyReward(1/value);
	}

	public List<Double> getDataInContact() {
		return this.dataInContact;
	}

	public List<Double> getCr() {
		return this.cr;
	}
}
