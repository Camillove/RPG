package com.RPG.TheLastRoar;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ============================================================
 * App.java — Controlador Principal do Jogo "The Last Roar"
 * ============================================================
 */
public class App extends javafx.application.Application {

    private double screenW;
    private double screenH;

    private Pane      gameRoot;
    private ImageView playerView;
    private ImageView mapView;
    private StackPane mainLayout;
    private Scene     cenaMestra;
    private Button    btnInventario; 

    private Character player;

    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    private int    direction     = 0;   
    private int    frame         = 0;
    private long   lastFrameTime = 0;

    private static final long   FRAME_DELAY        = 200_000_000L; 
    private static final double SPEED              = 4;
    private static final int    SPRITE_W           = 128;
    private static final int    SPRITE_H           = 128;
    private static final double PLAYER_DISPLAY_SIZE = 80;

    private int  enemyFrame         = 0;
    private long lastEnemyFrameTime = 0;

    private static boolean up, down, left, right;

    private boolean isPaused         = false;
    private boolean lojaAberta       = false;
    private boolean inventarioAberto = false;
    private boolean isTransitioning  = false; 
    private boolean jogoEmAndamento  = false; 

    private final String[] LISTA_MAPAS = {
        "mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"
    };
    private int  indiceMapa = 0;
    private boolean[][] inimigosDerrotados;

    private HudManager   hudManager;
    private PauseMenu    pauseMenu;
    private EnemyManager enemyManager;

    private ImageView npcView;

    private int monstroEmBatalhaIndex = -1;

    // ── Filtros Globais de Teclado ─────────────────────────────────────────
    private EventHandler<KeyEvent> keyPressHandler;
    private EventHandler<KeyEvent> keyReleaseHandler;

    public static void resetMovement() {
        up = down = left = right = false;
    }

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
            System.out.println("[App] Ícone não encontrado: " + e.getMessage());
        }

        cenaMestra = new Scene(menuLayout, 800, 600);
        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();

        // Tira o foco do botão inicial para evitar que Espaço/Enter o ative sem querer
        Platform.runLater(() -> menuLayout.requestFocus());
    }

    private void iniciarJogo(Stage stage, String saveFile) {
        if (jogoEmAndamento) return;
        jogoEmAndamento = true;

        if (playerMovement != null) playerMovement.stop();
        if (enemyAI != null) enemyAI.stop();

        try {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            mapView = new ImageView(new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));

            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH);

            player = new Character("Hero", 20, 0,
                new Sword("Madeira", 3, 6, "Comum", 4),
                new Image(getClass().getResource("/images/sprite_personagem.png").toExternalForm()),
                new Image(getClass().getResource("/images/personagem_battle.png").toExternalForm())
            );
            player.getInventory().addItem(player.getSword());

            playerView = new ImageView(player.getSprite());
            playerView.setViewport(new Rectangle2D(0, 0, SPRITE_W, SPRITE_H));
            playerView.setFitWidth(PLAYER_DISPLAY_SIZE);
            playerView.setFitHeight(PLAYER_DISPLAY_SIZE);
            playerView.setX((screenW / 2) - (PLAYER_DISPLAY_SIZE / 2));
            playerView.setY(screenH - PLAYER_DISPLAY_SIZE - 20);
            gameRoot.getChildren().add(playerView);

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

            atualizarVisibilidadeNPC();

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

            criarBotaoInventario();

            mainLayout = new StackPane(
                mapView,
                gameRoot,
                hudManager.getLayout(),
                btnInventario,
                pauseMenu.getLayout()
            );
            mainLayout.setStyle("-fx-background-color: black;");
            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            configurarControlesDeTeclado();
            cenaMestra.setRoot(mainLayout);
            iniciarTimers(stage, cenaMestra);

            Platform.runLater(() -> { 
                if (!stage.isFullScreen()) stage.setFullScreen(true); 
                mainLayout.requestFocus(); // Força o ecrã a receber os comandos do teclado
            });

            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            jogoEmAndamento = false; 
            System.err.println("[App] Erro crítico ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    private void criarBotaoInventario() {
        btnInventario = new Button("🎒 Inventário (I)");
        btnInventario.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        btnInventario.setFocusTraversable(false); // CRUCIAL: Impede que o botão roube o uso das setinhas!
        
        String estNormal = 
            "-fx-background-color: rgba(20, 20, 20, 0.7);" +
            "-fx-text-fill: #E8DFC0;" +
            "-fx-border-color: #B8960C;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;";
            
        String estHover = 
            "-fx-background-color: rgba(60, 50, 20, 0.9);" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: #FFD700;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;";

        btnInventario.setStyle(estNormal);
        btnInventario.setOnMouseEntered(e -> btnInventario.setStyle(estHover));
        btnInventario.setOnMouseExited(e -> btnInventario.setStyle(estNormal));

        btnInventario.setOnAction(e -> {
            Platform.runLater(() -> mainLayout.requestFocus());
            abrirInventario();
        });

        StackPane.setAlignment(btnInventario, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnInventario, new Insets(0, 30, 30, 0));
    }

    private void configurarControlesDeTeclado() {
        if (keyPressHandler != null) cenaMestra.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
        if (keyReleaseHandler != null) cenaMestra.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);

        keyPressHandler = e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
                e.consume();
            }

            if (!isPaused && !lojaAberta && !inventarioAberto && !isTransitioning && jogoEmAndamento) {
                switch (e.getCode()) {
                    case W, UP    -> { up    = true; e.consume(); } 
                    case S, DOWN  -> { down  = true; e.consume(); }
                    case A, LEFT  -> { left  = true; e.consume(); }
                    case D, RIGHT -> { right = true; e.consume(); }
                    case H        -> { usarPocaoNoMapa(); e.consume(); }
                    case I        -> { abrirInventario(); e.consume(); }
                    default       -> {}
                }
            }
        };

        keyReleaseHandler = e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = false;
                case S, DOWN  -> down  = false;
                case A, LEFT  -> left  = false;
                case D, RIGHT -> right = false;
                default       -> {}
            }
        };

        cenaMestra.addEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
        cenaMestra.addEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
    }

    private void usarPocaoNoMapa() {
        if (player.getLife() >= player.getMaxLife()) return;

        Potion pocaoUsada = null;
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Potion p) { pocaoUsada = p; break; }
        }
        if (pocaoUsada == null) return; 

        int curaReal = Math.min(pocaoUsada.getHealedLife(), player.getMaxLife() - player.getLife());
        player.heal(pocaoUsada.getHealedLife());
        player.getInventory().removeItem(pocaoUsada);
        hudManager.atualizar(player);

        exibirToast("+" + curaReal + " HP", Color.web("#00CC66"));
    }

    private void abrirInventario() {
        if (inventarioAberto || isPaused || lojaAberta || isTransitioning) return;
        
        inventarioAberto = true;
        btnInventario.setVisible(false); 
        
        playerMovement.stop();
        enemyAI.stop();
        resetMovement();

        InventoryScreen.open(mainLayout, player, () -> {
            inventarioAberto = false;
            btnInventario.setVisible(true);
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
            Platform.runLater(() -> mainLayout.requestFocus());
        });
    }

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

    private void atualizarVisibilidadeNPC() {
        if (npcView != null) npcView.setVisible(indiceMapa == 0);
    }

    private void verificarColisaoNPC() {
        if (indiceMapa != 0 || lojaAberta || npcView == null || isTransitioning) return;

        if (ShopNPC.verificarColisao(playerView.getX(), playerView.getY())) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            lojaAberta = true;
            btnInventario.setVisible(false); 

            ShopNPC.abrirLoja(mainLayout, player, hudManager, () -> {
                lojaAberta = false; 
                btnInventario.setVisible(true); 
                lastFrameTime = System.nanoTime();
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                Platform.runLater(() -> mainLayout.requestFocus());
            });
        }
    }

    public void showMainMenu() {
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI        != null) enemyAI.stop();

        jogoEmAndamento  = false; 
        isPaused         = false;
        lojaAberta       = false;
        inventarioAberto = false;
        isTransitioning  = false; 
        resetMovement();

        Battle.resetInBattle();

        Stage stage = (Stage) cenaMestra.getWindow();
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, cenaMestra, () -> iniciarJogo(stage, null)),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );
        cenaMestra.setRoot(menuLayout);
        if (!stage.isFullScreen()) stage.setFullScreen(true);
        
        Platform.runLater(() -> menuLayout.requestFocus()); 
    }

    private void togglePause() {
        if (lojaAberta || inventarioAberto || isTransitioning) return;
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            btnInventario.setVisible(false); 
            pauseMenu.atualizarBotoesLoad(
                SaveManager.existe("save1.json"),
                SaveManager.existe("save2.json"),
                SaveManager.existe("save3.json")
            );
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            btnInventario.setVisible(true); 
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
            Platform.runLater(() -> mainLayout.requestFocus());
        }
    }

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
                             inimigosDerrotados[i], 0,
                             data.inimigosDerrotados[i].length);
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
    // TROCA DE CENÁRIO (ATUALIZADO PARA TELEPORTAR O JOGADOR CORRETAMENTE)
    // =========================================================================

    private void trocarCenario(String direcao) {
        if (isTransitioning) return;

        int novoIndice = indiceMapa;

        // Avança o mapa se for pra Direita ou pra Cima. Volta se for pra Esquerda ou Baixo.
        // Você pode mudar essa lógica dependendo de como seus mapas se conectam!
        if (direcao.equals("CIMA") || direcao.equals("DIREITA")) {
            novoIndice++;
        } else if (direcao.equals("BAIXO") || direcao.equals("ESQUERDA")) {
            novoIndice--;
        }

        // Se bater no limite dos mapas, não faz nada
        if (novoIndice >= LISTA_MAPAS.length || novoIndice < 0) return;

        isTransitioning = true; 

        playerMovement.stop();
        enemyAI.stop();
        resetMovement(); 

        final int indiceFinal = novoIndice;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainLayout);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            
            indiceMapa = indiceFinal;

            // TELEPORTA O PERSONAGEM PARA O LADO OPOSTO
            if (direcao.equals("CIMA")) {
                playerView.setY(screenH - PLAYER_DISPLAY_SIZE - 20); // Aparece lá embaixo
            } 
            else if (direcao.equals("BAIXO")) {
                playerView.setY(20); // Aparece lá em cima
            } 
            else if (direcao.equals("ESQUERDA")) {
                playerView.setX(screenW - PLAYER_DISPLAY_SIZE - 20); // Aparece na extrema direita
            } 
            else if (direcao.equals("DIREITA")) {
                playerView.setX(20); // Aparece na extrema esquerda
            }

            mapView.setImage(new Image(
                getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            enemyManager.configurarParaMapa(indiceMapa);
            atualizarVisibilidadeNPC();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                lastFrameTime = System.nanoTime(); 
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                
                isTransitioning = false; 
            });
            fadeIn.play();
        });
        fadeOut.play();
    }

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
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
        });
    }

    private void iniciarTimers(Stage stage, Scene scene) {

        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (up) {
                    if (playerView.getY() <= 5) {
                        trocarCenario("CIMA");
                    } else {
                        playerView.setY(playerView.getY() - SPEED);
                    }
                    setDirection(playerView, 3);
                    animate(playerView, now);

                } else if (down) {
                    if (playerView.getY() >= screenH - PLAYER_DISPLAY_SIZE - 5) {
                        trocarCenario("BAIXO");
                    } else {
                        playerView.setY(playerView.getY() + SPEED);
                    }
                    setDirection(playerView, 0);
                    animate(playerView, now);

                } else if (left) {
                    if (playerView.getX() <= 5) {
                        trocarCenario("ESQUERDA");
                    } else {
                        playerView.setX(playerView.getX() - SPEED);
                    }
                    setDirection(playerView, 1);
                    animate(playerView, now);

                } else if (right) {
                    if (playerView.getX() >= screenW - PLAYER_DISPLAY_SIZE - 5) {
                        trocarCenario("DIREITA");
                    } else {
                        playerView.setX(playerView.getX() + SPEED);
                    }
                    setDirection(playerView, 2);
                    animate(playerView, now);
                }

                verificarColisaoNPC();
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastEnemyFrameTime > FRAME_DELAY) {
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                int colisao = enemyManager.atualizar(
                    playerView.getX(), playerView.getY(), enemyFrame);

                if (colisao != -1) {
                    playerMovement.stop();
                    enemyAI.stop();
                    
                    btnInventario.setVisible(false);

                    double dx = playerView.getX() - enemyManager.getView(colisao).getX();
                    playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40));
                    monstroEmBatalhaIndex = colisao;

                    Monsters  monstro = enemyManager.getMonstro(colisao);
                    ImageView ev      = enemyManager.getView(colisao);

                    FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> {
                        Battle.startBattle(stage, cenaMestra, player, monstro,
                                           playerView, ev, App.this, mainLayout, hudManager);
                        btnInventario.setVisible(true);
                    });
                    ft.play();
                }
            }
        };
        enemyAI.start();
    }

    private void animate(ImageView view, long now) {
        if (now - lastFrameTime > FRAME_DELAY) {
            frame = (frame + 1) % 4;
            view.setViewport(new Rectangle2D(
                frame * SPRITE_W,
                direction * SPRITE_H,
                SPRITE_W, SPRITE_H
            ));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView view, int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            frame = 0;
            view.setViewport(new Rectangle2D(
                0, direction * SPRITE_H, SPRITE_W, SPRITE_H));
        }
    }

    public static void main(String[] args) { launch(); }
}