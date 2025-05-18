
/*
 * Average Degree of all previous window
 * (now, considering COMMUNITY)
 */


package routing.community;

import java.util.*;

import core.*;


public class AverageWinCentrality1 implements Centrality {

	
	public static final String CENTRALITY_WINDOW_SETTING = "timeWindow";
	public static final String COMPUTATION_INTERVAL_SETTING = "computeInterval";
	public static final String EPOCH_COUNT_SETTING = "nrOfEpochsToAvg";
	
	
	protected static int COMPUTE_INTERVAL = 600; // 600=10 minutes 87000=24h10m
	protected static int CENTRALITY_TIME_WINDOW = 86400; //6 hours=21600 24h=86400
	protected static int EPOCH_COUNT = 787; //Reality duration 787 Cam 46
	
	protected double globalCentrality;
	protected double localCentrality;
	
	protected int lastGlobalComputationTime;
	protected int lastLocalComputationTime;
	
	//temporary inserted !!!
	protected int [] globalCentralities = new int[EPOCH_COUNT];
	
	
	
	public AverageWinCentrality1 (Settings s)
	{
		if (s.contains(CENTRALITY_WINDOW_SETTING))
			CENTRALITY_TIME_WINDOW = s.getInt(CENTRALITY_WINDOW_SETTING);
		if (s.contains(COMPUTATION_INTERVAL_SETTING))
			COMPUTE_INTERVAL = s.getInt(COMPUTATION_INTERVAL_SETTING);
		if (s.contains(EPOCH_COUNT_SETTING))
			EPOCH_COUNT = s.getInt(EPOCH_COUNT_SETTING);
	}
	
	public AverageWinCentrality1 (AverageWinCentrality1 proto)
	{
		this.lastGlobalComputationTime = this.lastLocalComputationTime = -COMPUTE_INTERVAL;
	}
	
	public double getGlobalCentrality (Map<DTNHost, List<Duration>> connHistory)
	{
		if (SimClock.getIntTime() - this.lastGlobalComputationTime < COMPUTE_INTERVAL)
			return globalCentrality;
		
		
		//start-initialisation
		int epochCount = (int)Math.round(SimClock.getIntTime() / CENTRALITY_TIME_WINDOW + 0.5);
		int [] centralities = new int[epochCount];
		
		int epoch;
		int timeNow=SimClock.getIntTime();
		Map<Integer, Set<DTNHost>> nodesCountedInEpoch = new HashMap<Integer, Set<DTNHost>>();
		
		for (int i=0; i<epochCount; i++) 
			nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
		//end-initialisation
		
		
		for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet())
		{
			DTNHost h= entry.getKey();
			for (Duration d: entry.getValue())
			{
				int timePassed = (int) (timeNow - d.end);
				if (timePassed > CENTRALITY_TIME_WINDOW * epochCount) 
					break;
				
				epoch= timePassed / CENTRALITY_TIME_WINDOW; // EPOCH NUMBER or LOCATION
				
				Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch); //Only consider each node counted 1 per epoch
				if(nodesAlreadyCounted.contains(h))
					continue;
				centralities[epoch]++;
				nodesAlreadyCounted.add(h);
								
			}
					
		}
		
		// compute and return average node degree
		int sum = 0;
				
		for (int i=0; i < epochCount; i++)
			sum += centralities[i];
		this.globalCentrality =((double)sum) / epochCount;
		
		this.lastGlobalComputationTime = SimClock.getIntTime();
		return this.globalCentrality;
		
	}
	
	public double getLocalCentrality(Map<DTNHost, List<Duration>> connHistory, CommunityDetection cd)
	{
		
		if(SimClock.getIntTime() - this.lastLocalComputationTime < COMPUTE_INTERVAL)
			return localCentrality;
		
		
		//start-initialisation
		// centralities will hold the count of unique encounters in each epoch
				
		int epochCount = (int)Math.round(SimClock.getIntTime() / CENTRALITY_TIME_WINDOW + 0.5);
		int [] centralities = new int[epochCount];
				
		int epoch; 
		int timeNow = SimClock.getIntTime();
		Map<Integer, Set<DTNHost>> nodesCountedInEpoch = new HashMap<Integer, Set<DTNHost>>();
								
		for(int i = 0; i < epochCount; i++)
					nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
		
		//end-initialisation
		
		
		// local centrality only considers nodes in the local community
		Set<DTNHost> community = cd.getLocalCommunity();
		
		/*
		 * For each node, loop through connection history until we crossed all
		 * the epochs we need to cover
		 */
		
		for(Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet())
		{
			DTNHost h = entry.getKey();
			
			// if the host isn't in the local community, we don't consider it
			if(!community.contains(h))
				continue;
			
			for(Duration d : entry.getValue())
			{
				int timePassed = (int)(timeNow - d.end);
				
				// if we reached the end of the last epoch, we're done with this node
				if(timePassed > CENTRALITY_TIME_WINDOW * epochCount)
					break;
				
				// compute the epoch this contact belongs to
				epoch = timePassed / CENTRALITY_TIME_WINDOW; //EPOCH NUMBER or LOCATION
				
				
				// Only consider each node once per epoch
				Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch);
				if(nodesAlreadyCounted.contains(h))
					continue;
				
				// increment the degree for the given epoch
				centralities[epoch]++;
				nodesAlreadyCounted.add(h);
			}
		}
		
		// compute and return average LOCAL node degree
		int sum = 0;
				
		for (int i=0; i < epochCount; i++)
					sum += centralities[i];
		this.localCentrality =((double)sum) / epochCount;
				
		this.lastLocalComputationTime = SimClock.getIntTime();
		return this.localCentrality;
		
				
	}
	
	
	
	
	
	
	//temporary inserted new method by me
	public int [] getGlobalArrayCentrality(Map<DTNHost, List<Duration>> connHistory)
	{
		//if (SimClock.getIntTime() - this.lastGlobalComputationTime < COMPUTE_INTERVAL)
		//	return globalCentralities;
		
		
		//initialise
		
		int [] centralities = new int[EPOCH_COUNT];
		
		int epoch;
		int timeNow=SimClock.getIntTime();
		Map<Integer, Set<DTNHost>> nodesCountedInEpoch = new HashMap<Integer, Set<DTNHost>>();
		
		for (int i=0; i<EPOCH_COUNT; i++)
			nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
		//end-initialisation
		
		
		for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet())
		{
			DTNHost h= entry.getKey();
			for (Duration d: entry.getValue())
			{
				int timePassed = (int) (timeNow - d.end);
				if (timePassed > CENTRALITY_TIME_WINDOW * EPOCH_COUNT)
					break;
				
				epoch= timePassed / CENTRALITY_TIME_WINDOW; // EPOCH NUMBER/LOCATION
				
				Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch); //Only consider each node counted 1 per epoch
				if(nodesAlreadyCounted.contains(h))
					continue;
				centralities[epoch]++;
				nodesAlreadyCounted.add(h);
								
			}
					
		}
		
		
		
		//this.lastGlobalComputationTime = SimClock.getIntTime();
		return this.globalCentralities = centralities;
			
	}
	
	
	
	public Centrality replicate()
	{
		return new AverageWinCentrality1(this);
	}


	
	
	
}
