package com.RPG.TheLastRoar;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * App.java — Classe principal do jogo "The Last Roar".
 *
 * ALTERACOES NESTA VERSAO:
 *
 * 1. NPC VENDEDOR (ShopNPC):
 *    - ShopNPC.criarSprite() adicionado ao gameRoot, visivel so no mapa 0
 *    - verificarColisaoNPC() roda a cada frame do playerMovement
 *    - Ao colidir, para os timers e chama ShopNPC.abrirLoja()
 *    - Ao fechar, retoma os timers normalmente
 *
 * 2. TECLA H — USAR POCAO NO MAPA:
 *    - Pressionar H usa a primeira Potion do inventario
 *    - Exibe toast flutuante "+XX HP" por 1.5s
 *    - Nao funciona com HP cheio
 *
 * 3. hudManager passado para Battle.startBattle():
 *    - Battle precisa do HudManager para atualizar moedas e HP apos batalha
 *    - Assinatura de startBattle() mudou: novo parametro HudManager ao final
 */
public class App extends javafx.application.Application {

    private double screenW;
    private double screenH;

    private Pane      gameRoot;
    private ImageView playerView;
    private ImageView mapView;
    private StackPane mainLayout;
    private Scene     cenaMestra;

    private Character player;

    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    private int    direction     = 0;
    private int    frame         = 0;
    private long   lastFrameTime = 0;
    private final long   frameDelay             = 200_000_000L;
    private final double speed                 = 4;
    private final int    spriteWidth           = 128;
    private final int    spriteHeight          = 128;
    private final double personagemTamanhoTela = 80;

    private int  enemyFrame         = 0;
    private long lastEnemyFrameTime = 0;

    private static boolean up, down, left, right;

    private boolean isPaused        = false;
    private boolean lojaAberta      = false;
    private boolean inventarioAberto = false; // Impede abrir inventario duas vezes

    private final String[] LISTA_MAPAS = {"mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"};
    private int indiceMapa = 0;
    private boolean[][] inimigosDerrotados;

    private HudManager   hudManager;
    private PauseMenu    pauseMenu;
    private EnemyManager enemyManager;

    // Sprite do NPC no mapa
    private ImageView npcView;

    private int monstroEmBatalhaIndex = -1;

    // =========================================================================

    public static void resetMovement() {
        up = down = left = right = false;
    }

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @Override
    public void start(Stage stage) {
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, cenaMestra, () -> iniciarJogo(stage, null)),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Nao foi possivel carregar o icone: " + e.getMessage());
        }

        cenaMestra = new Scene(menuLayout, 800, 600);
        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();
    }

    // =========================================================================
    // INICIALIZACAO DO JOGO
    // =========================================================================

    private void iniciarJogo(Stage stage, String saveFile) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            mapView = new ImageView(
                new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));

            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH);

            player = new Character("Hero", 100, 2,
                new Sword("Madeira", 3, 6, "Comum", 4),
                new Image(getClass().getResource("/images/sprite_personagem.png").toExternalForm()),
                new Image(getClass().getResource("/images/personagem_battle.png").toExternalForm())
            );

            // Espada inicial vai pro inventario para aparecer na tela de inventario
            player.getInventory().addItem(player.getSword());

            playerView = new ImageView(player.getSprite());
            playerView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));
            playerView.setFitWidth(personagemTamanhoTela);
            playerView.setFitHeight(personagemTamanhoTela);
            playerView.setX((screenW / 2) - (personagemTamanhoTela / 2));
            playerView.setY(screenH - personagemTamanhoTela - 20);
            gameRoot.getChildren().add(playerView);

            // ── NPC VENDEDOR ─── sempre criado, visibilidade controlada depois ──
            npcView = ShopNPC.criarSprite();
            gameRoot.getChildren().add(npcView);

            inimigosDerrotados = new boolean[LISTA_MAPAS.length][10];
            enemyManager = new EnemyManager(gameRoot, screenW, screenH, inimigosDerrotados);

            hudManager = new HudManager(stage);

            if (saveFile != null) {
                carregarDeJson(saveFile, true);
            } else {
                indiceMapa = 0;
                enemyManager.configurarParaMapa(indiceMapa);
                hudManager.atualizar(player);
            }

            // Visibilidade inicial do NPC (so aparece no mapa 0)
            atualizarVisibilidadeNPC();

            System.out.println("ALERTA: O jogo esta recriando os monstros agora!");

            pauseMenu = new PauseMenu(
                this::togglePause,
                () -> salvarSlot("save1.json"),
                () -> salvarSlot("save2.json"),
                () -> salvarSlot("save3.json"),
                () -> carregarDeJson("save1.json", false),
                () -> carregarDeJson("save2.json", false),
                () -> carregarDeJson("save3.json", false),
                () -> { togglePause(); showMainMenu(); }
            );

            mainLayout = new StackPane(
                mapView, gameRoot,
                hudManager.getLayout(),
                pauseMenu.getLayout()
            );
            mainLayout.setStyle("-fx-background-color: black;");
            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            // ── CONTROLES DE TECLADO ──────────────────────────────────────────
            cenaMestra.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) togglePause();

                if (!isPaused && !lojaAberta) {
                    if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP)    up    = true;
                    if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN)  down  = true;
                    if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT)  left  = true;
                    if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = true;

                    // Tecla H: usa a primeira pocao do inventario
                    if (e.getCode() == KeyCode.H) usarPocaoNoMapa();
                }
            });
            cenaMestra.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP)    up    = false;
                if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN)  down  = false;
                if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT)  left  = false;
                if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = false;
            });

            cenaMestra.setRoot(mainLayout);
            iniciarTimers(stage, cenaMestra);

            Platform.runLater(() -> { if (!stage.isFullScreen()) stage.setFullScreen(true); });

            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    // =========================================================================
    // USAR POCAO NO MAPA (tecla H)
    // =========================================================================

    /**
     * Usa a primeira pocao do inventario.
     * Exibe um toast "+XX HP" na tela. Nao funciona com HP cheio.
     */
    private void usarPocaoNoMapa() {
        if (player.getLife() >= player.getMaxLife()) return; // HP ja cheio

        // Procura a primeira pocao
        Potion pocaoUsada = null;
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Potion p) { pocaoUsada = p; break; }
        }
        if (pocaoUsada == null) return; // Sem pocoes

        // Calcula a cura real (nao passa do maximo)
        int curaReal = Math.min(pocaoUsada.getHealedLife(), player.getMaxLife() - player.getLife());
        player.heal(pocaoUsada.getHealedLife());
        player.getInventory().removeItem(pocaoUsada);
        hudManager.atualizar(player);

        exibirToast("+" + curaReal + " HP", Color.web("#00CC66"));
    }

    /**
     * Exibe um label flutuante temporario (toast) no centro da tela.
     */
    private void exibirToast(String mensagem, Color cor) {
        Label toast = new Label(mensagem);
        toast.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        toast.setTextFill(cor);
        toast.setStyle(
            "-fx-background-color: rgba(0,0,0,0.65);" +
            "-fx-padding: 12 28;" +
            "-fx-background-radius: 12;"
        );
        toast.setMouseTransparent(true);

        StackPane.setAlignment(toast, Pos.CENTER);
        mainLayout.getChildren().add(toast);
        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            PauseTransition espera = new PauseTransition(Duration.millis(1000));
            espera.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(eff -> mainLayout.getChildren().remove(toast));
                fadeOut.play();
            });
            espera.play();
        });
        fadeIn.play();
    }

    // =========================================================================
    // NPC
    // =========================================================================

    /** Mostra o NPC somente no mapa 0. */
    private void atualizarVisibilidadeNPC() {
        if (npcView != null) npcView.setVisible(indiceMapa == 0);
    }

    /**
     * Verifica colisao com o NPC a cada frame e abre a loja se necessario.
     */
    private void verificarColisaoNPC() {
        if (indiceMapa != 0 || lojaAberta || npcView == null) return;

        if (ShopNPC.verificarColisao(playerView.getX(), playerView.getY())) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            lojaAberta = true;

            ShopNPC.abrirLoja(mainLayout, player, hudManager, () -> {
                lojaAberta = false;
                lastFrameTime = System.nanoTime();
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                Platform.runLater(() -> mainLayout.requestFocus());
            });
        }
    }

    // =========================================================================
    // MENU PRINCIPAL
    // =========================================================================

    public void showMainMenu() {
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI        != null) enemyAI.stop();

        isPaused   = false;
        lojaAberta = false;
        resetMovement();

        Stage stage = (Stage) cenaMestra.getWindow();
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, cenaMestra, () -> iniciarJogo(stage, null)),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );
        cenaMestra.setRoot(menuLayout);
        if (!stage.isFullScreen()) stage.setFullScreen(true);
    }

    // =========================================================================
    // PAUSA
    // =========================================================================

    private void togglePause() {
        if (lojaAberta) return;
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            pauseMenu.atualizarBotoesLoad(
                SaveManager.existe("save1.json"),
                SaveManager.existe("save2.json"),
                SaveManager.existe("save3.json")
            );
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
        }
    }

    // =========================================================================
    // SAVE / LOAD
    // =========================================================================

    private void salvarSlot(String arquivo) {
        SaveManager.salvar(arquivo, indiceMapa,
            playerView.getX(), playerView.getY(), player, inimigosDerrotados);
        togglePause();
    }

    private void carregarDeJson(String arquivo, boolean isInitialLoad) {
        SaveManager.SaveData data = SaveManager.carregar(arquivo, LISTA_MAPAS.length);
        if (data == null) return;

        indiceMapa = data.mapa;
        for (int i = 0; i < data.inimigosDerrotados.length; i++) {
            System.arraycopy(data.inimigosDerrotados[i], 0,
                             inimigosDerrotados[i], 0, data.inimigosDerrotados[i].length);
        }
        mapView.setImage(new Image(
            getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
        enemyManager.configurarParaMapa(indiceMapa);
        atualizarVisibilidadeNPC();

        playerView.setX(data.posX);
        playerView.setY(data.posY);
        player.setLife(data.vida);
        player.setNivel(data.level);
        player.setCoin(data.ouro);

        if (!isInitialLoad) togglePause();
        if (hudManager != null) hudManager.atualizar(player);
    }

    // =========================================================================
    // TROCA DE CENARIO
    // =========================================================================

    private void trocarCenario(boolean avancar) {
        playerMovement.stop();
        enemyAI.stop();

        FadeTransition ft = new FadeTransition(Duration.millis(300), mainLayout);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            if (avancar && indiceMapa < LISTA_MAPAS.length - 1) {
                indiceMapa++;
                playerView.setY(screenH - spriteHeight - 20);
                playerView.setX((screenW / 2) - (spriteWidth / 2));
            } else if (!avancar && indiceMapa > 0) {
                indiceMapa--;
                playerView.setY(20);
                playerView.setX((screenW / 2) - (spriteWidth / 2));
            } else {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                return;
            }
            mapView.setImage(new Image(
                getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            enemyManager.configurarParaMapa(indiceMapa);
            atualizarVisibilidadeNPC(); // Atualiza visibilidade do NPC ao trocar de mapa

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
            });
            fadeIn.play();
        });
        ft.play();
    }

    // =========================================================================
    // POS-BATALHA
    // =========================================================================

    public void resumeTimers() {
        if (monstroEmBatalhaIndex != -1) {
            enemyManager.removerInimigo(monstroEmBatalhaIndex);
            monstroEmBatalhaIndex = -1;
        }
        resetMovement();
        lastFrameTime = System.nanoTime();
        mainLayout.setOpacity(1.0);
        Platform.runLater(() -> {
            mainLayout.requestFocus();
            System.out.println("Teclado reativado.");
        });
        playerMovement.start();
        if (!enemyManager.isEmpty()) enemyAI.start();
    }

    // =========================================================================
    // TIMERS
    // =========================================================================

    private void iniciarTimers(Stage stage, Scene scene) {
        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (up) {
                    if (playerView.getY() <= 5) {
                        if (indiceMapa < LISTA_MAPAS.length - 1) trocarCenario(true);
                        else playerView.setY(5);
                    } else { playerView.setY(playerView.getY() - speed); }
                    setDirection(playerView, 3); animate(playerView);
                } else if (down) {
                    if (playerView.getY() >= screenH - spriteHeight - 5) {
                        if (indiceMapa > 0) trocarCenario(false);
                        else playerView.setY(screenH - spriteHeight - 5);
                    } else { playerView.setY(playerView.getY() + speed); }
                    setDirection(playerView, 0); animate(playerView);
                } else if (left) {
                    if (playerView.getX() > 5) playerView.setX(playerView.getX() - speed);
                    setDirection(playerView, 1); animate(playerView);
                } else if (right) {
                    if (playerView.getX() < screenW - spriteWidth - 5)
                        playerView.setX(playerView.getX() + speed);
                    setDirection(playerView, 2); animate(playerView);
                }

                // Checa colisao com NPC a cada frame do movimento
                verificarColisaoNPC();
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastEnemyFrameTime > frameDelay) {
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }
                int colisao = enemyManager.atualizar(playerView.getX(), playerView.getY(), enemyFrame);
                if (colisao != -1) {
                    playerMovement.stop();
                    enemyAI.stop();

                    double dx = playerView.getX() - enemyManager.getView(colisao).getX();
                    playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40));
                    monstroEmBatalhaIndex = colisao;

                    Monsters monstro = enemyManager.getMonstro(colisao);
                    ImageView ev     = enemyManager.getView(colisao);

                    FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                    ft.setFromValue(1); ft.setToValue(0);
                    // NOVO: passa hudManager para Battle poder atualizar HUD de moedas e HP
                    ft.setOnFinished(e ->
                        Battle.startBattle(stage, cenaMestra, player, monstro,
                                           playerView, ev, App.this, mainLayout, hudManager)
                    );
                    ft.play();
                }
            }
        };
        enemyAI.start();
    }

    // =========================================================================
    // ANIMACAO DO SPRITE
    // =========================================================================

    private void animate(ImageView view) {
        long now = System.nanoTime();
        if (now - lastFrameTime > frameDelay) {
            frame = (frame + 1) % 4;
            view.setViewport(new Rectangle2D(
                frame * spriteWidth, direction * spriteHeight, spriteWidth, spriteHeight));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView view, int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            frame = 0;
            view.setViewport(new Rectangle2D(0, direction * spriteHeight, spriteWidth, spriteHeight));
        }
    }

    public static void main(String[] args) { launch(); }
}
