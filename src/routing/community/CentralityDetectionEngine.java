
/*
 * Declares a RoutingDecisionEngine object to also perform centrality
 * detection in some fashion. This is needed for Centrality Detection Reports
 * to print out the global and local centrality of each node 
 * 
 */



package routing.community;





public interface CentralityDetectionEngine {

	//returns the global centrality of a node
	public double getGlobalDegreeCentrality();
	
	//returns the local centrality of a node
	public double getLocalDegreeCentrality ();	
	
	//temporary inserted
	//return the array of the global centrality of a node
	//public int [] getArrayCentrality ();
	
}
