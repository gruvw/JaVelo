![Screenshot](https://user-images.githubusercontent.com/63407038/176963572-b6dea60f-c34d-45a0-a62c-94a5635ad6e5.png)

# JaVelo

A Switzerland bike route computer. (EPFL BA2 Semester Project)

## Usage

Java entry point: `src/ch/epfl/javelo/gui/JaVelo.java`

### GUI

- Place a new waypoint: single mouse clic
- Remove a waypoint: single mouse clic on waypoint
- Move a waypoint: mouse drag and drop on waypoint
- Place a new waypoint, between two existing waypoints: single mouse clic over the route
- Eport route as [GPX](https://en.wikipedia.org/wiki/GPS_Exchange_Format): using the top left application menu

### Others

The map tiles are downloaded from [OpenStreetMap](https://www.openstreetmap.org/) and cached locally.  
When no route was found between at least two of the waypoints, the entire route will not be displayed.

## Code

The entire app is written in Java 17 using [JavaFx](https://openjfx.io/).  
There are a total of 628 unit tests covering every part of the application except the GUI.  
The data required by the application (OSM cache and Switzerland map) is stored in `.javelo`.
