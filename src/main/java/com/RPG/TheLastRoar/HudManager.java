package com.RPG.TheLastRoar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * ============================================================
 * HudManager.java — Heads-Up Display (HUD) do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Exibe em tempo real: Nível, HP (barra + numérico), XP (barra + numérico),
 * Resistência, Ouro. Posicionado canto superior esquerdo.
 *
 * FIXES APLICADOS:
 * - CRITICAL: Removido hardcode maxLife=100, maxXp=100 → usa player.getMaxLife()
 * - CRITICAL: Removido hardcode → usa player.getXpNecessary() 
 * - NOVO: Adicionado display de Resistência (player.getResistance())
 * - Avatar `/images/avatar.png` removido (arquivo inexistente)
 * 
 * DEPENDENCIES:
 * - Character.java (getMaxLife(), getXpNecessary(), getResistance())
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

        VBox statsBox = new VBox(8);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // ── NÍVEL ──────────────────────────────────────────────────────────
        lblLevel = new Label("Nível: 1");
        lblLevel.setTextFill(Color.CYAN);
        lblLevel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statsBox.getChildren().add(lblLevel);

        // ── HP ─────────────────────────────────────────────────────────────
        HBox hpBox = new HBox(12);
        lblHp = new Label("HP: 100/100");
        lblHp.setTextFill(Color.WHITE);
        lblHp.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(160);
        hpBar.setPrefHeight(12);
        hpBar.setStyle("-fx-accent: #e74c3c; -fx-background-color: #333;");
        HBox.setHgrow(hpBar, javafx.scene.layout.Priority.ALWAYS);
        hpBox.getChildren().addAll(lblHp, hpBar);
        statsBox.getChildren().add(hpBox);

        // ── XP ─────────────────────────────────────────────────────────────
        HBox xpBox = new HBox(12);
        lblXp = new Label("XP: 0/10");
        lblXp.setTextFill(Color.WHITE);
        lblXp.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        xpBar = new ProgressBar(0.0);
        xpBar.setPrefWidth(160);
        xpBar.setPrefHeight(12);
        xpBar.setStyle("-fx-accent: #2ecc71; -fx-background-color: #333;");
        HBox.setHgrow(xpBar, javafx.scene.layout.Priority.ALWAYS);
        xpBox.getChildren().addAll(lblXp, xpBar);
        statsBox.getChildren().add(xpBox);

        // ── RESISTÊNCIA (NOVO) ─────────────────────────────────────────────
        Label lblRes = new Label("Resistência: 2");
        lblRes.setId("lblResistencia");  // ID para lookup no atualizar()
        lblRes.setTextFill(Color.ORANGE);
        lblRes.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        statsBox.getChildren().add(lblRes);

        // ── OURO ───────────────────────────────────────────────────────────
        lblGold = new Label("🪙 Ouro: 0");
        lblGold.setTextFill(Color.GOLD);
        lblGold.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statsBox.getChildren().add(lblGold);

        hudLayout.getChildren().add(statsBox);

        javafx.scene.layout.StackPane.setAlignment(hudLayout, Pos.TOP_LEFT);
        javafx.scene.layout.StackPane.setMargin(hudLayout, new Insets(20));
    }

    // Avatar removido: /images/avatar.png inexistente
    // HUD sem avatar mantém visual limpo e sem erros

    /**
     * ── ATUALIZAÇÃO EM TEMPO REAL ──────────────────────────────────────────
     * Atualiza TODOS os elementos HUD com dados dinâmicos do player.
     * 
     * FIXES:
     * - maxLife: player.getMaxLife() (corrige levelup bug)
     * - xpMax: player.getXpNecessary() (dinâmico por level)
     * - NOVO: lblRes com player.getResistance() (inclui armadura equipada)
     */
    public void atualizar(Character player) {
        if (player == null) return;

        // HP dinâmico (corrige bug levelup)
        int vidaAtual = player.getLife();
        int vidaMaxima = player.getMaxLife();  // ← FIX CRITICAL
        lblHp.setText("HP: " + vidaAtual + "/" + vidaMaxima);
        hpBar.setProgress((double) vidaAtual / Math.max(1, vidaMaxima));

        // Nível
        lblLevel.setText("Nível: " + player.getNivel());

        // XP dinâmico (corrige bug levelup)
        int xpAtual = player.getXp();
        int xpMaximo = player.getXpNecessary();  // ← FIX CRITICAL
        lblXp.setText("XP: " + xpAtual + "/" + xpMaximo);
        xpBar.setProgress((double) xpAtual / Math.max(1, xpMaximo));

        // Resistência TOTAL (base + armadura)
        Label lblRes = (Label) hudLayout.lookup("#lblResistencia");  // ID adicionado no criar()
        if (lblRes != null) {
            lblRes.setText("Resistência: " + player.getResistance());
        }

        // Ouro
        lblGold.setText("🪙 Ouro: " + player.getCoin());
    }

    public HBox getLayout() {
        return hudLayout;
    }
}
