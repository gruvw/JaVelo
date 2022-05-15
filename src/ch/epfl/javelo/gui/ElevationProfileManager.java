package ch.epfl.javelo.gui;

import java.util.Arrays;
import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 * Handles interactions and display of the profile.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class ElevationProfileManager {

    /**
     * Value used as the position on the profile when there is none.
     */
    protected static final double DISABLED_VALUE = Double.NaN;

    /**
     * Possible distances between two vertical lines, representing the position.
     */
    private static final int[] POSITION_STEPS = {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};

    /**
     * Minimum number of pixels between two vertical lines of the grid.
     */
    private static final int MIN_VERTICAL_SPACING = 25;

    /**
     * Possible distances between two horizontal lines, representing the elevation.
     */
    private static final int[] ELEVATION_STEPS = {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};

    /**
     * Minimum number of pixels between two horizontal lines of the grid.
     */
    private static final int MIN_HORIZONTAL_SPACING = 50;

    /**
     * Spacing (padding) between the center pane and its parent.
     */
    private static final Insets PROFILE_PADDING = new Insets(10, 10, 20, 40);

    /**
     * Font used when drawing the labels next to the profile.
     */
    private static final Font LABEL_FONT = Font.font("Avenir", 10);

    /**
     * Text used to display statistics about the profile.
     */
    private static final String STATS_TEXT = "Longueur : %.1f km" + "     Montée : %.0f m"
            + "     Descente : %.0f m" + "     Altitude : de %.0f m à %.0f m";

    private final ReadOnlyObjectProperty<ElevationProfile> profileProperty;
    private final ReadOnlyDoubleProperty highlightedPositionProperty;

    private final DoubleProperty mousePositionOnProfileProperty;

    private final ObjectProperty<Rectangle2D> surroundingRectangleProperty;
    private final ObjectProperty<Transform> screenToWorldProperty;
    private final ObjectProperty<Transform> worldToScreenProperty;

    private final BorderPane pane;
    private final Pane centerPane;
    private final VBox bottomBox;
    private final Path gridPath;
    private final Group textLabels;
    private final Polygon graphPolygon;
    private final Line highlightedLine;
    private final Text statsText;

    /**
     * Constructor of an elevation profile manager.
     *
     * @param profileProperty             property containing the profile to display
     * @param highlightedPositionProperty property containing the position on the route to highlight
     *                                    on the graph
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profileProperty,
                                   ReadOnlyDoubleProperty highlightedPositionProperty) {
        this.profileProperty = profileProperty;
        this.highlightedPositionProperty = highlightedPositionProperty;

        // Center pane elements
        this.gridPath = new Path();
        this.gridPath.setId("grid");

        this.textLabels = new Group();

        this.graphPolygon = new Polygon();
        this.graphPolygon.setId("profile");

        this.highlightedLine = new Line();

        // Bottom box element
        this.statsText = new Text();

        this.centerPane = new Pane(gridPath, textLabels, graphPolygon, highlightedLine);
        this.bottomBox = new VBox(statsText);
        this.bottomBox.setId("profile_data");

        this.pane = new BorderPane(centerPane);
        this.pane.setBottom(bottomBox);
        this.pane.getStylesheets().add("elevation_profile.css");

        this.surroundingRectangleProperty = new SimpleObjectProperty<>(Rectangle2D.EMPTY);
        this.screenToWorldProperty = new SimpleObjectProperty<>(new Affine());
        this.worldToScreenProperty = new SimpleObjectProperty<>();
        this.mousePositionOnProfileProperty = new SimpleDoubleProperty(DISABLED_VALUE);

        registerBindings();
        registerHandlers();
    }

    /**
     * Returns the pane rendering the profile layout.
     *
     * @return the pane rendering the profile layout
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Returns the position of the cursor along the profile.
     *
     * @return the position of the cursor along the profile, in meters, or {@code Double.NaN} if no
     *         position needs to be highlighted
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfileProperty;
    }

    /**
     * Registers bindings.
     */
    private void registerBindings() {
        surroundingRectangleProperty.bind(Bindings.createObjectBinding(
                () -> createRectangle(centerPane.getWidth(), centerPane.getHeight()),
                centerPane.widthProperty(), centerPane.heightProperty()));

        screenToWorldProperty.bind(Bindings.createObjectBinding(
                () -> createScreenToWorldTransform(surroundingRectangleProperty.get()),
                surroundingRectangleProperty, profileProperty));
        worldToScreenProperty.bind(Bindings.createObjectBinding(
                () -> screenToWorldProperty.get().createInverse(), screenToWorldProperty));

        highlightedLine.layoutXProperty()
                       .bind(Bindings.createDoubleBinding(
                               () -> worldToScreenProperty.get()
                                                          .transform(
                                                                  highlightedPositionProperty.get(),
                                                                  0)
                                                          .getX(),
                               highlightedPositionProperty, worldToScreenProperty));
        highlightedLine.startYProperty()
                       .bind(Bindings.select(surroundingRectangleProperty, "minY"));
        highlightedLine.endYProperty().bind(Bindings.select(surroundingRectangleProperty, "maxY"));
        highlightedLine.visibleProperty().bind(highlightedPositionProperty.greaterThanOrEqualTo(0));

        statsText.textProperty().bind(Bindings.createStringBinding(() -> {
            ElevationProfile profile = profileProperty.get();
            if (profile == null)
                return "";
            return STATS_TEXT.formatted(profile.length() / 1000, profile.totalAscent(),
                    profile.totalDescent(), profile.minElevation(), profile.maxElevation());
        }, profileProperty));
    }

    /**
     * Registers event handlers and listeners.
     */
    private void registerHandlers() {
        profileProperty.addListener(e -> draw());
        surroundingRectangleProperty.addListener(e -> draw());

        centerPane.setOnMouseMoved(e -> mousePositionOnProfileProperty.set(
                surroundingRectangleProperty.get().contains(e.getX(), e.getY())
                        ? screenToWorldProperty.get().transform(Math.round(e.getX()), 0).getX()
                        : DISABLED_VALUE));
        centerPane.setOnMouseExited(e -> mousePositionOnProfileProperty.set(DISABLED_VALUE));
    }

    /**
     * Draws the grid, the labels and the polygon representing the profile. (everything in the
     * center pane)
     */
    private void draw() {
        gridPath.getElements().clear();
        textLabels.getChildren().clear();
        graphPolygon.getPoints().clear();

        ElevationProfile profile = profileProperty.get();
        if (profile == null)
            return;

        Transform worldToScreen = worldToScreenProperty.get();

        // Horizontal lines (Elevations)
        int elevationStep = Arrays.stream(ELEVATION_STEPS)
                                  .filter(e -> -worldToScreen.deltaTransform(0, e)
                                                             .getY() >= MIN_HORIZONTAL_SPACING)
                                  .findFirst()
                                  .orElse(ELEVATION_STEPS[ELEVATION_STEPS.length - 1]);
        int startElevation = (int) (Math.ceil(profile.minElevation() / elevationStep)
                * elevationStep);
        for (int elevation = startElevation;
             elevation <= profile.maxElevation();
             elevation += elevationStep)
            drawElevation(elevation);

        // Vertical lines (Positions)
        int positionStep = Arrays.stream(POSITION_STEPS)
                                 .filter(e -> worldToScreen.deltaTransform(e, 0)
                                                           .getX() >= MIN_VERTICAL_SPACING)
                                 .findFirst()
                                 .orElse(POSITION_STEPS[POSITION_STEPS.length - 1]);
        for (int position = 0; position <= profile.length(); position += positionStep)
            drawPosition(position);

        drawPolygon();
    }

    /**
     * Draws a horizontal line and its label at a given elevation.
     *
     * @param elevation elevation, in meters, in the real world
     */
    private void drawElevation(int elevation) {
        ElevationProfile profile = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();

        // Line
        Point2D start = worldToScreen.transform(0, elevation);
        Point2D end = worldToScreen.transform(profile.length(), elevation);
        gridPath.getElements()
                .addAll(new MoveTo(start.getX(), start.getY()), new LineTo(end.getX(), end.getY()));

        // Label
        Text text = newLabel(String.valueOf(elevation), "vertical");
        text.textOriginProperty().set(VPos.CENTER);
        text.setLayoutX(start.getX() - (text.prefWidth(0) + 2));
        text.setLayoutY(start.getY());
    }

    /**
     * Draws a vertical line and its label at a given position.
     *
     * @param position position on the route, in meters, in the real world
     */
    private void drawPosition(int position) {
        ElevationProfile profile = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();

        // Line
        Point2D start = worldToScreen.transform(position, profile.minElevation());
        Point2D end = worldToScreen.transform(position, profile.maxElevation());
        gridPath.getElements()
                .addAll(new MoveTo(start.getX(), start.getY()), new LineTo(end.getX(), end.getY()));

        // Label
        Text text = newLabel(String.valueOf(position / 1000), "horizontal");
        text.textOriginProperty().set(VPos.TOP);
        text.setLayoutX(start.getX() - (text.prefWidth(0) / 2));
        text.setLayoutY(start.getY());
    }

    /**
     * Draws the polygon representing the profile.
     */
    private void drawPolygon() {
        ElevationProfile profile = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();

        Point2D bottomLeft = worldToScreen.transform(profile.length(), profile.minElevation());
        Point2D bottomRight = worldToScreen.transform(0, profile.minElevation());
        graphPolygon.getPoints()
                    .addAll(bottomLeft.getX(), bottomLeft.getY(), bottomRight.getX(),
                            bottomRight.getY());

        int min = (int) surroundingRectangleProperty.get().getMinX();
        int max = (int) surroundingRectangleProperty.get().getMaxX();
        for (int p = min; p <= max; p++) {
            double position = screenToWorldProperty.get().transform(p, 0).getX();
            Point2D converted = worldToScreen.transform(position, profile.elevationAt(position));
            graphPolygon.getPoints().addAll(converted.getX(), converted.getY());
        }
    }

    /**
     * Computes the transformation mapping screen coordinates to real world coordinates.
     * <p>
     * [Pixels X, Pixels Y] -> [Position (meters), Elevation (meters)]
     *
     * @param rectangle surrounding rectangle containing the initial points
     * @return the transformation mapping screen coordinates to real world coordinates
     */
    private Transform createScreenToWorldTransform(Rectangle2D rectangle) {
        ElevationProfile profile = profileProperty.get();
        Affine screenToWorldTransform = new Affine();
        if (profile != null) {
            screenToWorldTransform.prependTranslation(-PROFILE_PADDING.getLeft(),
                    -PROFILE_PADDING.getTop());
            screenToWorldTransform.prependScale(profile.length() / rectangle.getWidth(),
                    (profile.minElevation() - profile.maxElevation()) / rectangle.getHeight());
            screenToWorldTransform.prependTranslation(0, profile.maxElevation());
        }
        return screenToWorldTransform;
    }

    /**
     * Creates the surrounding rectangle around the graph.
     *
     * @param paneWidth  width of the pane containing the surrounding rectangle
     * @param paneHeight height of the pane containing the surrounding rectangle
     * @return the newly created rectangle
     */
    private Rectangle2D createRectangle(double paneWidth, double paneHeight) {
        double width = Math.max(
                paneWidth - (PROFILE_PADDING.getLeft() + PROFILE_PADDING.getRight()), 0);
        double height = Math.max(
                paneHeight - (PROFILE_PADDING.getBottom() + PROFILE_PADDING.getTop()), 0);
        return new Rectangle2D(PROFILE_PADDING.getLeft(), PROFILE_PADDING.getTop(), width, height);
    }

    /**
     * Creates a new text label with a given text and class.
     *
     * @param text      text inside the label
     * @param className style class assigned to the label
     * @return the newly created label
     */
    private Text newLabel(String text, String className) {
        Text label = new Text();
        label.setFont(LABEL_FONT);
        label.getStyleClass().addAll("grid_label", className);
        label.setText(text);
        textLabels.getChildren().add(label);
        return label;
    }

}
