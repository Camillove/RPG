package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Battle {
    private static boolean inBattle = false;

    public static void startBattle(Stage stage, Scene mapScene, Character playerChar, Monsters monstro, 
                                   ImageView playerView, ImageView enemyView, App app, StackPane originalLayout) {
        if(inBattle) return;
        inBattle = true;
        
        // --- CARREGAMENTO VISUAL ---
        ImageView bg = new ImageView(new Image(Battle.class.getResource("/images/battlebg2.png").toExternalForm()));
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        ImageView pImg = new ImageView(playerChar.getBattleSprite());
        pImg.setFitWidth(200); pImg.setPreserveRatio(true);
        
        ImageView eImg = new ImageView(new Image(Battle.class.getResource(monstro.getBattleImagePath()).toExternalForm()));
        eImg.setFitWidth(200); eImg.setPreserveRatio(true);

        // Barras de vida
        ProgressBar pBar = new ProgressBar(playerChar.getLife() / 100.0);
        ProgressBar eBar = new ProgressBar((double)monstro.getLife() / monstro.getMaxLife());
        pBar.setPrefWidth(200); eBar.setPrefWidth(200);

        // Layout de Combate
        VBox pBox = new VBox(10, pBar, pImg);
        VBox eBox = new VBox(10, eBar, eImg);
        HBox fighters = new HBox(100, pBox, eBox);
        fighters.setAlignment(Pos.CENTER);

        Button attack = new Button("Atacar");
        Button run = new Button("Fugir");
        HBox buttons = new HBox(20, attack, run);
        buttons.setAlignment(Pos.CENTER);
        buttons.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 20; -fx-background-radius: 10;");

        BorderPane root = new BorderPane();
        root.setCenter(fighters);
        root.setBottom(buttons);
        BorderPane.setMargin(buttons, new Insets(0, 0, 50, 0));

        // Layout principal da Batalha
        StackPane battleLayout = new StackPane(bg, root);
        
        // Efeito de entrada
        battleLayout.setOpacity(0);

        // --- A MÁGICA ACONTECE AQUI ---
        // Em vez de recriar a cena, nós trocamos o recheio dela
        mapScene.setRoot(battleLayout);
        
        // Garante que o modo tela cheia não caia durante a transição
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        }

        // Fade In da batalha
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), battleLayout);
        fadeIn.setToValue(1);
        fadeIn.play();

        // --- LÓGICA E ANIMAÇÕES ---
        attack.setOnAction(e -> {
            buttons.setDisable(true);
            playAttackAnimation(pImg, eImg, () -> {
                monstro.setLife(monstro.getLife() - 20);
                eBar.setProgress((double)monstro.getLife() / monstro.getMaxLife());
                
                if(monstro.getLife() <= 0) {
                    // Passamos originalLayout para poder devolvê-lo à cena
                    exitBattle(stage, mapScene, app, battleLayout, originalLayout);
                } else {
                    PauseTransition pause = new PauseTransition(Duration.millis(600));
                    pause.setOnFinished(ev -> {
                        playerChar.setLife(playerChar.getLife() - 10);
                        pBar.setProgress(playerChar.getLife() / 100.0);
                        playShakeAnimation(pImg);
                        buttons.setDisable(false);
                    });
                    pause.play();
                }
            });
        });

        run.setOnAction(e -> exitBattle(stage, mapScene, app, battleLayout, originalLayout));
    }

    private static void playAttackAnimation(ImageView attacker, ImageView victim, Runnable onFinished) {
        TranslateTransition move = new TranslateTransition(Duration.millis(200), attacker);
        move.setByX(150);
        move.setAutoReverse(true);
        move.setCycleCount(2);
        move.setOnFinished(e -> onFinished.run());
        move.play();
    }

    private static void playShakeAnimation(ImageView target) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), target);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    // Adicionamos originalLayout nos parâmetros
    private static void exitBattle(Stage stage, Scene mapScene, App app, StackPane battleLayout, StackPane originalLayout){
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), battleLayout);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            inBattle = false;
            
            // --- A MÁGICA DE VOLTAR ---
            // Devolvemos o mapa (originalLayout) para a cena principal!
            mapScene.setRoot(originalLayout);
            
            // Reajusta opacidade para garantir que o mapa apareça
            originalLayout.setOpacity(1); 
            
            if (!stage.isFullScreen()) {
                stage.setFullScreen(true);
            }
            
            // Avisa o App que acabou
            app.resumeTimers();
        });
        fadeOut.play();
    }
}