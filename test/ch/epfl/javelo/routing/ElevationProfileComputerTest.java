package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.test.TestUtils;

public class ElevationProfileComputerTest {

    private static final PointCh point = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
    private static final DoubleUnaryOperator nanProfile = Functions.constant(Double.NaN);
    private static final Edge nanEdge = edgeOf(5, nanProfile);

    private static final Edge edgeOf(double length, DoubleUnaryOperator profile) {
        return new Edge(0, 1, point, point, length, profile);
    }

    @Test
    void ElevationProfileThrowsForInvalidMaxStepLength() {
        List<Edge> edges = Arrays.asList(new Edge[] {nanEdge});
        Route r = new SingleRoute(edges);
        assertThrows(IllegalArgumentException.class,
                () -> ElevationProfileComputer.elevationProfile(r, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ElevationProfileComputer.elevationProfile(r, -1));
    }

    static Stream<Arguments> elevationProfileTest() {
        return Stream.of(
                // Single edge
                Arguments.of(new SingleRoute(Arrays.asList(edgeOf(5, Functions.constant(3)))), 2,
                        new ElevationProfile(5, new float[] {3, 3, 3, 3})),
                // No holes, with profile
                Arguments.of(new SingleRoute(Arrays.asList(edgeOf(1, Functions.constant(4)),
                        edgeOf(3, Functions.constant(5)), edgeOf(1, Functions.constant(1)))), 0.5,
                        new ElevationProfile(5, new float[] {4, 4, 5, 5, 5, 5, 5, 5, 1, 1, 1})),
                // Start holes
                Arguments.of(new SingleRoute(Arrays.asList(edgeOf(1, nanProfile),
                        edgeOf(3, Functions.constant(5)), edgeOf(1, Functions.constant(1)))), 0.5,
                        new ElevationProfile(5, new float[] {5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1})),
                // End holes
                Arguments.of(
                        new SingleRoute(Arrays.asList(edgeOf(1, Functions.constant(4)),
                                edgeOf(3, Functions.constant(5)), edgeOf(1, nanProfile))),
                        0.5,
                        new ElevationProfile(5, new float[] {4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5})),
                // Start + end holes
                Arguments.of(
                        new SingleRoute(Arrays.asList(edgeOf(1, nanProfile),
                                edgeOf(3, Functions.constant(5)), edgeOf(1, nanProfile))),
                        0.5,
                        new ElevationProfile(5, new float[] {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5})),
                // Holes in the middle
                Arguments.of(
                        new SingleRoute(Arrays.asList(edgeOf(1, Functions.constant(4)),
                                edgeOf(3, nanProfile), edgeOf(1, Functions.constant(1)))),
                        0.9,
                        // (right on a point can't know which edge)
                        new ElevationProfile(5, new float[] {4, 4, 3.25F, 2.5F, 1.75F, 1, 1})),
                // Holes everywhere, non-constant profile
                Arguments.of(
                        new SingleRoute(Arrays.asList(edgeOf(0.5, nanProfile),
                                edgeOf(1.5, Functions.sampled(new float[] {2, 5}, 1.5)),
                                edgeOf(0.4, Functions.sampled(new float[] {5, 4}, 0.4)),
                                edgeOf(1.1, nanProfile), edgeOf(0.7, nanProfile),
                                edgeOf(1.6, Functions.sampled(new float[] {8.2F, 7, 10}, 1.6)),
                                edgeOf(1.5, nanProfile),
                                edgeOf(0.7, Functions.sampled(new float[] {4, 6.1F}, 0.7)),
                                edgeOf(1, nanProfile))),
                        0.8,
                        new ElevationProfile(9,
                                new float[] {2.5F, 2.5F, 4, 4.375F, 5.5F, 6.625F, 7.75F, 7.9375F,
                                             6.825F, 5.7125F, 4.6F, 4.6F, 4.6F})));

    }

    @ParameterizedTest
    @MethodSource
    void elevationProfileTest(Route route, double maxStepLength, ElevationProfile expected) {
        ElevationProfile actual = ElevationProfileComputer.elevationProfile(route, maxStepLength);
        TestUtils.assertEqualsElevationProfile(expected, actual);
    }

}
