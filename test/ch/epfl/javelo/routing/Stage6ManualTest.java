package ch.epfl.javelo.routing;

import java.io.IOException;
import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;

public class Stage6ManualTest {

    public static void main(String[] args) throws IOException {
        Graph g = Graph.loadFrom(Path.of("lausanne"));
        CostFunction cf = new CityBikeCF(g);
        RouteComputer rc = new RouteComputer(g, cf);
        Route r = rc.bestRouteBetween(159049, 117669);
        KmlPrinter.write("javelo.kml", r);
    }

}
