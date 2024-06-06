import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Class for a wall object.
 */
public class Wall extends Rectangle {
    /**
     * Image used for the wall graphic.
     */
    Image img = new Image("wall.jpg");
    /**
     * Default movement speed.
     */
    double baseMoveSpeed = (double) 5 / 12;
    /**
     * Current movement speed.
     */
    double moveSpeed = baseMoveSpeed;
    /**
     * States whether the walls are closed on the screen.
     */
    boolean closed = false;
    /**
     * Moves the wall based on its moveSpeed.
     */
    AnimationTimer move = new AnimationTimer() {
        @Override
        public void handle(long l) {
            wallMove = (wallMove + moveSpeed) % 128;
            setFill(new ImagePattern(img, 0, wallMove, 128, 128, false));
        }
    };
    /**
     * Animation of a closing wall.
     */
    AnimationTimer close = new AnimationTimer() {
        @Override
        public void handle(long l) {
            setLayoutX(getLayoutX() + isLeft * 5);
            if ((isLeft == 1 && getLayoutX() == -100) || (isLeft == -1 && getLayoutX() == 400)) {
                closed = true;
                close.stop();
            }
        }
    };
    /**
     * Animation of an opening wall.
     */
    AnimationTimer open = new AnimationTimer() {
        @Override
        public void handle(long l) {
            setLayoutX(getLayoutX() - isLeft * 5);
            if ((isLeft == 1 && getLayoutX() == -400) || (isLeft == -1 && getLayoutX() == 700)) {
                open.stop();
            }
        }
    };
    private double wallMove = 0;
    private final int isLeft;

    /**
     * Constructor sets up the look of a wall.
     * @param x x-coordinate of the wall
     * @param y y-coordinate of the wall
     * @param isLeft 1 if it is the left wall, -1 if it is the right wall. This value is used to move the wall in the right direction when opening or closing.
     */
    public Wall(int x, int y, int isLeft) {
        this.isLeft = isLeft;
        setLayoutY(y);
        setLayoutX(x);
        setHeight(800);
        setWidth(500);
        setFill(new ImagePattern(img, 0, wallMove, 128, 128, false));
        setStrokeWidth(3);
        setStroke(Color.rgb(47, 71, 71));
    }
}
