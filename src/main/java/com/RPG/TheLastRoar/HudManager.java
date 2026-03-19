package com.RPG.TheLastRoar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Gerencia a HUD (Heads-Up Display) do jogador:
 * avatar, barra de HP, barra de XP, nível e ouro.
 */
public class HudManager {

    private HBox hudLayout;
    private ProgressBar hpBar;
    private ProgressBar xpBar;
    private Label lblHp;
    private Label lblXp;
    private Label lblGold;
    private Label lblLevel;

    private final Stage stage;

    public HudManager(Stage stage) {
        this.stage = stage;
        criar();
    }

    private void criar() {
        hudLayout = new HBox(15);
        hudLayout.setAlignment(Pos.CENTER_LEFT);
        hudLayout.setStyle(
            "-fx-background-color: rgba(20, 20, 20, 0.8);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #DAA520;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10px;"
        );
        hudLayout.setMaxSize(
            javafx.scene.layout.Region.USE_PREF_SIZE,
            javafx.scene.layout.Region.USE_PREF_SIZE
        );

        ImageView avatar = carregarAvatar();

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        lblLevel = new Label("Nível: 1");
        lblLevel.setTextFill(Color.CYAN);
        lblLevel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox hpBox = new HBox(10);
        lblHp = new Label("HP: 100/100");
        lblHp.setTextFill(Color.WHITE);
        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(150);
        hpBar.setStyle("-fx-accent: #e74c3c;");
        hpBox.getChildren().addAll(lblHp, hpBar);

        HBox xpBox = new HBox(10);
        lblXp = new Label("XP: 0/100");
        lblXp.setTextFill(Color.WHITE);
        xpBar = new ProgressBar(0.0);
        xpBar.setPrefWidth(150);
        xpBar.setStyle("-fx-accent: #2ecc71;");
        xpBox.getChildren().addAll(lblXp, xpBar);

        lblGold = new Label("Ouro: 0");
        lblGold.setTextFill(Color.GOLD);
        lblGold.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        statsBox.getChildren().addAll(lblLevel, hpBox, xpBox, lblGold);

        if (avatar.getImage() != null) hudLayout.getChildren().add(avatar);
        hudLayout.getChildren().add(statsBox);

        javafx.scene.layout.StackPane.setAlignment(hudLayout, Pos.TOP_LEFT);
        javafx.scene.layout.StackPane.setMargin(hudLayout, new Insets(20));
    }

    private ImageView carregarAvatar() {
        try {
            ImageView avatar = new ImageView(
                new Image(getClass().getResource("/images/avatar.png").toExternalForm())
            );
            avatar.setFitWidth(64);
            avatar.setFitHeight(64);
            return avatar;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    /**
     * Atualiza todos os elementos da HUD com os dados atuais do jogador.
     */
    public void atualizar(Character player) {
        if (player == null) return;

        int vidaAtual = player.getLife();
        int vidaMaxima = 100;
        lblHp.setText("HP: " + vidaAtual + " / " + vidaMaxima);
        hpBar.setProgress((double) vidaAtual / vidaMaxima);

        lblLevel.setText("Nível: " + player.getNivel());

        int xpAtual = player.getXp();
        int xpMaximo = 100;
        lblXp.setText("XP: " + xpAtual + " / " + xpMaximo);
        xpBar.setProgress((double) xpAtual / xpMaximo);

        lblGold.setText("Ouro: " + player.getCoin());
    }

    public HBox getLayout() {
        return hudLayout;
    }
}
