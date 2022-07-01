package ch.epfl.javelo.routing;

import java.io.IOException;
import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;

public class Stage7ManualTest {

    public static void main(String[] args) throws IOException {
        Graph gWest = Graph.loadFrom(Path.of("data/ch_west"));
        CostFunction cfWest = new CityBikeCF(gWest);
        RouteComputer rcWest = new RouteComputer(gWest, cfWest);
        Route routeWest = rcWest.bestRouteBetween(2046055, 2694240);
        GpxGenerator.writeGpx("ch_west-out.gpx", routeWest,
                ElevationProfileComputer.elevationProfile(routeWest, 1.2));

        Graph gLausanne = Graph.loadFrom(Path.of(".javelo/lausanne"));
        CostFunction cfLausanne = new CityBikeCF(gLausanne);
        RouteComputer rcLausanne = new RouteComputer(gLausanne, cfLausanne);
        Route routeLausanne = rcLausanne.bestRouteBetween(159049, 117669);
        GpxGenerator.writeGpx("lausanne-out.gpx", routeLausanne,
                ElevationProfileComputer.elevationProfile(routeLausanne, 1.2));
    }

}
