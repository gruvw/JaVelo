package ch.epfl.javelo.projection;
// TODO global javadoc
public final class Ch1903 {
    private Ch1903() {
    } // non-instantiable, arguments non checked

    /**
     * @param lon longitude in radians
     * @return converted longitude (after degrees conversion)
     */
    private static double convertLon(double lon) {
        return 1e-4 * Math.fma(3_600, Math.toDegrees(lon), 26_782.5);
    }

    /**
     * @param lat latitude in radians
     * @return converted latitude (after degrees conversion)
     */
    private static double convertLat(double lat) {
        return 1e-4 * Math.fma(3_600, Math.toDegrees(lat), 169_028.66);
    }

    /**
     * Converting from WGS84 to Switzerland coordinates system (E).
     *
     * @param lon point's longitude, in radians
     * @param lat point's latitude, in radians
     * @return the east coordinate, in meters, of the point of the given longitude
     *         and latitude (WGS84)
     */
    public static double e(double lon, double lat) {
        double convertedLon = convertLon(lon), convertedLat = convertLat(lat);
        // FIXME should we use fma ?
        return 2_600_072.37 + 211_455.93 * convertedLon - 10_938.51 * convertedLon * convertedLat
                - 0.36 * convertedLon * Math.pow(convertedLat, 2) - 44.54 * Math.pow(convertedLon, 3);
    }

    /**
     * Converting from WGS84 to Switzerland coordinates system (N).
     *
     * @param lon point's longitude, in radians
     * @param lat point's latitude, in radians
     * @return the north coordinate, in meters, of the point of the given longitude
     *         and latitude (WGS84)
     */
    public static double n(double lon, double lat) {

    }

    /**
     * Converting from Switzerland coordinates system to WGS84 (longitude).
     *
     * @param e point's east coordinates, in meters
     * @param n point's north coordinates, in meters
     * @return the longitude (WGS84), in radians, of the point of the given east and
     *         north coordinates
     */
    public static double lon(double e, double n) {

    }

    /**
     * Converting from Switzerland coordinates system to WGS84 (latitude).
     *
     * @param e point's east coordinates, in meters
     * @param n point's north coordinates, in meters
     * @return the latitude (WGS84), in radians, of the point of the given east and
     *         north coordinates
     */
    public static double lat(double e, double n) {

    }
}
