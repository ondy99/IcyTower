import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import java.util.Random;

/**
 * Class for platform object.
 */
public class Platform extends Rectangle {
    /**
     * Width of a platform.
     */
    int width;
    /**
     * Identifier number of a platform.
     */
    int id;
    /**
     * Default movement speed of a platform.
     */
    double baseMoveSpeed = (double) 5 / 24;
    /**
     * Current movement speed of a platform.
     */
    double moveSpeed = baseMoveSpeed;
    /**
     * Moves the platform based on its moveSpeed.
     */
    AnimationTimer move = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (getLayoutY() <= 610)
                setLayoutY(getLayoutY() + moveSpeed);
        }
    };
    private final AnimationTimer regenerate = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (id >= 0 && getLayoutY() >= 600) {
                id += 8;
                setLayoutY(getLayoutY() - 8 * 90);
                setLayoutX(random.nextInt(550 - 104) + 104);
                int platformMinWidth = 150;
                int platformMaxWidth = 250;
                width = random.nextInt(platformMaxWidth - platformMinWidth) + platformMinWidth;
                setWidth(width);
                fixEdge();
            }
        }
    };
    private final Random random = new Random();

    /**
     * Constructor sets up the look of the platform. Also takes care of correct location on the screen.
     * @param id sets id value
     * @param x x-coordinate of the platform
     * @param y y-coordinate of the platform
     * @param width width of the platform
     */
    public Platform(int id, int x, int y, int width) {
        this.id = id;
        this.width = width;
        setLayoutY(y);
        setLayoutX(x);
        setHeight(32);
        setWidth(width);
        Image img = new Image("stone.png");
        setFill(new ImagePattern(img, 0, 0, 32, 32, false));
        setStrokeWidth(3);
        setStrokeType(StrokeType.OUTSIDE);
        setStroke(Color.BLACK);
        fixEdge();
        regenerate.start();
    }

    private void fixEdge() {
        if (getLayoutX() + width > 696) {
            setLayoutX(getLayoutX() - (getLayoutX() + width - 696));
        }
    }
}
