package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Battle {
    private static boolean inBattle = false;

    public static void startBattle(Stage stage, Scene mapScene, Character playerChar, Goblin goblin,
                                    ImageView playerView, ImageView enemyMapView, App app){
        if(inBattle) return;
        inBattle = true;

        // Fundo que estica para a tela toda
        ImageView bg = new ImageView(new Image(Battle.class.getResource("/images/battlebg2.png").toExternalForm()));
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // Área de luta com tamanho fixo (800x600) centralizada
        Pane gameArea = new Pane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMaxSize(800, 600);

        ImageView pImg = new ImageView(playerChar.getBattleSprite());
        pImg.setFitWidth(200); pImg.setPreserveRatio(true);
        pImg.setX(120); pImg.setY(320);

        ImageView eImg = new ImageView(new Image(Battle.class.getResource("/images/goblin.png").toExternalForm()));
        eImg.setFitWidth(200); eImg.setPreserveRatio(true);
        eImg.setX(500); eImg.setY(120);

        ProgressBar pBar = new ProgressBar(playerChar.getLife()/100.0);
        pBar.setLayoutX(80); pBar.setLayoutY(280); pBar.setPrefWidth(200);

        ProgressBar eBar = new ProgressBar(goblin.getLife()/80.0);
        eBar.setLayoutX(500); eBar.setLayoutY(80); eBar.setPrefWidth(200);

        Button attack = new Button("Atacar");
        Button defend = new Button("Defender");
        Button run = new Button("Fugir");
        HBox buttons = new HBox(20, attack, defend, run);
        buttons.setAlignment(Pos.CENTER); buttons.setLayoutX(250); buttons.setLayoutY(500);

        gameArea.getChildren().addAll(pImg, eImg, pBar, eBar, buttons);

        // StackPane que junta o fundo esticado com a área de jogo centralizada
        StackPane battleLayout = new StackPane(bg, gameArea);
        battleLayout.setOpacity(0);
        battleLayout.setStyle("-fx-background-color: black;");
        
        stage.setScene(new Scene(battleLayout));
        stage.setFullScreen(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), battleLayout);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.play();

        attack.setOnAction(e -> {
            buttons.setDisable(true);
            playAtk(pImg, 60, () -> {
                goblin.setLife(goblin.getLife() - 20);
                eBar.setProgress(goblin.getLife()/80.0);
                showHit(gameArea, eImg);

                PauseTransition pause = new PauseTransition(Duration.millis(600));
                pause.setOnFinished(ev -> {
                    if(goblin.getLife() <= 0) exitBattle(stage, mapScene, app, battleLayout);
                    else {
                        playAtk(eImg, -60, () -> {
                            playerChar.setLife(playerChar.getLife() - 10);
                            pBar.setProgress(playerChar.getLife()/100.0);
                            showHit(gameArea, pImg);
                            if(playerChar.getLife() <= 0) stage.close();
                            else buttons.setDisable(false);
                        });
                    }
                });
                pause.play();
            });
        });

        run.setOnAction(e -> exitBattle(stage, mapScene, app, battleLayout));
    }

    private static void playAtk(ImageView v, double d, Runnable callback){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), v);
        tt.setByX(d); tt.setCycleCount(2); tt.setAutoReverse(true);
        tt.setOnFinished(e -> callback.run());
        tt.play();
    }

    private static void showHit(Pane root, ImageView target){
        try {
            ImageView hit = new ImageView(new Image(Battle.class.getResource("/images/hit.png").toExternalForm()));
            hit.setFitWidth(150); hit.setPreserveRatio(true);
            hit.setX(target.getX() + 25); hit.setY(target.getY() + 25);
            root.getChildren().add(hit);
            FadeTransition ft = new FadeTransition(Duration.millis(300), hit);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(e -> root.getChildren().remove(hit));
            ft.play();
        } catch(Exception e) {}
    }

    private static void exitBattle(Stage stage, Scene mapScene, App app, StackPane layout){
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), layout);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            inBattle = false;
            stage.setScene(mapScene);
            stage.setFullScreen(true);
            app.resumeTimers();
        });
        fadeOut.play();
    }
}