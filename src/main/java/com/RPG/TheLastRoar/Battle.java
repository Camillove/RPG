package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Battle.java — Batalha por turnos.
 *
 * ALTERACOES NESTA VERSAO:
 *
 * 1. startBattle() recebe HudManager como ultimo parametro
 *    (pode ser null — sempre verificado antes de usar).
 *
 * 2. DROP DE MOEDAS ao vencer:
 *    - playerChar.addCoin(monstro.getDropCoin()) chamado ao derrotar o monstro
 *    - Mensagem mostra "+X moedas  +30 XP"
 *    - hudManager.atualizar() chamado para refletir as moedas no HUD
 *
 * 3. BOTAO MOCHILA FUNCIONAL:
 *    - Lista todas as Potion do inventario com botoes clicaveis
 *    - Ao usar a pocao: cura o jogador, remove do inventario, passa o turno
 *    - Mensagem "Sem pocoes!" se inventario vazio
 *
 * 4. enemyTurn() usa monstro.getDamage() e player.getResistance() corretamente
 *    (sem mais dano fixo de 15)
 */
public class Battle {
    private static boolean inBattle = false;

    public static void startBattle(Stage stage, Scene mapScene, Character playerChar,
                                   Monsters monstro, ImageView playerView, ImageView enemyView,
                                   App app, StackPane originalLayout, HudManager hudManager) {
        if (inBattle) return;
        inBattle = true;

        // ─── FUNDO ────────────────────────────────────────────────────────────
        ImageView bg = new ImageView();
        try {
            var bgUrl = Battle.class.getResource("/images/battlebg2.png");
            if (bgUrl != null) bg.setImage(new Image(bgUrl.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("Erro ao carregar fundo: " + ex.getMessage());
        }
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // ─── SPRITES ──────────────────────────────────────────────────────────
        ImageView pImg = new ImageView();
        try { if (playerChar.getBattleSprite() != null) pImg.setImage(playerChar.getBattleSprite()); }
        catch (Exception ex) { /* ignora */ }
        pImg.setFitWidth(200); pImg.setPreserveRatio(true);

        ImageView eImg = new ImageView();
        try {
            var eUrl = Battle.class.getResource(monstro.getBattleImagePath());
            if (eUrl != null) eImg.setImage(new Image(eUrl.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("Erro sprite monstro: " + ex.getMessage());
        }
        eImg.setFitWidth(200); eImg.setPreserveRatio(true);

        // ─── STATUS ───────────────────────────────────────────────────────────
        VBox opponentStatus = createMonsterStatusBox(
            monstro.getName(), monstro.getLife(), monstro.getMaxLife());
        StackPane.setMargin(opponentStatus, new Insets(50, 0, 0, 50));
        StackPane.setAlignment(opponentStatus, Pos.TOP_LEFT);

        VBox playerStatus = createPlayerStatusBox(
            playerChar.getName(), playerChar.getNivel(),
            playerChar.getLife(), playerChar.getMaxLife(),
            playerChar.getXp(), playerChar.getMaxXp());
        StackPane.setMargin(playerStatus, new Insets(0, 50, 50, 0));
        StackPane.setAlignment(playerStatus, Pos.BOTTOM_RIGHT);

        // ─── ARENA ────────────────────────────────────────────────────────────
        VBox pSpriteBox = new VBox(pImg); pSpriteBox.setAlignment(Pos.BOTTOM_LEFT);
        VBox eSpriteBox = new VBox(eImg); eSpriteBox.setAlignment(Pos.TOP_RIGHT);
        pSpriteBox.setTranslateX(150); pSpriteBox.setTranslateY(-100);
        eSpriteBox.setTranslateX(-150); eSpriteBox.setTranslateY(150);
        StackPane arenaArea = new StackPane(pSpriteBox, eSpriteBox, opponentStatus, playerStatus);

        // ─── MENU INFERIOR ────────────────────────────────────────────────────
        HBox bottomMenu = new HBox(15);
        bottomMenu.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-color: #555; -fx-border-width: 4 0 0 0;");
        bottomMenu.setPrefHeight(150); bottomMenu.setMinHeight(150);
        bottomMenu.setAlignment(Pos.CENTER);

        Label promptText = new Label("O que " + playerChar.getName() + "\nvai fazer?");
        promptText.setStyle("-fx-background-color: white; -fx-border-color: #444; -fx-border-width: 5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 30; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', sans-serif;");
        promptText.setMaxHeight(Double.MAX_VALUE);
        promptText.setPrefWidth(500);
        HBox.setHgrow(promptText, Priority.ALWAYS);

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10); buttonGrid.setVgap(10);
        buttonGrid.setStyle("-fx-background-color: white; -fx-border-color: #444; -fx-border-width: 5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");
        buttonGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // ─── BOTOES PRINCIPAIS ────────────────────────────────────────────────
        Button fightBtn   = createPokeButton("LUTAR",   "#ff8c94");
        Button bagBtn     = createPokeButton("MOCHILA", "#fcd059"); // Agora funciona!
        Button pokemonBtn = createPokeButton("HEROIS",  "#85d685");
        Button runBtn     = createPokeButton("FUGIR",   "#7aa9f5");
        pokemonBtn.setDisable(true);

        buttonGrid.add(fightBtn,   0, 0);
        buttonGrid.add(bagBtn,     1, 0);
        buttonGrid.add(pokemonBtn, 0, 1);
        buttonGrid.add(runBtn,     1, 1);

        bottomMenu.getChildren().addAll(promptText, buttonGrid);

        BorderPane uiLayer = new BorderPane();
        uiLayer.setCenter(arenaArea);
        uiLayer.setBottom(bottomMenu);

        StackPane root = new StackPane(bg, uiLayer);
        root.setOpacity(0);

        mapScene.setRoot(root);
        if (!stage.isFullScreen()) stage.setFullScreen(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setToValue(1);
        fadeIn.play();

        // ─────────────────────────────────────────────────────────────────────
        // LÓGICA DOS BOTÕES
        // ─────────────────────────────────────────────────────────────────────

        // Helper: restaura o menu principal de acoes
        Runnable voltarMenu = () -> {
            promptText.setText("O que " + playerChar.getName() + "\nvai fazer?");
            buttonGrid.getChildren().clear();
            buttonGrid.add(fightBtn, 0, 0); buttonGrid.add(bagBtn, 1, 0);
            buttonGrid.add(pokemonBtn, 0, 1); buttonGrid.add(runBtn, 1, 1);
            buttonGrid.setDisable(false);
        };

        // ── FUGIR ─────────────────────────────────────────────────────────────
        runBtn.setOnAction(e -> {
            if (playerChar.leave()) {
                promptText.setText("Fugiu com sucesso!");
                PauseTransition p = new PauseTransition(Duration.millis(800));
                p.setOnFinished(ev ->
                    exitBattle(stage, mapScene, app, root, originalLayout, hudManager, playerChar));
                p.play();
            } else {
                promptText.setText("Nao conseguiu fugir!");
                buttonGrid.setDisable(true);
                PauseTransition p = new PauseTransition(Duration.millis(1000));
                p.setOnFinished(ev ->
                    enemyTurn(playerChar, monstro, playerStatus, pImg, root, app, buttonGrid, hudManager));
                p.play();
            }
        });

        // ── LUTAR ─────────────────────────────────────────────────────────────
        fightBtn.setOnAction(e -> {
            promptText.setText("Escolha seu ataque!");
            buttonGrid.getChildren().clear();

            Button attackBtn = createPokeButton("ATACAR", "#ff8c94");
            Button backBtn   = createPokeButton("VOLTAR", "#b3b3b3");
            buttonGrid.add(attackBtn, 0, 0);
            buttonGrid.add(backBtn,   1, 0);

            backBtn.setOnAction(ev -> voltarMenu.run());

            attackBtn.setOnAction(atk -> {
                buttonGrid.setDisable(true);
                playAttackAnimation(pImg, eImg, () -> {
                    int dano = playerChar.attack(monstro);
                    promptText.setText(playerChar.getName() + " causou " + dano + " de dano!");
                    updateHpBar(opponentStatus, monstro.getLife(), monstro.getMaxLife(), false);

                    if (monstro.getLife() <= 0) {
                        // ── VITÓRIA: XP + MOEDAS ──────────────────────────────
                        playerChar.earnXp(30);
                        int moedas = monstro.getDropCoin(); // Usa o dropCoin do monstro!
                        playerChar.addCoin(moedas);

                        // Atualiza HUD imediatamente
                        if (hudManager != null) hudManager.atualizar(playerChar);

                        promptText.setText(monstro.getName() + " derrotado!\n" +
                                           "+" + moedas + " moedas  +30 XP");

                        PauseTransition win = new PauseTransition(Duration.millis(1800));
                        win.setOnFinished(ev ->
                            exitBattle(stage, mapScene, app, root, originalLayout, hudManager, playerChar));
                        win.play();
                    } else {
                        PauseTransition p = new PauseTransition(Duration.millis(1200));
                        p.setOnFinished(ev ->
                            enemyTurn(playerChar, monstro, playerStatus, pImg, root, app, buttonGrid, hudManager));
                        p.play();
                    }
                });
            });
        });

        // ── MOCHILA — usa pocoes ──────────────────────────────────────────────
        bagBtn.setOnAction(e -> {
            buttonGrid.getChildren().clear();

            // Filtra apenas pocoes do inventario
            List<Potion> pocoes = playerChar.getInventory().getItems().stream()
                .filter(it -> it instanceof Potion)
                .map(it -> (Potion) it)
                .toList();

            if (pocoes.isEmpty()) {
                promptText.setText("Sem pocoes no inventario!");
                Button back = createPokeButton("VOLTAR", "#b3b3b3");
                buttonGrid.add(back, 0, 0);
                back.setOnAction(ev -> voltarMenu.run());
                return;
            }

            promptText.setText("Escolha uma pocao:");

            int col = 0, row = 0;
            // Mostra ate 3 pocoes por vez
            for (int i = 0; i < Math.min(pocoes.size(), 3); i++) {
                Potion p = pocoes.get(i);
                Button btn = createPokeButton(p.getName() + "\n+" + p.getHealedLife() + " HP", "#85d685");
                btn.setPrefSize(160, 60);

                btn.setOnAction(useEvent -> {
                    // Aplica a cura e remove do inventario
                    playerChar.heal(p.getHealedLife());
                    playerChar.getInventory().removeItem(p);

                    // Atualiza barra de HP na batalha
                    updateHpBar(playerStatus, playerChar.getLife(), playerChar.getMaxLife(), true);
                    promptText.setText("Usou " + p.getName() + "!\n+" + p.getHealedLife() + " HP recuperado.");

                    // Atualiza HUD fora da batalha tambem
                    if (hudManager != null) hudManager.atualizar(playerChar);

                    buttonGrid.setDisable(true);
                    PauseTransition pausa = new PauseTransition(Duration.millis(1200));
                    pausa.setOnFinished(ev ->
                        enemyTurn(playerChar, monstro, playerStatus, pImg, root, app, buttonGrid, hudManager));
                    pausa.play();
                });

                buttonGrid.add(btn, col, row);
                col++;
                if (col > 1) { col = 0; row++; }
            }

            // Botao VOLTAR apos os itens
            Button back = createPokeButton("VOLTAR", "#b3b3b3");
            buttonGrid.add(back, col, row);
            back.setOnAction(ev -> voltarMenu.run());
        });
    }

    // =========================================================================
    // TURNO DO INIMIGO
    // =========================================================================

    private static void enemyTurn(Character playerChar, Monsters monstro, VBox playerStatus,
                                   ImageView pImg, StackPane root, App app,
                                   GridPane buttonGrid, HudManager hudManager) {
        // Usa o dano real do monstro menos a resistencia do jogador (inclui armadura)
        int dano = Math.max(0, monstro.getDamage() - playerChar.getResistance());
        playerChar.setLife(playerChar.getLife() - dano);

        updateHpBar(playerStatus, playerChar.getLife(), playerChar.getMaxLife(), true);
        playShakeAnimation(pImg);

        if (hudManager != null) hudManager.atualizar(playerChar);

        if (playerChar.getLife() <= 0) {
            triggerGameOver(root, app);
        } else {
            buttonGrid.setDisable(false);
        }
    }

    // =========================================================================
    // GAME OVER
    // =========================================================================

    private static void triggerGameOver(StackPane rootNode, App app) {
        StackPane go = new StackPane();
        go.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        go.setOpacity(0);

        Label txt = new Label("Game Over");
        txt.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 100px; -fx-font-weight: bold; -fx-font-family: 'Impact', sans-serif; -fx-effect: dropshadow(gaussian, rgba(255,0,0,0.5), 20, 0.5, 0, 0);");

        Button menuBtn = new Button("Voltar para o Menu");
        String bs = "-fx-background-color: #222; -fx-text-fill: white; -fx-font-size: 20px; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 10 20;";
        String bh = "-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-size: 20px; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 10 20;";
        menuBtn.setStyle(bs);
        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(bh));
        menuBtn.setOnMouseExited(e  -> menuBtn.setStyle(bs));
        menuBtn.setOnAction(e -> { inBattle = false; app.showMainMenu(); });

        StackPane.setAlignment(txt, Pos.CENTER);
        StackPane.setAlignment(menuBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(menuBtn, new Insets(0, 50, 50, 0));
        go.getChildren().addAll(txt, menuBtn);
        rootNode.getChildren().add(go);

        FadeTransition f = new FadeTransition(Duration.millis(1500), go);
        f.setToValue(1); f.play();
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    private static VBox createPlayerStatusBox(String name, int level, int hp, int maxHp, int xp, int maxXp) {
        VBox box = buildBaseStatusBox(name, "Lv " + level, hp, maxHp, 130);
        double dHp = Math.max(0, hp);
        Label hpN = new Label((int) dHp + " / " + maxHp);
        hpN.setId("hpText"); hpN.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox row = new HBox(hpN); row.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(row);

        HBox xpRow = new HBox(10); xpRow.setAlignment(Pos.CENTER_RIGHT);
        Label xpLbl = new Label("XP");
        xpLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4da6ff; -fx-background-color: #333; -fx-padding: 0 5; -fx-background-radius: 3;");
        ProgressBar xpBar = new ProgressBar((double) xp / Math.max(1, maxXp));
        xpBar.setId("xpBar"); xpBar.setPrefWidth(200); xpBar.setPrefHeight(10);
        xpBar.setStyle("-fx-accent: #4da6ff; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        xpRow.getChildren().addAll(xpLbl, xpBar);
        box.getChildren().add(xpRow);
        return box;
    }

    private static VBox createMonsterStatusBox(String name, int hp, int maxHp) {
        return buildBaseStatusBox(name, "", hp, maxHp, 80);
    }

    private static VBox buildBaseStatusBox(String name, String lvl, int hp, int maxHp, int h) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 4; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 20;");
        box.setMaxWidth(300); box.setMaxHeight(h);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label nLbl = new Label(name); nLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lLbl = new Label(lvl); lLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        top.getChildren().addAll(nLbl, sp, lLbl);

        HBox hpRow = new HBox(10); hpRow.setAlignment(Pos.CENTER_RIGHT);
        Label hpLbl = new Label("HP");
        hpLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e6b800; -fx-background-color: #333; -fx-padding: 0 5; -fx-background-radius: 3;");
        double dHp = Math.max(0, hp);
        ProgressBar hpBar = new ProgressBar(dHp / maxHp);
        hpBar.setId("hpBar"); hpBar.setPrefWidth(200); hpBar.setPrefHeight(15);
        String c = (dHp / maxHp) <= 0.2 ? "#ff3333" : "#48e85c";
        hpBar.setStyle("-fx-accent: " + c + "; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        hpRow.getChildren().addAll(hpLbl, hpBar);
        box.getChildren().addAll(top, hpRow);
        return box;
    }

    private static Button createPokeButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(160, 50);
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-border-color: #333; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);";
        String hover = base.replace("-fx-background-color: " + color, "-fx-background-color: derive(" + color + ", -15%)");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private static void updateHpBar(VBox status, int hp, int maxHp, boolean isPlayer) {
        ProgressBar bar = (ProgressBar) status.lookup("#hpBar");
        double d = Math.max(0, hp);
        if (bar != null) {
            bar.setProgress(d / maxHp);
            String c = (d / maxHp) <= 0.2 ? "#ff3333" : "#48e85c";
            bar.setStyle("-fx-accent: " + c + "; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        }
        if (isPlayer) {
            Label t = (Label) status.lookup("#hpText");
            if (t != null) t.setText((int) d + " / " + maxHp);
        }
    }

    // =========================================================================
    // ANIMACOES
    // =========================================================================

    private static void playAttackAnimation(ImageView attacker, ImageView victim, Runnable onFinished) {
        TranslateTransition move = new TranslateTransition(Duration.millis(200), attacker);
        move.setByX(100); move.setByY(-50);
        move.setAutoReverse(true); move.setCycleCount(2);
        move.setOnFinished(e -> onFinished.run());
        move.play();
    }

    private static void playShakeAnimation(ImageView target) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), target);
        shake.setByX(10); shake.setCycleCount(6); shake.setAutoReverse(true);
        shake.play();
    }

    // =========================================================================
    // SAIR DA BATALHA
    // =========================================================================

    private static void exitBattle(Stage stage, Scene mapScene, App app, Pane rootLayout,
                                    StackPane originalLayout, HudManager hudManager, Character playerChar) {
        FadeTransition fo = new FadeTransition(Duration.millis(500), rootLayout);
        fo.setToValue(0);
        fo.setOnFinished(e -> {
            inBattle = false;
            mapScene.setRoot(originalLayout);
            originalLayout.setOpacity(1);
            if (!stage.isFullScreen()) stage.setFullScreen(true);
            // Atualiza HUD completo ao retornar ao mapa (moedas, HP, XP)
            if (hudManager != null) hudManager.atualizar(playerChar);
            app.resumeTimers();
        });
        fo.play();
    }
}
