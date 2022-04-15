# Conception

## Order In Code

Statics are before non-statics.

1. Constants
   1. Public constants
   2. Private constants
2. Attributes
    1. Public attributes
    2. Private attributes
3. Constructors
4. Public Inner Classes
5. Methods
   1. Of methods (constructor like)
   2. Public methods
   3. Public overrides
   4. Private methods

## Styling

The styling rules are defined in the [java-style-rules.xml](java-style-rules.xml) file.

## Public Interface Modifications

### `PointWebMercator`

Created a new static method `of(double lat, double lon)` designed to be human friendly and mainly used in testing.
It takes a latitude and longitude in degrees and returns the corresponding `PointWebMercator` object.

### `TileManager.TileId`

Created a new static method `of(PointWebMercator point, int zoomLevel)` designed to retrieve the `TileId` containing a point in the Web Mercator projection.
