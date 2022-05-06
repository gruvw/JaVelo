package ch.epfl.javelo.gui;

import java.util.concurrent.Callable;
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
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
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
     * Distances between the center pane and its parent.
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
     * @throws NonInvertibleTransformException
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profileProperty,
                                   ReadOnlyDoubleProperty highlightedPositionProperty)
            throws NonInvertibleTransformException {
        this.profileProperty = profileProperty;
        this.highlightedPositionProperty = highlightedPositionProperty;

        this.pane = new BorderPane();
        // TODO: Find a way to automatically add to bin directory at runtime
        this.pane.getStylesheets().add("file:elevation_profile.css");

        this.centerPane = new Pane();
        this.bottomBox = new VBox();
        this.bottomBox.setId("profile_data");

        // Center pane elements
        this.gridPath = new Path();
        this.gridPath.setId("grid");

        this.textLabels = new Group();

        this.graphPolygon = new Polygon();
        this.graphPolygon.setId("profile");

        this.highlightedLine = new Line();

        // Bottom box element
        this.statsText = new Text();

        this.centerPane.getChildren().addAll(gridPath, textLabels, graphPolygon, highlightedLine);
        this.bottomBox.getChildren().add(statsText);

        this.pane.getChildren().addAll(centerPane, bottomBox);

        this.surroundingRectangleProperty = new SimpleObjectProperty<>();
        this.surroundingRectangleProperty.set(
                new Rectangle2D(PROFILE_PADDING.getLeft(), PROFILE_PADDING.getTop(), 0, 0));

        this.screenToWorldProperty = new SimpleObjectProperty<>();
        this.worldToScreenProperty = new SimpleObjectProperty<>();
        ElevationProfile profile = this.profileProperty.get();
        Rectangle2D surroundingRectangle = this.surroundingRectangleProperty.get();
        Affine screenToWorldTransform = new Affine();
        screenToWorldTransform.prependTranslation(-PROFILE_PADDING.getLeft(),
                -PROFILE_PADDING.getTop());
        screenToWorldTransform.prependScale(profile.length() / surroundingRectangle.getWidth(),
                (profile.maxElevation() - profile.minElevation())
                        / surroundingRectangle.getHeight());
        screenToWorldTransform.prependTranslation(0, profile.minElevation());
        this.screenToWorldProperty.set(screenToWorldTransform);
        this.worldToScreenProperty.set(screenToWorldTransform.createInverse());

        this.mousePositionOnProfileProperty = new SimpleDoubleProperty();

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
        // TODO: Where do we round to one meter?
        return mousePositionOnProfileProperty;
    }

    private void registerBindings() {
        highlightedLine.layoutXProperty()
                       .bind(Bindings.createDoubleBinding(
                               worldToScreenProperty.get()
                                                    .transform(highlightedPositionProperty.get(),
                                                            0)::getX,
                               highlightedPositionProperty));
        highlightedLine.startYProperty()
                       .bind(Bindings.select(surroundingRectangleProperty, "minY"));
        highlightedLine.endYProperty().bind(Bindings.select(surroundingRectangleProperty, "maxY"));
        highlightedLine.visibleProperty().bind(highlightedPositionProperty.greaterThanOrEqualTo(0));
    }

    private void registerHandlers() {
        pane.setOnMouseMoved(e -> mousePositionOnProfileProperty.set(
                surroundingRectangleProperty.get().contains(e.getX(), e.getY()) ? e.getX()
                        : Double.NaN));
        pane.setOnMouseExited(e -> mousePositionOnProfileProperty.set(Double.NaN));
    }

    private void drawGrid() {
        ElevationProfile profile = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();

        gridPath.getElements().clear();

        // Horizontal lines (Elevations)
        int elevationStep = ELEVATION_STEPS[ELEVATION_STEPS.length - 1];
        for (int elevationStepCandidate : ELEVATION_STEPS)
            if (worldToScreen.deltaTransform(0, elevationStepCandidate)
                             .getY() >= MIN_HORIZONTAL_SPACING) {
                elevationStep = elevationStepCandidate;
                break;
            }
        int startElevation = (int) (Math.round(profile.minElevation() / elevationStep)
                * elevationStep);
        for (int elevation = startElevation;
             elevation <= profile.maxElevation();
             elevation += elevationStep) {
            // ASK right way to use deltaTransform
            Point2D start = worldToScreen.deltaTransform(0, elevation);
            Point2D end = worldToScreen.deltaTransform(profile.length(), elevation);
            gridPath.getElements()
                    .addAll(new MoveTo(start.getX(), start.getY()),
                            new LineTo(end.getX(), end.getY()));
        }

        // Vertical lines (Positions)
        int positionStep = ELEVATION_STEPS[POSITION_STEPS.length - 1];
        for (int positionStepCandidate : POSITION_STEPS)
            if (worldToScreen.deltaTransform(positionStepCandidate, 0)
                             .getX() >= MIN_VERTICAL_SPACING) {
                positionStep = positionStepCandidate;
                break;
            }
        for (int position = 0; position <= profile.length(); position += positionStep) {
            Point2D start = worldToScreen.deltaTransform(position, 0);
            Point2D end = worldToScreen.deltaTransform(position, profile.maxElevation());
            gridPath.getElements()
                    .addAll(new MoveTo(start.getX(), start.getY()),
                            new LineTo(end.getX(), end.getY()));
        }
    }

    private void drawLabels(int nbPosLabels, int posStep, int nbEleLabels, int eleStep) {
        textLabels.getChildren().clear();
        for (int i = 0; i < nbPosLabels; i++) {
            Text text = new Text();
            // set the font before using the prefWidth method
            text.setFont(LABEL_FONT);
            text.getStyleClass().add("horizontal");
            text.textOriginProperty().set(VPos.TOP);
            text.setLayoutX(text.getLayoutX() - (text.prefWidth(0) / 2));
            // Labels display value in kilometers
            text.setText(String.valueOf(i * posStep / 1000));
            textLabels.getChildren().add(text);
        }
        for (int i = 0; i < nbEleLabels; i++) {
            Text text = new Text();
            // set the font before using the prefWidth method
            text.setFont(LABEL_FONT);
            text.getStyleClass().add("vertical");
            text.textOriginProperty().set(VPos.CENTER);
            text.setLayoutX(text.getLayoutX() - (text.prefWidth(0) / 2));
            textLabels.getChildren().add(text);
        }
        textLabels.getChildren().forEach(child -> ((Text) child).getStyleClass().add("grid_label"));
    }

}
