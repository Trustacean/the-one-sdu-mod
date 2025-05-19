# General Settings Information

## Movement models

Movement models govern the way nodes move in the simulation. They provide
coordinates, speeds and pause times for the nodes. The basic installation
contains 5 movement models: random waypoint, map based movement, shortest
path map based movement, map route movement and external movement. All
models, except external movement, have configurable speed and pause time
distributions. A minimum and maximum values can be given and the movement
model draws uniformly distributed random values that are within the given
range. Same applies for pause times. In external movement model the speeds
and pause times are interpreted from the given data.

When a node uses the random waypoint movement model (RandomWaypoint), it is
given a random coordinate in the simulation area. Node moves directly to the
given destination at constant speed, pauses for a while, and then gets a new
destination. This continues throughout the simulations and nodes move along
these zig-zag paths.

Map-based movement models constrain the node movement to predefined paths.  
Different types of paths can be defined and one can define valid paths for
all node groups. This way e.g., cars can be prevented from driving indoors or
on pedestrian paths.

The basic map-based movement model (MapBasedMovement) initially distributes
the nodes between any two adjacent (i.e., connected by a path) map nodes and
then nodes start moving from adjacent map node to another. When node reaches
the next map node, it randomly selects the next adjacent map node but chooses
the map node where it came from only if that is the only option (i.e., avoids
going back to where it came from). Once node has moved trough 10-100 map
nodes, it pauses for a while and then starts moving again.

The more sophisticated version of the map-based movement model
(ShortestPathMapBasedMovement) uses Dijkstra's shortest path algorithm to
find its way trough the map area. Once a node reaches its destination, and
has waited for the pause time, a new random map node is chosen and node moves
there using the shortest path that can be taken using only valid map nodes.

For the shortest path based movement models, map data can also contain Points
Of Interest (POIs). Instead of selecting any random map node for the next
destination, the movement model can be configured to give a POI belonging to
a certain POI group with a configurable probability. There can be unlimited
amount of POI groups and all groups can contain any amount of POIs. All node
groups can have different probabilities for all POI groups. POIs can be used
to model e.g., shops, restaurants and tourist attractions.

Route based movement model (MapRouteMovement) can be used to model nodes that
follow certain routes, e.g. bus or tram lines. Only the stops on the route
have to be defined and then the nodes using that route move from stop to stop
using shortest paths and stop on the stops for the configured time.

All movement models can also decide when the node is active (moves and can be
connected to) and when not. For all models, except for the external movement,
multiple simulation time intervals can be given and the nodes in that group
will be active only during those times.

All map-based models get their input data using files formatted with a subset
of the Well Known Text (WKT) format. LINESTRING and MULTILINESTRING
directives of WKT files are supported by the parser for map path data. For
point data (e.g. for POIs), also the POINT directive is supported. Adjacent
nodes in a (MULTI)LINESTRING are considered to form a path and if some lines
contain some vertex(es) with exactly the same coordinates, the paths are
joined from those places (this is how you create intersections). WKT files
can be edited and generated from real world map data using any suitable
Geographic Information System (GIS) program. The map data included in the
simulator distribution was converted and edited using the free, Java based
OpenJUMP GIS program.

Different map types are defined by storing the paths belonging to different
types to different files. Points Of Interest are simply defined with WKT
POINT directive and POI groups are defined by storing all POIs belonging to a
certain group in the same file. All POIs must also be part of the map data so
they are accessible using the paths. Stops for the routes are defined with
LINESTRING and the stops are traversed in the same order they appear in the
LINESTRING. One WKT file can contain multiple routes and they are given to
nodes in the same order as they appear in the file.

The experimental movement model that uses external movement data
(ExternalMovement) reads timestamped node locations from a file and moves the
nodes in the simulation accordingly. See Javadocs of the ExternalMovementReader
class from the input package for details of the format. A suitable, experimental
converter script (transimsParser.pl) for TRANSIMS data is included in the
toolkit folder.

The movement model to use is defined per node group with the "movementModel"
setting. The value of the setting must be a valid movement model class name from
the movement package. Settings that are common for all movement models are
read in the MovementModel class and movement model specific settings are read
in the respective classes. See the Javadoc documentation and example
configuration files for details.

## Routing modules and message creation

Routing modules define how the messages are handled in the simulation. Six
different active routing modules (First Contact, Epidemic, Spray and Wait,
Direct delivery, PRoPHET and MaxProp) and also a passive router for external
routing simulation are included in the package. The active routing modules are
implementations of the well-known routing algorithms for DTN routing. See the
classes in the routing package for details.

Passive router is made especially for interacting with other (DTN) routing
simulators or running simulations that don't need any routing functionality.
The router doesn't do anything unless commanded by external events. These
external events are provided to the simulator by a class that implements the
EventQueue interface.

The current release includes two classes that can be used as a source of
message events: ExternalEventsQueue and MessageEventGenerator. The former
can read events from a file that can be created by hand, with a suitable
script (e.g., createCreates.pl script in the toolkit folder), or by
converting e.g., dtnsim2's output to a suitable form. See the StandardEventsReader
class from the input package for details of the format. MessageEventGenerator is
a simple message generator class that creates uniformly distributed message
creation patterns with configurable message creation interval, message size
and source/destination host ranges.

The toolkit folder contains an experimental parser script (dtnsim2parser.pl)
for dtnsim2's output (there used to be a more capable Java-based parser but
it was discarded in favor of this more easily extendable script). The script
requires a few patches to dtnsim2's code and those can be found from the
toolkit/dtnsim2patches folder.

The routing module to use is defined per node group with the setting
"router". All routers can't interact properly (e.g., PRoPHET router can only
work with other PRoPHET routers) so usually it makes sense to use the same
(or compatible) router for all groups.

## Reports

Reports can be used to create summary data of simulation runs, detailed data
of connections and messages, files suitable for post-processing using e.g.,
Graphviz (to create graphs) and also to interface with other programs. See
javadocs of report-package classes for details.

There can be any number of reports for any simulation run and the number of
reports to load is defined with "Report.nrofReports" setting. Report class
names are defined with "Report.reportN" setting, where N is an integer value
starting from 1. The values of the settings must be valid report class names
from the report package. The output directory of all reports (which can be
overridden per report class with the "output" setting) must be defined with
Report.reportDir -setting. If no "output" setting is given for a report
class, the resulting report file name is "ReportClassName_ScenarioName.cfg".

All reports have many configurable settings which can be defined using
ReportClassName.settingKey -syntax. See javadocs of Report class and specific
report classes for details (look for "setting id" definitions).

## Host groups

A host group is group of hosts (nodes) that shares movement and routing
module settings. Different groups can have different values for the settings,  
this way they can represent different types of nodes. Base settings can
be defined in the "Group" namespace, different node groups can override
these settings or define new settings in their specific namespaces (Group1,
Group2, etc.).

## The settings

There are plenty of settings to configure; more than is meaningful to
present here. See Javadocs of especially report, routing and movement
model classes for details. See also included settings files for examples.
Perhaps the most important settings are the following.

### Scenario settings:

| Key                          | Description                                                                                                                                                               |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scenario.name                | Name of the scenario. All report files are by default prefixed with this.                                                                                                 |
| Scenario.simulateConnections | Should connections be simulated. If you're only interested in movement modeling, you can disable this to get faster simulation. Usually you want this to be on.           |
| Scenario.updateInterval      | How many seconds are stepped on every update. Increase this to get faster simulation, but then you'll lose some precision. Values from 0.1 to 2 are good for simulations. |
| Scenario.endTime             | How many simulated seconds to simulate.                                                                                                                                   |
| Scenario.nrofHostGroups      | How many hosts groups are present in the simulation.                                                                                                                      |

### Interface settings (used to define the possible interfaces the nodes can have)

| Key           | Description                                                           |
|---------------|-----------------------------------------------------------------------|
| type          | What class (from the interfaces-directory) is used for this interface |
| transmitRange | Range (meters) of the interface.                                      |
| transmitSpeed | Transmit speed of the interface (bytes per second).                   |

### Host group settings (used in Group or GroupN namespace):

| Key            | Description                                                                                                                                                                                                                                           |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| groupID        | Group's identifier (a string or a character). Used as the prefix of host names that are shown in the GUI and reports. Host's full name is groupID+networkAddress.                                                                                     |
| nrofHosts      | Number of hosts in this group.                                                                                                                                                                                                                        |
| nrofInterfaces | Number of interfaces this the nodes of this group use.                                                                                                                                                                                                |
| interfaceX     | The interface that should be used as the interface number X.                                                                                                                                                                                          |
| movementModel  | The movement model all hosts in the group use. Must be a valid class (one that is a subclass of MovementModel class) name from the movement package.                                                                                                  |
| waitTime       | Minimum and maximum (two comma-separated decimal values) of the wait time interval (seconds). Defines how long nodes should stay in the same place after reaching the destination of the current path. Default value is 0,0.                          |
| speed          | Minimum and maximum (two comma-separated decimal values) of the speed interval (m/s). Defines how fast nodes move. A new random value is used on every new path. Default value is 1,1.                                                                |
| bufferSize     | Size of the nodes' message buffer (bytes). When the buffer is full, node can't accept any more messages unless it drops some old messages from the buffer.                                                                                            |
| router         | Router module which is used to route messages. Must be a valid class (subclass of Report class) name from routing package.                                                                                                                            |
| activeTimes    | Time intervals (comma-separated simulated time value tuples: start1, end1, start2, end2, ...) when the nodes in the group should be active. If no intervals are defined, nodes are active all the time.                                               |
| msgTtl         | Time To Live (simulated minutes) of the messages created by this host group. Nodes (with active routing module) check every one minute whether some of their messages' TTLs have expired and drop such messages. Infinite TTL is used if not defined. |

Group and movement model specific settings (only meaningful for certain
movement models):

| Key       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| pois      | Points Of Interest indexes and probabilities (comma-separated index-probability tuples: poiIndex1, poiProb1, poiIndex2, poiProb2, ... ). Indexes are integers and probabilities are decimal values in the range of 0.0-1.0. Setting defines the POI groups where the nodes in this host group can choose destinations from and the probabilities for choosing a certain POI group. For example, a (random) POI from the group defined in the POI file1 (defined with PointsOfInterest.poiFile1 setting) is chosen with the probability poiProb1. If the sum of all probabilities is less than 1.0, a probability of choosing any random map node for the next destination is (1.0 - theSumOfProbabilities). Setting can be used only with ShortestPathMapBasedMovement -based movement models. |
| okMaps    | Which map node types (refers to map file indexes) are OK for the group (comma-separated list of integers). Nodes will not travel through map nodes that are not OK for them. As default, all map nodes are OK. Setting can be used with any MapBasedMovement -based movement model.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| routeFile | If MapRouteMovement movement model is used, this setting defines the route file (path) where the route is read from. Route file should contain LINESTRING WKT directives. Each vertex in a LINESTRING represents one stop on the route.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| routeType | If MapRouteMovement movement model is used, this setting defines the route type. Type can be either circular (value 1) or ping-pong (value 2). See movement.map.MapRoute class for details.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

### Movement model settings:

| Key                           | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MovementModel.rngSeed         | The seed for all movement models' random number generator. If the seed and all the movement model related settings are kept the same, all nodes should move the same way in different simulations (same destinations and speed & wait time values are used).                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| MovementModel.worldSize       | Size of the simulation world in meters (two comma separated values: width, height).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| PointsOfInterest.poiFileN     | For ShortestPathMapBasedMovement -based movement models, this setting defines the WKT files where the POI coordinates are read from. POI coordinates are defined using the POINT WKT directive. The "N" in the end of the setting must be a positive integer (i.e., poiFile1, poiFile2, ...).                                                                                                                                                                                                                                                                                                                                                                                                    |
| MapBasedMovement.nrofMapFiles | How many map file settings to look for in the settings file.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| MapBasedMovement.mapFileN     | Path to the Nth map file ("N" must be a positive integer). There must be at least nrofMapFiles separate files defined in the configuration files(s). All map files must be WKT files with LINESTRING and/or MULTILINESTRING WKT directives. Map files can contain POINT directives too, but those are skipped. This way the same file(s) can be used for both POI and map data. By default the map coordinates are translated so that the upper left corner of the map is at coordinate point (0,0). Y-coordinates are mirrored before translation so that the map's north points up in the playfield view. Also all POI and route files are translated to match to the map data transformation. |

### Report settings:

| Key                | Description                                                                                                                                                                                                                                              |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Report.nrofReports | How many report modules to load. Module names are defined with settings `Report.report1`, `Report.report2`, etc.                                                                                                                                         |
| Report.reportDir   | Where to store the report output files. Can be absolute path or relative to the path where the simulation was started. If the directory doesn't exist, it is created.                                                                                    |
| Report.warmup      | Length of the warm up period (simulated seconds from the start). During the warm up, the report modules should discard the new events. The behavior is report module specific, so check the (java)documentation of different report modules for details. |

### Event generator settings:

| Key           | Description                                                                                                                                                                                                |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Events.nrof   | How many event generators are loaded for the simulation. Event generator specific settings are defined in EventsN namespaces (e.g., Events1.settingName configures a setting for the 1st event generator). |
| EventsN.class | Name of the generator class to load (e.g., ExternalEventsQueue or MessageEventGenerator). The class must be found from the input package.                                                                  |
| filePath      | For the ExternalEventsQueue, you must define the path to the external events file. See input.StandardEventsReader class' javadocs for information about different external events.                         |

### Other settings:

| Key                               | Description                                                                                                                                                                                                                                                                                                              |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Optimization.randomizeUpdateOrder | Should the order in which the nodes' update method is called be randomized. Call to update causes the nodes to check their connections and also update their routing module. If set to false, node update order is the same as their network address order. With randomizing, the order is different on every time step. |
