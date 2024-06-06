import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main class of the game.
 */
public class Game extends Application {
    private final Pane root = new Pane();
    private final Pane startPane = new Pane();
    private final Player player = new Player();
    private final ImageView logo = new ImageView(player.avatar.getImage());
    private final Pane gameOverPane = new StackPane();
    private final Text gameOverText = new Text("GAME OVER");
    private final Text finalScore = new Text();
    private final Wall wallL = new Wall(-100, -100, 1);
    private final Wall wallR = new Wall(400, -100, -1);
    private final Text score = new Text(15, 585, "Score: " + player.score);
    private final Sign sign = new Sign();
    private final Platform bigPlatform = new Platform(-1, 104, 550, 592);
    private Platform stand = bigPlatform;
    private final List<Platform> platforms = new ArrayList<>();
    private final Random random = new Random();
    private final Image img = new Image("wallDark.jpg");
    private double bgMove = 0;
    private final double baseBgMoveSpeed = (double) 1 / 9;
    private double bgSpeed = baseBgMoveSpeed;
    private AnimationTimer gameOverCheck;
    private final AnimationTimer bgMoveDown = new AnimationTimer() {
        @Override
        public void handle(long l) {
            bgMove = (bgMove + bgSpeed) % 128;
            root.setBackground(new Background(new BackgroundFill(new ImagePattern(img,
                    0, bgMove, 128, 128, false), null, null)));
        }
    };
    private final AnimationTimer screenMoveStart = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (player.getLayoutY() <= 125) {
                for (Platform p : platforms) {
                    p.move.start();
                }
                wallL.move.start();
                wallR.move.start();
                bigPlatform.move.start();
                player.moveDown.start();
                bgMoveDown.start();
                sign.move.start();
                // timer stops because it's no longer needed
                screenMoveStart.stop();
            }
        }
    };
    private final AnimationTimer fasterScreen = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (!sign.inUse)
                for (Platform p : platforms) {
                    if ((p.id + 1) % 10 == 0) {
                        sign.setText("" + (p.id + 1));
                        sign.setLayoutX(p.getLayoutX() + (double) p.width / 2 - sign.getWidth() / 2);
                        sign.setLayoutY(p.getLayoutY() + 7.5);
                        sign.inUse = true;
                    }
                }
            double screenSpeedFactor = .5 * (player.score / 500);
            if (player.getLayoutY() < 125) {
                int fast = 5;
                wallL.moveSpeed = fast + (wallL.baseMoveSpeed + screenSpeedFactor);
                wallR.moveSpeed = fast + (wallR.baseMoveSpeed + screenSpeedFactor);
                player.moveSpeed = fast + (player.baseMoveSpeed + screenSpeedFactor);
                sign.moveSpeed = fast + (sign.baseMoveSpeed + screenSpeedFactor);
                bgSpeed = fast + (baseBgMoveSpeed + screenSpeedFactor);
                bigPlatform.moveSpeed = fast + (bigPlatform.baseMoveSpeed + screenSpeedFactor);
                for (Platform p : platforms) {
                    p.moveSpeed = fast + (p.baseMoveSpeed + screenSpeedFactor);
                }
            }
            else {
                wallL.moveSpeed = wallL.baseMoveSpeed + screenSpeedFactor;
                wallR.moveSpeed = wallR.baseMoveSpeed + screenSpeedFactor;
                player.moveSpeed = player.baseMoveSpeed + screenSpeedFactor;
                sign.moveSpeed = sign.baseMoveSpeed + screenSpeedFactor;
                bgSpeed = baseBgMoveSpeed + screenSpeedFactor;
                bigPlatform.moveSpeed = bigPlatform.baseMoveSpeed + screenSpeedFactor;
                for (Platform p : platforms) {
                    p.moveSpeed = p.baseMoveSpeed + screenSpeedFactor;
                }
            }
        }
    };
    private final AnimationTimer gravity = new AnimationTimer() {
        @Override
        public void handle(long t) {
            if (player.inAir)
                player.jumpPower = Math.max(player.jumpPower - player.gravityStr, -20);
            else
                player.jumpPower = 0;
            player.setLayoutY(player.getLayoutY() - player.jumpPower);
            // Jumping on a higher platform
            for (Platform p : platforms) {
                if (player.getLayoutY() + player.height <= p.getLayoutY()
                        && player.getLayoutX() + player.width >= p.getLayoutX()
                        && player.getLayoutX() <= p.getLayoutX() + p.width
                        && p.id > stand.id) {
                    stand = p;
                }
            }
            // Falling off a platform
            if (player.getLayoutX() + player.width < stand.getLayoutX()
                    || player.getLayoutX() > stand.getLayoutX() + stand.width) {
                player.inAir = true;
                stand = bigPlatform;
            }
            // Standing on a platform
            if (player.getLayoutY() >= stand.getLayoutY() - player.height
                    && player.getLayoutY() <=  stand.getLayoutY()
                    && player.getLayoutX() + player.width >= stand.getLayoutX()
                    && player.getLayoutX() <= stand.getLayoutX() + stand.width) {
                player.inAir = false;
                player.bigJump = false;
                player.rotate.stop();
                player.avatar.setRotate(0);
                player.score = Math.max(player.score, stand.id * 10 + 10);
                score.setText("Score: " + player.score);
                player.setLayoutY(stand.getLayoutY() - player.height);
            }
        }
    };

    private void setupGameScene() {
        int platformMinWidth = 150;
        int platformMaxWidth = 250;
        for (int i = 1; i <= 8; i++) {
            var platform = new Platform(i - 1,random.nextInt(550 - 104) + 104, 550 - 90 * i,
                    random.nextInt(platformMaxWidth - platformMinWidth) + platformMinWidth);
            platforms.add(platform);
            root.getChildren().add(platform);
        }
        root.setBackground(new Background(new BackgroundFill(new ImagePattern(img,
                0, bgMove, 128, 128, false), null, null)));
        score.setFill(Color.WHITESMOKE);
        score.setStroke(Color.BLACK);
        score.setStrokeWidth(2);
        score.setFont(new Font("Impact", 26));
        root.getChildren().addAll(sign, player, bigPlatform, wallL, wallR, score);
        screenMoveStart.start();
    }
    private void setupPlayerMovement(Scene scene) {
        gravity.start();
        fasterScreen.start();
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> player.leftKeyPressed = true;
                case RIGHT -> player.rightKeyPressed = true;
                case SPACE -> player.spaceKeyPressed = true;
            }
        });
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT -> player.leftKeyPressed = false;
                case RIGHT -> player.rightKeyPressed = false;
                case SPACE -> player.spaceKeyPressed = false;
            }
        });
    }

    private void setupGameOverScene() {
        gameOverPane.setBackground(new Background(new BackgroundFill(new ImagePattern(wallL.img,
                0, 0, 128, 128, false), null, null)));
        gameOverText.setFill(Color.WHITESMOKE);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(4);
        gameOverText.setFont(new Font("Impact", 104));
        gameOverText.setTranslateY(-80);
        finalScore.setFill(Color.WHITESMOKE);
        finalScore.setStroke(Color.BLACK);
        finalScore.setStrokeWidth(2);
        finalScore.setFont(new Font("Impact", 39));
        finalScore.setTextAlignment(TextAlignment.CENTER);
        finalScore.setTranslateY(50);
        gameOverPane.getChildren().addAll(gameOverText, finalScore);
    }

    private void setupStartScene() {
        startPane.setBackground(gameOverPane.getBackground());
        logo.setLayoutX(50);
        logo.setLayoutY(70);
        logo.setPreserveRatio(true);
        logo.setFitWidth(300);
        logo.setViewport(new Rectangle2D(673, 5, 151, 127));
        startPane.getChildren().add(logo);
        var scores = loadScores();
        Text board = new Text("TOP 10 SCORES\n______________________\n" + String.join("\n", scores));
        Text controls = new Text("SPACE · Play\nESC · Quit");
        controls.setFill(Color.WHITESMOKE);
        controls.setStroke(Color.BLACK);
        controls.setStrokeWidth(2);
        controls.setFont(new Font("Impact", 39));
        controls.setTextAlignment(TextAlignment.CENTER);
        controls.setLayoutX(100);
        controls.setLayoutY(400);
        board.setFill(Color.WHITESMOKE);
        board.setStroke(Color.BLACK);
        board.setStrokeWidth(2);
        board.setFont(new Font("Impact", 32));
        board.setTextAlignment(TextAlignment.CENTER);
        board.setLayoutX(380);
        board.setLayoutY(100);
        startPane.getChildren().addAll(board, controls);
    }

    /**
     * Sets up the start screen, main screen and end screen. Enables player movement and screen movement.
     * @param stage Stage where the game is shown
     */
    @Override
    public void start(Stage stage) {
        Scene gameScene = new Scene(root, 800, 600);
        setupGameScene();
        setupPlayerMovement(gameScene);
        Scene gameOverScene = new Scene(gameOverPane, 800, 600);
        setupGameOverScene();
        Scene startScene = new Scene(startPane, 800, 600);
        setupStartScene();

        gameOverScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.SPACE) {
                    restart(stage);
                    stage.setScene(gameScene);
                }
                else if (event.getCode() == KeyCode.ESCAPE)
                    stage.close();
            }
        });
        gameOverCheck = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (player.getLayoutY() + player.height >= 600) {
                    player.setLayoutY(700);
                    wallL.move.stop();
                    wallR.move.stop();
                    wallL.close.start();
                    wallR.close.start();
                    if (wallL.closed && wallR.closed) {
                        stage.setScene(gameOverScene);
                        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                        saveScores(String.format("%6d" + "%29s", player.score, date));
                        finalScore.setText("Your Score: " + player.score + "\nPress SPACE to return to menu\nPress ESC to quit");
                        gameOverCheck.stop();
                    }
                }
            }
        };
        gameOverCheck.start();

        startScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.SPACE) {
                    stage.setScene(gameScene);
                    wallL.open.start();
                    wallR.open.start();
                }
                else if (event.getCode() == KeyCode.ESCAPE)
                    stage.close();
            }
        });

        stage.setScene(startScene);
        stage.setTitle("Icy Tower");
        stage.setResizable(false);
        stage.show();
    }
    private void restart(Stage st) {
        Game newGame = new Game();
        newGame.start(new Stage());
        st.close();
    }
    private List<String> loadScores() {
        List<String> topScores = new ArrayList<>();
        try {
            topScores = Files.readAllLines(Paths.get("scoreboard.txt"));
            topScores.sort(Collections.reverseOrder());
        } catch (IOException e) {
            return topScores;
        }
        return topScores;
    }

    private void saveScores(String newScore) {
        var scores = loadScores();
        scores.add(newScore);
        scores.sort(Collections.reverseOrder());
        if (scores.size() == 11)
            scores.remove(10);
        try {
            FileWriter myWriter = new FileWriter("scoreboard.txt");
            myWriter.write(String.join("\n", scores) + "\n");
            myWriter.close();
        } catch (IOException ignored) {}
    }

    /**
     * Starts the game.
     * @param args arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
