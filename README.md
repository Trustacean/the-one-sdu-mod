# The ONE Sanata Dharma University Mod

A modified [The ONE Simulator](https://github.com/knightcode/the-one-pitt) 
repository by students of Sanata Dharma University (Jarkom).

The ONE is an Opportunistic Network Environment simulator that provides a
powerful tool for generating mobility traces, running DTN messaging
simulations with different routing protocols, and visualizing both
simulations interactively in real-time and results after their completion.

**Note:** This modern repository encourages the use of Java SDK 21 (LTS)
to both compile and run the simulator.

# Quick start

## Compiling

You can compile ONE from the source code using the included compile.bat
script. That should work both in Windows and Unix/Linux environment with
Java 6 JDK or later.

If you want to use Eclipse for compiling the ONE, since version 1.1.0 you need
to include some jar libraries in the project's build path. The libraries are
located in the lib folder. To include them in Eclipse, assuming that you have
an Eclipse Java project whose root folder is the folder where you extracted
the ONE, do the following:

1. select from menus: Project -> Properties -> Java Build Path
2. Go to "Libraries" tab
3. Click "Add JARs..."
4. Select "DTNConsoleConnection.jar" under the "lib" folder
5. Add the "ECLA.jar" the same way
6. Press "OK".

Now Eclipse should be able to compile the ONE without warnings.

## Running

ONE can be started using the included one.bat (for Windows) or one.sh (for
Linux/Unix) script. Following examples assume you're using the Linux/Unix
script (just replace "./one.sh" with "one.bat" for Windows).

Synopsis:

```
./one.sh [-b runcount] [conf-files]
```

Options:
-b Run simulation in batch mode. Doesn't start GUI but prints
information about the progress to terminal. The option must be followed
by the number of runs to perform in the batch mode or by a range of runs
to perform, delimited with a colon (e.g, value 2:4 would perform runs 2,
3 and 4). See section "Run indexing" for more information.

Parameters:  
conf-files: The configuration file names where simulation parameters
are read from. Any number of configuration files can be defined and they are
read in the order given in the command line. Values in the later config files
override values in earlier config files.

## Configuring

All simulation parameters are given using configuration files. These files
are normal text files that contain key-value pairs. Syntax for most of the
variables is:
Namespace.key = value

I.e., the key is (usually) prefixed by a namespace, followed by a dot, and
then key name. Key and value are separated by equals-sign. Namespaces
start with a capital letter, both namespace and keys are written in
CamelCase (and are case-sensitive). Namespace defines (loosely) the part
of the simulation environment where the setting has an effect on. Many, but
not all, namespaces are equal to the class name where they are read.
Especially movement models, report modules and routing modules follow this
convention.

Numeric values use '.' as the decimal separator and can be suffixed with
kilo (k) mega (M) or giga (G) suffix. Boolean settings accept "true",
"false", "0", and "1" as values.

Many settings define paths to external data files. The paths can be relative
or absolute but the directory separator must be '/' in both Unix and Windows
environment.

Some variables contain comma-separated values, and for them the syntax is:
Namespace.key = value1, value2, value3, etc.

For run-indexed values the syntax is:

```
Namespace.key = [run1value, run2value, run3value, etc]
```

I.e., all values are given in brackets and values for different run are
separated by semicolon. Each value can also be a comma-separated value.
For more information about run indexing, go to section "Run indexing".

Setting files can contain comments too. A comment line must start with "#"
character. Rest of the line is skipped when the settings are read. This can
be also useful for disabling settings easily.

Some values (scenario and report names at the moment) support "value
filling". With this feature, you can construct e.g., scenario name
dynamically from the setting values. This is especially useful when using
run indexing. Just put setting key names in the value part prefixed and
suffixed by two percent (%) signs. These placeholders are replaces by the
current setting value from the configuration file. See the included
snw_comparison_settings.cfg for an example.

File "default_settings.cfg", if exists, is always read and the other
configuration files given as parameter can define more settings or override
some (or even all) settings in the previous files. The idea is that
you can define in the earlier files all the settings that are common for
all the simulations and run different, specific, simulations using
different configuration files.

## Run indexing

Run indexing is a feature that allows you to run large amounts of
different configurations using only single configuration file. The idea is
that you provide an array of settings (using the syntax described above)
for the variables that should be changed between runs. For example, if you
want to run the simulation using five different random number generator
seeds for movement models, you can define in the settings file the
following:

```
MovementModel.rngSeed = [1, 2, 3, 4, 5]
```

Now, if you run the simulation using command:

```
./one.sh -b 5 my_config.cfg
```

you would run first using seed 1 (run index 0), then another run using
seed 2, etc. Note that you have to run it using batch mode (-b option) if
you want to use different values. Without the batch mode flag the first
parameter (if numeric) is the run index to use when running in GUI mode.

Run indexes wrap around: used value is the value at index (runIndex %
arrayLength). Because of wrapping, you can easily run large amount of
permutations easily. For example, if you define two key-value pairs:

```
key1 = [1, 2] 
key2 = [a, b, c]
```

and run simulation using run-index count 6, you would get all permutations
of the two values (1,a; 2,b; 1,c; 2,a; 1,b; 2,c). This naturally works
with any amount of arrays. Just make sure that the smallest common
nominator of all array sizes is 1 (e.g., use arrays whose sizes are primes)
-- unless you don't want all permutations but some values should be
paired.

## DTN2 Reference Implementation Connectivity

DTN2 connectivity allows bundles to be passed between the ONE and any
number of DTN2 routers. This is done through DTN2's External Convergence
Layer Interface.

When DTN2 connectivity is enabled ONE will connect to dtnd routers as
an external convergence layer adapter. ONE will also automatically configure
dtnd through a console connection with a link and route for bundles to reach
the simulator.

When a bundle is received from dtnd, ONE attempts to match the destination EID
against the regular expressions configured in the configuration file (see DTN2
Connectivity Configuration File below). For each matching node a copy of a
message is created and routed inside ONE. When the bundle reaches its destination
inside ONE it is delivered to the dtnd router instance attached to the node.
Copies of the bundle payload are stored within 'bundles' directory.

To enable this functionality, the following steps must be taken:

1) DTN2 must be compiled and configured with ECL support enabled.
2) DTN2Events event generator must be configured to be loaded into ONE
   as an events class.
3) DTN2Reporter must be configured and loaded into one as a report class.
4) DTN2 connectivity configuration file must be configured as DTN2.configFile

To start the simulation:

1) Start all the dtnd router instances.
2) Start ONE.

## Example Configuration (2-4 above)

Events.nrof = 1
Events1.class = DTN2Events
Report.nrofReports = 1
Report.report1 = DTN2Reporter
DTN2.configFile = cla.conf

### DTN2 Connectivity Configuration File

The DTN2 connectivity configuration file defines which nodes inside ONE
should connect to which DTN2 router instances. It also defines the EID's
that the nodes match. The configuration file is composed of comment lines
starting with # and configuration lines with the following format:

`<nodeID> <EID regexp> <dtnd host> <ECL port> <console port>`

The fields have the following meaning:

| Format           | Description                                                                                                                          |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `<nodeID>`       | The ID of a node inside ONE (integer \>= 0).                                                                                         |
| `EID <regexp>`   | Incoming bundles whose destination EID matches this regexp will be forwarded to the node inside ONE. (see `java.util.regex.Pattern`) |
| ` dtnd <host>`   | Hostname/IP of the dtnd router to connect to this node.                                                                              |
| `ECL <port>`     | dtnd router's port listening to ECLAs.                                                                                               |
| `console <port>` | dtnd router's console port.                                                                                                          |

Example:

```
# <nodeID> <EID regexp> <dtnd host> <ECL port> <console port>
1 dtn://local-1.dtn/(.*) localhost 8801 5051
2 dtn://local-2.dtn/(.*) localhost 8802 5052
```

## GUI

The GUI's main window is divided into three parts. The main part contains
the playfield view (where node movement is displayed) and simulation and
GUI control and information. The right part is used to select nodes and
the lower part is for logging and breakpoints.

The main part's topmost section is for simulation and GUI controls. The
first field shows the current simulation time. Next field shows the
simulation speed (simulated seconds per second). The following four
buttons are used to pause, step, fast forward, and fast forward simulation
to given time. Pressing step-button multiple times runs simulation
step-by-step. Fast forward (FFW) can be used to skip uninteresting parts
of simulation. In FFW, the GUI update speed is set to a large value. Next
drop-down is used to control GUI update speed. Speed 1 means that GUI is
updated on every simulated second. Speed 10 means that GUI is updated only
on every 10th second etc. Negative values slow down the simulation. The
following drop-down controls the zoom factor. The last button saves the
current view as a png-image.

Middle section, i.e., the playfield view, shows the node placement, map
paths, node identifiers, connections among nodes etc. All nodes are
displayed as small rectangles and their radio range is shown as a green
circle around the node. Node's group identifier and network address (a
number) are shown next to each node. If a node is carrying messages, each
message is represented by a green or blue filled rectangle. If node
carries more than 10 messages, another column of rectangles is drawn for
each 10 messages but every other rectangle is now red. You can center the
view to any place by clicking with mouse button on the play field. Zoom
factor can also be changed using mouse wheel on top of the playfield view.

The right part of main window is for choosing a node for closer inspection.
Simply clicking a button shows the node info in main parts lower section.
From there more information can be displayed by selecting one of the
messages the node is carrying (if any) from the drop-down menu. Pressing
the "routing info" button opens a new window where information about the
routing module is displayed. When a node is chosen, the playfield view is
also centered on that node and the current path the node is traveling is
shown in red.

Logging (the lowest part) if divided to two sections, control and log. From
the control part you can select what kind of messages are shown in the
log. You can also define if simulation should be paused on certain type of
event (using the check boxes in the "pause" column). Log part displays time
stamped events. All nodes and message names in the log messages are
buttons and you can get more information about them by clicking the
buttons.

## Toolkit (Legacy)

The simulation package includes a folder called "toolkit" that contains
scripts for generating input and processing the output of the simulator. All
(currently included) scripts are written with Perl (http://www.perl.com/) so
you need to have it installed before running the scripts. Some post processing
scripts use gnuplot (http://www.gnuplot.info/) for creating graphics. Both of
the programs are freely available for most of the Unix/Linux and Windows
environments. For Windows environment, you may need to change the path to the
executables for some of the scripts.

| File                | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `getStats.pl`       | This script can be used to create bar-plots of various statistics gathered by the MessageStatsReport -report module. The only mandatory option is "-stat" which is used to define the name of the statistics value that should be parsed from the report files (e.g., "delivery_prob" for message delivery probabilities). Rest of the parameters should be MessageStatsReport output filenames (or paths). Script creates three output files: one with values from all the files, one with the gnuplot commands used to create the graphics and finally an image file containing the graphics. One bar is created to the plot for each input file. The title for each bar is parsed from the report filename using the regular expression defined with "-label" option. Run getStats.pl with "-help" option for more help.                                                                                                                                                        |
| `ccdfPlotter.pl`    | Script for creating Complementary(/Inverse) Cumulative Distribution Function plots (using gluplot) from reports that contain time-hitcount-tuples. Output filename must be defined with the "-out" option and rest of the parameters should be (suitable) report filenames. "-label" option can be used for defining label extracting regular expression (similar to one for the getStats script) for the legend.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `createCreates.pl`  | Message creation pattern for the simulation can be defined with external events file. Such a file can be simply created with any text editor but this script makes it easier to create a large amount of messages. Mandatory options are the number of messages ("-nrof"), time range ("-time"), host address range ("-hosts") and message size range ("-sizes"). The number of messages is simply an integer but the ranges are given with two integers with a colon (:) between them. If hosts should reply to the messages that they receive, size range of the reply messages can be defined with "-rsizes" option. If a certain random number generator seed should be used, that can be defined with "-seed" option. All random values are drawn from a uniform distribution with inclusive minimum value and exclusive maximum value. Script outputs commands that are suitable for external events file's contents. You probably want to redirect the output to some file. |
| `dtnsim2parser.pl`  | Converts dtnsim2's (http://watwire.uwaterloo.ca/DTN/sim/) output (with verbose mode 8) to an external events file that can be fed to ONE. The main idea of this parser is that you can first create a connectivity pattern file using ONE and ConnectivityDtnsim2Report, feed that to dtnsim2 and then observe the results visually in ONE (using the output converted with dtnsim2parser as the external events file).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `transimsParser.pl` | Converts TRANSIM's (http://transims-opensource.net/) vehicle snapshot files to external movement files that can be used as an input for node movement. See ExternalMovement and ExternalMovementReader classes for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |

`dtnsim2parser.pl` and `transimsParser.pl`:
These two (quite experimental) parsers convert data from other programs to a
form that is suitable for ONE. Both take two parameters: input and output
file. If these parameters are omitted, stdin and stdout are used for input and
output. With "-h" option a short help is printed.

