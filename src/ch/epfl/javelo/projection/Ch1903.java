package ch.epfl.javelo.projection;

/**
 * Used to convert coordinates between the WSG84 and the Swiss coordinates system.
 * <p>
 * Non-instantiable. Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Ch1903 {

    private Ch1903() {}

    /**
     * Computes λ₁ from the longitude.
     *
     * @param lon initial longitude in radians
     * @return the converted longitude (after degrees conversion)
     */
    private static double convertLon(double lon) {
        return 1e-4 * (3_600 * Math.toDegrees(lon) - 26_782.5);
    }

    /**
     * Computes φ₁ from the latitude.
     *
     * @param lat initial latitude in radians
     * @return the converted latitude (after degrees conversion)
     */
    private static double convertLat(double lat) {
        return 1e-4 * (3_600 * Math.toDegrees(lat) - 169_028.66);
    }

    /**
     * Computes x from the east coordinate.
     *
     * @param e initial east coordinate
     * @return the converted east coordinate
     */
    private static double convertE(double e) {
        return 1e-6 * (e - 2.6e6);
    }

    /**
     * Computes y from the north coordinate.
     *
     * @param n initial north coordinate
     * @return the converted north coordinate
     */
    private static double convertN(double n) {
        return 1e-6 * (n - 1.2e6);
    }

    /**
     * Converts from WGS84 to Swiss coordinates system (E).
     *
     * @param lon point's longitude, in radians
     * @param lat point's latitude, in radians
     * @return the east coordinate, in meters, of the point of given longitude and latitude (WGS84)
     */
    public static double e(double lon, double lat) {
        double lon1 = convertLon(lon), lat1 = convertLat(lat);
        return 2_600_072.37 + 211_455.93 * lon1 - 10_938.51 * lon1 * lat1
                - .36 * lon1 * Math.pow(lat1, 2) - 44.54 * Math.pow(lon1, 3);
    }

    /**
     * Converts from WGS84 to Swiss coordinates system (N).
     *
     * @param lon point's longitude, in radians
     * @param lat point's latitude, in radians
     * @return the north coordinate, in meters, of the point of given longitude and latitude (WGS84)
     */
    public static double n(double lon, double lat) {
        double lon1 = convertLon(lon), lat1 = convertLat(lat);
        double lon1Squared = Math.pow(lon1, 2);
        return 1_200_147.07 + 308_807.95 * lat1 + 3_745.25 * lon1Squared + 76.63 * Math.pow(lat1, 2)
                - 194.56 * lon1Squared * lat1 + 119.79 * Math.pow(lat1, 3);
    }

    /**
     * Converts from the Swiss coordinates system to WGS84 (longitude).
     *
     * @param e point's east coordinates, in meters
     * @param n point's north coordinates, in meters
     * @return the longitude (WGS84), in radians, of the point of given east and north coordinates
     */
    public static double lon(double e, double n) {
        double x = convertE(e), y = convertN(n);
        double lon0 = 2.6779094 + 4.728982 * x + .791484 * x * y + .1306 * x * Math.pow(y, 2)
                - .0436 * Math.pow(x, 3);
        return Math.toRadians(lon0 * 10 / 3.6);
    }

    /**
     * Converts from the Swiss coordinates system to WGS84 (latitude).
     *
     * @param e point's east coordinates, in meters
     * @param n point's north coordinates, in meters
     * @return the latitude (WGS84), in radians, of the point of given east and north coordinates
     */
    public static double lat(double e, double n) {
        double x = convertE(e), y = convertN(n);
        double xSquared = Math.pow(x, 2);
        double lat0 = 16.9023892 + 3.238272 * y - .270978 * xSquared - .002528 * Math.pow(y, 2)
                - .0447 * xSquared * y - .0140 * Math.pow(y, 3);
        return Math.toRadians(lat0 * 10 / 3.6);
    }

}
