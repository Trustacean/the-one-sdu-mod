package routing;

import java.util.*;
import core.*;

public interface NodeConn {
  public Set<DTNHost> getSetofNode();
  public void addNode(DTNHost host);
}
