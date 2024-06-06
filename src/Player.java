import javafx.animation.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.util.*;

/**
 * Class for a player object.
 */
public class Player extends Pane {
    /**
     * Image that stores all frames for animations of the player.
     */
    ImageView avatar = new ImageView(new Image("IcyGuy.png"));
    /**
     * Height of the player.
     */
    int height = 54;
    /**
     * Width of the player.
     */
    int width = 32;
    /**
     * Default speed of the player moving left or right.
     */
    float baseSpeed = 1.5f;
    /**
     * Current speed of the player moving left or right.
     */
    float speed = baseSpeed;
    /**
     * Determines the height of a jump.
     */
    float jumpPower;
    /**
     * Factor of how fast is player pulled to the ground when in air.
     */
    float gravityStr = .5f;
    /**
     * States if player is currently in air.
     */
    boolean inAir = false;
    /**
     * Default speed of the player moving down with the screen.
     */
    double baseMoveSpeed = (double) 5 / 24;
    /**
     * Current speed of the player moving down with the screen.
     */
    double moveSpeed = baseMoveSpeed;
    /**
     * Stores score of the player.
     */
    int score = 0;
    /**
     * True if left arrow is pressed, false if not.
     */
    boolean leftKeyPressed = false;
    /**
     * True if right arrow is pressed, false if not.
     */
    boolean rightKeyPressed = false;
    /**
     * True if Space is pressed, false if not.
     */
    boolean spaceKeyPressed = false;
    /**
     * Moves player down with the screen based on moveSpeed.
     */
    AnimationTimer moveDown = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (getLayoutY() <= 610)
                setLayoutY(getLayoutY() + moveSpeed);
        }
    };
    /**
     * States if a jump is big (true) or not (false).
     */
    boolean bigJump = false;
    /**
     * Animation for rotation during big jumps.
     */
    RotateTransition rotate = new RotateTransition(Duration.millis(1000), avatar);
    private final float acceleration = 1.04f;
    private final float inAirAcc = 1.03f;
    private boolean moveEnabled = true;
    private final Rectangle2D bigJumpFrame = new Rectangle2D(430, 6, 45, 60);
    private final Rectangle2D[] jumpFrames = {new Rectangle2D(268, 8, 32, 58),
            new Rectangle2D(307, 8, 32, 58), new Rectangle2D(346, 11, 32, 55),
            new Rectangle2D(386, 9, 32, 57)};
    private final Queue<Rectangle2D> idleFrames = new ArrayDeque<>(Arrays.asList(new Rectangle2D(4, 12, 32, 54),
            new Rectangle2D(78, 12, 32, 54), new Rectangle2D(42, 12, 32, 54)));
    private final Queue<Rectangle2D> runningFrames = new ArrayDeque<>(Arrays.asList(new Rectangle2D(117, 12, 32, 54),
            new Rectangle2D(154, 12, 32, 54),
            new Rectangle2D(191, 12, 32, 54), new Rectangle2D(229, 12, 32, 54)));
    private final AnimationTimer jumping = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (speed < 0)
                avatar.setScaleX(-1);
            else
                avatar.setScaleX(1);
            if (inAir) {
                idle.pause();
                running.pause();

                if (bigJump) {
                    avatar.setViewport(bigJumpFrame);
                    if (rotate.getStatus() != Animation.Status.RUNNING)
                        rotate.play();
                }
                else if (Math.abs(speed) == baseSpeed) {
                    avatar.setViewport(jumpFrames[0]);
                }
                else if (jumpPower > 2) {
                    avatar.setViewport(jumpFrames[1]);
                }
                else if (jumpPower <= 2 && jumpPower >= -2) {
                    avatar.setViewport(jumpFrames[2]);
                }
                else if (jumpPower < -2) {
                    avatar.setViewport(jumpFrames[3]);
                }
            }
            else if (Math.abs(speed) == baseSpeed && idle.getStatus() != Animation.Status.RUNNING) {
                running.pause();
                idle.playFrom(Duration.millis((double) 1000 / 3 - 1));
            }
            else if (Math.abs(speed) > baseSpeed && running.getStatus() != Animation.Status.RUNNING) {
                idle.pause();
                running.playFrom(Duration.millis((double) 1000 / 4 - 1));
            }
        }
    };
    private final Timeline running = new Timeline(new KeyFrame(Duration.millis((double) 1000 / 4), event -> {
        Rectangle2D currFrame = runningFrames.remove();
        avatar.setViewport(currFrame);
        runningFrames.add(currFrame);
    }));
    private final Timeline idle = new Timeline(new KeyFrame(Duration.millis((double) 1000 / 3), event -> {
        Rectangle2D currFrame = idleFrames.remove();
        avatar.setViewport(currFrame);
        idleFrames.add(currFrame);
    }));
    private final AnimationTimer move = new AnimationTimer() {
        @Override
        public void handle(long t) {
            if (moveEnabled && leftKeyPressed && getLayoutX() > 105) {
                if (speed >= 1.5) {
                    speed = -baseSpeed;
                    momentumL.start();
                }
                if (speed <= 16 && speed >= -16)
                    if (inAir)
                        speed *= inAirAcc;
                    else
                        speed *= acceleration;
                movePlayer();
            }
            if (moveEnabled && rightKeyPressed && getLayoutX() + width < 695) {
                if (speed <= 1.5) {
                    speed = baseSpeed;
                    momentumR.start();
                }
                if (speed <= 16 && speed >= -16)
                    if (inAir)
                        speed *= inAirAcc;
                    else
                        speed *= acceleration;
                movePlayer();
            }
            if (spaceKeyPressed)
                if (!inAir) {
                    inAir = true;
                    jump();
                }
        }
    };
    private boolean halve = true;
    private final AnimationTimer momentumR = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (rightKeyPressed)
                if (getLayoutX() + width >= 695) {
                    speed *= -1;
                    momentumR.stop();
                    momentumL.start();
                    moveEnabled = false;
                }
                else {
                    halve = true;
                    moveEnabled = true;
                    return;
                }
            if (speed > baseSpeed) {
                if (halve) {
                    speed /= Math.pow(1.04, 18);
                    halve = false;
                }
                speed /= acceleration;
                if (getLayoutX() + width < 695)
                    movePlayer();
                else {
                    speed *= -1;
                    momentumR.stop();
                    momentumL.start();
                }
            }
            if (Math.abs(speed) < baseSpeed + 2) {
                moveEnabled = true;
                halve = true;
            }
            if (Math.abs(speed) < baseSpeed) {
                speed = baseSpeed;
            }
         }
    };
    private final AnimationTimer momentumL = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (leftKeyPressed)
                if (getLayoutX() <= 105) {
                    speed *= -1;
                    momentumL.stop();
                    momentumR.start();
                    moveEnabled = false;
                }
                else {
                    halve = true;
                    moveEnabled = true;
                    return;
                }
            if (speed < -baseSpeed) {
                if (halve) {
                    speed /= Math.pow(1.04, 18);
                    halve = false;
                }
                speed /= acceleration;
                if (getLayoutX() > 105)
                    movePlayer();
                else {
                    speed *= -1;
                    momentumL.stop();
                    momentumR.start();
                }
            }
            if (Math.abs(speed) < baseSpeed + 2) {
                moveEnabled = true;
                halve = true;
            }
            if (Math.abs(speed) < baseSpeed) {
                speed = baseSpeed;
            }
        }
    };

    /**
     * Constructor sets up the look, animations and physics of a player.
     */
    public Player() {
        setLayoutY(496);
        setLayoutX(400);
        avatar.setFitWidth(width);
        avatar.setFitHeight(height);
        avatar.setViewport(new Rectangle2D(4, 12, 32, 54));
        getChildren().add(avatar);
        idle.setCycleCount(Animation.INDEFINITE);
        running.setCycleCount(Animation.INDEFINITE);
        idle.playFrom(Duration.millis((double) 1000 / 3 - 1));
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        jumping.start();
        move.start();
        momentumR.start();
        momentumL.start();
    }

    /**
     * Moves the player based on speed. If speed is greater/equal to 0, player moves right, if speed is less than 0, left.
     */
    public void movePlayer() {
        setLayoutX(getLayoutX() + speed);
    }

    /**
     * Sets jumpPower based on speed. If jumpPower is higher than 17, bigJump is set to true.
     */
    public void jump() {
        jumpPower = 9 * (1 + .08f * Math.abs(speed));
        if (jumpPower >= 17)
            bigJump = true;
    }
}
