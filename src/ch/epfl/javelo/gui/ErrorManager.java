package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Handles display of GUI error messages.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class ErrorManager {

    /**
     * Duration of the fade in animation of the error toast.
     */
    private static final Duration FADE_IN_DURATION = new Duration(200);

    /**
     * Duration of the error toast, at max opacity.
     */
    private static final Duration PAUSE_DURATION = new Duration(2000);

    /**
     * Duration of the fade out animation of the error toast.
     */
    private static final Duration FADE_OUT_TRANSITION = new Duration(500);

    /**
     * Final/Maximal opacity of the error toast.
     */
    private static final double FINAL_OPACITY = 0.8;

    private final Pane pane;
    private final Text text;

    private final SequentialTransition transition;

    /**
     * Constructor of an error manager.
     */
    public ErrorManager() {
        this.text = new Text();
        this.pane = new VBox(text);
        this.pane.getStylesheets().add("error.css");
        this.pane.setMouseTransparent(true);

        // Transition
        FadeTransition fadeIn = new FadeTransition(FADE_IN_DURATION);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(FINAL_OPACITY);
        PauseTransition pause = new PauseTransition(PAUSE_DURATION);
        FadeTransition fadeOut = new FadeTransition(FADE_OUT_TRANSITION);
        fadeOut.setFromValue(FINAL_OPACITY);
        fadeOut.setToValue(0);
        this.transition = new SequentialTransition(this.pane, fadeIn, pause, fadeOut);
    }

    /**
     * Returns the pane rendering the error toast.
     *
     * @return the pane rendering the error toast
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Displays the error toast on the pane for a short time, along with a beep sound.
     *
     * @param message short error message that will be displayed in the error toast
     */
    public void displayError(String message) {
        java.awt.Toolkit.getDefaultToolkit().beep(); // ASK error
        transition.stop();
        text.setText(message);
        transition.play();
    }

}
