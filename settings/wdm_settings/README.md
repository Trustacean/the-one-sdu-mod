# WDM Settings

This table includes specific parameters for WDM configurations.

| Key                         | Description                                                                                               |
|-----------------------------|-----------------------------------------------------------------------------------------------------------|
| `meetingSpotsFile`          | WKT File containing the coordinates of meeting spots for evening activity                                 |
| `officeLocationsFile`       | WKT File containing the coordinates of the office locations                                               |
| `homeLocationsFile`         | WKT File containing the coordinates of home locations                                                     |
| `nrOfMeetingSpots`          | Only used if no meetingSpotsFile is specified, in which case the spots will be randomly placed on the map |
| `nrOfOffices`               | Same but for offices                                                                                      |
| `workDayLength`             | Length of the time spent at work                                                                          |
| `probGoShoppingAfterWork`   | Probability to do evening activity                                                                        |
| `officeWaitTimeParetoCoeff` | The coefficient for the Pareto distribution controlling pause time inside office                          |
| `officeMinWaitTime`         | Min pause time inside office                                                                              |
| `officeMaxWaitTime`         | Max pause time inside office                                                                              |
| `officeSize`                | Size of the office in meters                                                                              |
| `timeDiffSTD`               | Standard deviation for the normal distribution controlling differences in schedules nodes have            |
| `minGroupSize`              | Minimum groups size for evening activity                                                                  |
| `maxGroupSize`              | Maximum group size for evening activity                                                                   |
| `minAfterShoppingStopTime`  | Minimum pause time after evening activity                                                                 |
| `maxAfterShoppingStopTime`  | Maximum pause time after evening activity                                                                 |
| `busControlSystemNr`        | The bus control system the node group is registered to (WDM nodes and Bus nodes)                          |
| `ownCarProb`                | Probability that the node owns a car                                                                      |
| `shoppingControlSystemNr`   | The Evening activity control system the node group is registered to                                       |
