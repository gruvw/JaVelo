package ch.epfl.javelo.routing;

import java.io.IOException;
import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;

public class Stage6ManualTest {

    public static void main(String[] args) throws IOException {
        // CH_WEST
        long totalTime = 0;
        int iterations = 40;
        Graph gWest = Graph.loadFrom(Path.of("data/ch_west"));
        CostFunction cfWest = new CityBikeCF(gWest);
        RouteComputer rcWest = new RouteComputer(gWest, cfWest);
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            Route r = rcWest.bestRouteBetween(2046055, 2694240);
            totalTime += (System.nanoTime() - t0) / 1_000_000;
            KmlPrinter.write("ch_west-out.kml", r);
        }
        System.out.println("Average of " + totalTime / (double) iterations + "ms");

        // LAUSANNE
        Graph gLausanne = Graph.loadFrom(Path.of("data/lausanne"));
        CostFunction cfLausanne = new CityBikeCF(gLausanne);
        RouteComputer rcLausanne = new RouteComputer(gLausanne, cfLausanne);
        Route r = rcLausanne.bestRouteBetween(159049, 117669); // lausanne exemple donnée
        KmlPrinter.write("lausanne-out.kml", r);
    }

}
