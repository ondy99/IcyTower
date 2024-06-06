import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

/**
 * Class for a sign object that labels every 10th platform.
 */

public class Sign extends Label {
    /**
     * Default movement speed.
     */
    double baseMoveSpeed = (double) 5 / 24;
    /**
     *  Current movement speed.
     */
    double moveSpeed = baseMoveSpeed;
    /**
     * States whether the Sign is currently being used on a Platform.
     */
    boolean inUse = false;
    /**
     * Moves the Sign down based on its moveSpeed. If it goes out of screen, inUse is set to false.
     */
    AnimationTimer move = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (getLayoutY() <= 610)
                setLayoutY(getLayoutY() + moveSpeed);
            else {
                inUse = false;
            }
        }
    };

    /**
     * Constructor sets up the look of the sign.
     */
    public Sign() {
        setText("10");
        setFont(new Font("Arial Bold", 12));
        ImageView sign = new ImageView(new Image("Base-01.png"));
        setGraphic(sign);
        sign.setViewport(new Rectangle2D(16, 272, 48, 16));
        setContentDisplay(ContentDisplay.CENTER);
    }
}
