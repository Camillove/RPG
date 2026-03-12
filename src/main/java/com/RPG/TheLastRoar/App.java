package com.RPG.TheLastRoar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends javafx.application.Application {

    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;
    private Pane gameRoot;
    private ImageView playerView;
    private ImageView mapView;
    private StackPane mainLayout;
    private Character player;
    
    private Scene cenaMestra;

    private final String[] LISTA_MAPAS = {"mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"};
    private int indiceMapa = 0; 
    
    private boolean[][] inimigosDerrotados = new boolean[LISTA_MAPAS.length][10]; 

    private List<Monsters> monstrosAtuais = new ArrayList<>();
    private List<ImageView> inimigosViewsAtuais = new ArrayList<>();
    private int monstroEmBatalhaIndex = -1;

    private long lastFrameTime = 0;
    private int direction = 0;
    private final long frameDelay = 200_000_000;
    private double speed = 4;
    private int frame = 0;
    private final int spriteWidth = 64;
    private final int spriteHeight = 64;

    private int enemyFrame = 0;
    private long lastEnemyFrameTime = 0;
    private final int enemySpriteWidth = 128;
    private final int enemySpriteHeight = 128;

    private static boolean up, down, left, right;

    private double screenW;
    private double screenH;

    private boolean isPaused = false;
    private VBox pauseMenu;
    
    // Botões do menu de pausa para podermos desabilitá-los dinamicamente
    private Button btnLoadSlot1, btnLoadSlot2, btnLoadSlot3, btnLoadMenu;

    public static void resetMovement(){ up = down = left = right = false; }

    @Override
    public void start(Stage stage) {
        StackPane menuLayout = StartScreen.createLayout(
            () -> iniciarJogo(stage, null), 
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()), 
            (slotName) -> iniciarJogo(stage, slotName) 
        );

        cenaMestra = new Scene(menuLayout, 800, 600);

        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); 
        stage.setFullScreen(true);
        stage.show();
    }

    private void iniciarJogo(Stage stage, String saveFile) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            mapView = new ImageView(new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));
            
            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH); 

            player = new Character("Hero", 100, 2, new Sword("Madeira", 3, 6, "Comum", 4),
                    new Image(getClass().getResource("/images/12.png").toExternalForm()),
                    new Image(getClass().getResource("/images/guts.png").toExternalForm()));

            playerView = new ImageView(player.getSprite());
            playerView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));
            playerView.setFitWidth(64); playerView.setFitHeight(64);
            
            playerView.setX((screenW / 2) - (spriteWidth / 2)); 
            playerView.setY(screenH - spriteHeight - 20); 

            gameRoot.getChildren().add(playerView);
            
            if (saveFile != null) {
                carregarDeJson(saveFile, true); 
            } else {
                inimigosDerrotados = new boolean[LISTA_MAPAS.length][10];
                indiceMapa = 0;
                configurarInimigosPorMapa(0);
            }

            criarMenuPausa();

            mainLayout = new StackPane(mapView, gameRoot, pauseMenu);
            mainLayout.setStyle("-fx-background-color: black;");

            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            cenaMestra.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    togglePause();
                }
                
                if (!isPaused) {
                    if(e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = true;
                    if(e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = true;
                    if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = true;
                    if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = true;
                }
            });

            cenaMestra.setOnKeyReleased(e -> {
                if(e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = false;
                if(e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = false;
                if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = false;
                if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = false;
            });

            cenaMestra.setRoot(mainLayout);
            iniciarTimers(stage, cenaMestra);

            Platform.runLater(() -> {
                if (!stage.isFullScreen()) {
                    stage.setFullScreen(true);
                }
            });

            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    private void criarMenuPausa() {
        pauseMenu = new VBox(20); 
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);"); 
        pauseMenu.setVisible(false); 

        Label title = new Label("JOGO PAUSADO");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));

        Button btnResume = new Button("Voltar ao Jogo");
        btnResume.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        btnResume.setOnAction(e -> togglePause());

        // --- SUBMENU DE SALVAR ---
        HBox boxSalvar = new HBox(10); boxSalvar.setAlignment(Pos.CENTER); boxSalvar.setVisible(false);
        Button s1 = criarBotaoSlot("Salvar Slot 1"); s1.setOnAction(e -> salvarEmJson("save1.json"));
        Button s2 = criarBotaoSlot("Salvar Slot 2"); s2.setOnAction(e -> salvarEmJson("save2.json"));
        Button s3 = criarBotaoSlot("Salvar Slot 3"); s3.setOnAction(e -> salvarEmJson("save3.json"));
        boxSalvar.getChildren().addAll(s1, s2, s3);

        Button btnSave = new Button("Salvar Jogo");
        btnSave.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnSave.setOnAction(e -> boxSalvar.setVisible(!boxSalvar.isVisible()));

        // --- SUBMENU DE CARREGAR ---
        HBox boxCarregar = new HBox(10); boxCarregar.setAlignment(Pos.CENTER); boxCarregar.setVisible(false);
        btnLoadSlot1 = criarBotaoSlot("Carregar Slot 1"); btnLoadSlot1.setOnAction(e -> carregarDeJson("save1.json", false));
        btnLoadSlot2 = criarBotaoSlot("Carregar Slot 2"); btnLoadSlot2.setOnAction(e -> carregarDeJson("save2.json", false));
        btnLoadSlot3 = criarBotaoSlot("Carregar Slot 3"); btnLoadSlot3.setOnAction(e -> carregarDeJson("save3.json", false));
        boxCarregar.getChildren().addAll(btnLoadSlot1, btnLoadSlot2, btnLoadSlot3);

        btnLoadMenu = new Button("Carregar Jogo");
        btnLoadMenu.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        btnLoadMenu.setOnAction(e -> boxCarregar.setVisible(!boxCarregar.isVisible()));

        Button btnExit = new Button("Sair do Jogo");
        btnExit.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        btnExit.setOnAction(e -> Platform.exit()); 

        pauseMenu.getChildren().addAll(title, btnResume, btnSave, boxSalvar, btnLoadMenu, boxCarregar, btnExit);
    }

    private Button criarBotaoSlot(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px; -fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }

    // ==========================================
    // SISTEMA DE SAVE / LOAD
    // ==========================================

    private void salvarEmJson(String arquivo) {
        try {
            StringBuilder sbInimigos = new StringBuilder();
            for (int i = 0; i < inimigosDerrotados.length; i++) {
                for (int j = 0; j < inimigosDerrotados[i].length; j++) {
                    sbInimigos.append(inimigosDerrotados[i][j]);
                    if (j < inimigosDerrotados[i].length - 1) sbInimigos.append(",");
                }
                if (i < inimigosDerrotados.length - 1) sbInimigos.append(";");
            }

            String json = "{\n" +
                          "  \"mapaAtual\": " + indiceMapa + ",\n" +
                          "  \"posicaoX\": " + playerView.getX() + ",\n" +
                          "  \"posicaoY\": " + playerView.getY() + ",\n" +
                          "  \"vidaPlayer\": " + player.getLife() + ",\n" +
                          "  \"inimigosMortos\": \"" + sbInimigos.toString() + "\"\n" +
                          "}";
            
            Files.write(Paths.get(arquivo), json.getBytes());
            System.out.println("Jogo Salvo com Sucesso no " + arquivo + "!");
            
            togglePause(); 
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    private void carregarDeJson(String arquivo, boolean isInitialLoad) {
        try {
            if (!Files.exists(Paths.get(arquivo))) {
                System.out.println("Arquivo " + arquivo + " não encontrado!");
                return;
            }

            String conteudoJson = new String(Files.readAllBytes(Paths.get(arquivo)));
            String textoLimpo = conteudoJson.replaceAll("[\\{\\}\"\\s]", "");
            
            int mapaSalvo = 0;
            double posX = 0;
            double posY = 0;
            int vidaSalva = 100; 
            String inimigosSalvos = "";

            String[] atributos = textoLimpo.split("[:,]"); 
            
            for (int i = 0; i < atributos.length; i++) {
                if (atributos[i].equals("mapaAtual")) mapaSalvo = Integer.parseInt(atributos[i+1]);
                if (atributos[i].equals("posicaoX")) posX = Double.parseDouble(atributos[i+1]);
                if (atributos[i].equals("posicaoY")) posY = Double.parseDouble(atributos[i+1]);
                if (atributos[i].equals("vidaPlayer")) vidaSalva = Integer.parseInt(atributos[i+1]);
                if (atributos[i].equals("inimigosMortos")) {
                    inimigosSalvos = textoLimpo.substring(textoLimpo.indexOf("inimigosMortos:") + 15);
                }
            }

            if (!inimigosSalvos.isEmpty()) {
                String[] mapasString = inimigosSalvos.split(";");
                for (int i = 0; i < mapasString.length; i++) {
                    String[] inims = mapasString[i].split(",");
                    for (int j = 0; j < inims.length; j++) {
                        inimigosDerrotados[i][j] = Boolean.parseBoolean(inims[j]);
                    }
                }
            }

            indiceMapa = mapaSalvo;
            mapView.setImage(new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            configurarInimigosPorMapa(indiceMapa);
            
            playerView.setX(posX);
            playerView.setY(posY);
            player.setLife(vidaSalva);

            System.out.println("Jogo Carregado do " + arquivo + "!");
            
            if (!isInitialLoad) {
                togglePause(); 
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
        }
    }

    // ==========================================

    private void togglePause() {
        isPaused = !isPaused; 

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement(); 
            
            // Verifica arquivos para habilitar/desabilitar botões de load
            boolean f1 = Files.exists(Paths.get("save1.json"));
            boolean f2 = Files.exists(Paths.get("save2.json"));
            boolean f3 = Files.exists(Paths.get("save3.json"));

            btnLoadSlot1.setDisable(!f1); btnLoadSlot1.setText(f1 ? "Carregar Slot 1" : "Slot 1 (Vazio)");
            btnLoadSlot2.setDisable(!f2); btnLoadSlot2.setText(f2 ? "Carregar Slot 2" : "Slot 2 (Vazio)");
            btnLoadSlot3.setDisable(!f3); btnLoadSlot3.setText(f3 ? "Carregar Slot 3" : "Slot 3 (Vazio)");
            
            btnLoadMenu.setDisable(!f1 && !f2 && !f3);
            btnLoadMenu.setText((!f1 && !f2 && !f3) ? "Carregar Jogo (Vazio)" : "Carregar Jogo");
            
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            playerMovement.start();
            if (!inimigosViewsAtuais.isEmpty()) enemyAI.start();
        }
    }

    private void iniciarTimers(Stage stage, Scene scene) {
        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (up) {
                    if (playerView.getY() <= 5) {
                        if (indiceMapa < LISTA_MAPAS.length - 1) trocarCenario(true);
                        else playerView.setY(5);
                    } else {
                        playerView.setY(playerView.getY() - speed);
                    }
                    setDirection(playerView, 3); 
                    animate(playerView);
                } 
                else if (down) {
                    if (playerView.getY() >= screenH - spriteHeight - 5) {
                        if (indiceMapa > 0) trocarCenario(false);
                        else playerView.setY(screenH - spriteHeight - 5); 
                    } else {
                        playerView.setY(playerView.getY() + speed);
                    }
                    setDirection(playerView, 0); 
                    animate(playerView);
                } 
                else if (left) {
                    if (playerView.getX() > 5) { 
                        playerView.setX(playerView.getX() - speed);
                    }
                    setDirection(playerView, 1); 
                    animate(playerView);
                } 
                else if (right) {
                    if (playerView.getX() < screenW - spriteWidth - 5) { 
                        playerView.setX(playerView.getX() + speed);
                    }
                    setDirection(playerView, 2); 
                    animate(playerView);
                }
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now - lastEnemyFrameTime > frameDelay){
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                for (int i = 0; i < inimigosViewsAtuais.size(); i++) {
                    ImageView ev = inimigosViewsAtuais.get(i);
                    Monsters monstro = monstrosAtuais.get(i);

                    double dirMove = (double) ev.getProperties().get("dirMove");
                    ev.setX(ev.getX() + dirMove);
                    
                    int eDir = (dirMove > 0) ? 2 : 1;
                    ev.setViewport(new Rectangle2D(enemyFrame * enemySpriteWidth, eDir * enemySpriteHeight, enemySpriteWidth, enemySpriteHeight));

                    if (ev.getX() > screenW - 150) ev.getProperties().put("dirMove", -1.0);
                    if (ev.getX() < 50) ev.getProperties().put("dirMove", 1.0);

                    double dx = playerView.getX() - ev.getX();
                    double dy = playerView.getY() - ev.getY();
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    
                    if (distance < 50) {
                        playerMovement.stop(); 
                        enemyAI.stop();
                        playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40)); 
                        
                        monstroEmBatalhaIndex = i; 

                        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                        ft.setFromValue(1); ft.setToValue(0);
                        ft.setOnFinished(evTransition -> Battle.startBattle(stage, scene, player, monstro, playerView, ev, App.this, mainLayout));
                        ft.play();
                        return; 
                    }
                }
            }
        };
        enemyAI.start();
    }

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
                if(!inimigosViewsAtuais.isEmpty()) enemyAI.start(); 
                return; 
            }

            mapView.setImage(new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            
            configurarInimigosPorMapa(indiceMapa);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> { 
                playerMovement.start(); 
                if(!inimigosViewsAtuais.isEmpty()) enemyAI.start(); 
            });
            fadeIn.play();
        });
        ft.play();
    }

    private void configurarInimigosPorMapa(int mapa) {
        gameRoot.getChildren().removeAll(inimigosViewsAtuais);
        monstrosAtuais.clear();
        inimigosViewsAtuais.clear();

        if (mapa == 0) {
            adicionarInimigo(new Goblin(), 0, screenW * 0.3, screenH * 0.1); 
            adicionarInimigo(new Goblin(), 1, screenW * 0.7, screenH * 0.2); 
        } 
        else if (mapa == 1) {
            adicionarInimigo(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1);
            adicionarInimigo(new Goblin(), 1, screenW * 0.2, screenH * 0.15);
        } 
        else if (mapa == 2) {
            adicionarInimigo(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1);
        }
    }

    private void adicionarInimigo(Monsters monstro, int idUnico, double startX, double startY) {
        if (inimigosDerrotados[indiceMapa][idUnico]) return;
        
        String path = monstro.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);
        
        if (imageUrl == null) {
            System.err.println("ERRO FATAL: Imagem não encontrada no caminho: " + path);
            return;
        }

        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        view.setViewport(new Rectangle2D(0, 0, enemySpriteWidth, enemySpriteHeight));
        view.setFitWidth(80); view.setFitHeight(80);
        view.setX(startX); view.setY(startY);

        view.getProperties().put("idNoMapa", idUnico);
        view.getProperties().put("dirMove", 1.0);

        monstrosAtuais.add(monstro);
        inimigosViewsAtuais.add(view);
        gameRoot.getChildren().add(view);
    }

    public void resumeTimers() {
        resetMovement();
        
        if (monstroEmBatalhaIndex != -1) {
            Monsters monstroLutado = monstrosAtuais.get(monstroEmBatalhaIndex);
            ImageView viewLutada = inimigosViewsAtuais.get(monstroEmBatalhaIndex);

            if (monstroLutado.getLife() <= 0) {
                int idNoMapa = (int) viewLutada.getProperties().get("idNoMapa");
                inimigosDerrotados[indiceMapa][idNoMapa] = true;

                gameRoot.getChildren().remove(viewLutada);
                inimigosViewsAtuais.remove(monstroEmBatalhaIndex);
                monstrosAtuais.remove(monstroEmBatalhaIndex);
            }
            monstroEmBatalhaIndex = -1; 
        }

        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
        
        playerMovement.start();
        if (!inimigosViewsAtuais.isEmpty()) enemyAI.start(); 
    }

    private void animate(ImageView view) {
        long now = System.nanoTime();
        if(now - lastFrameTime > frameDelay){
            frame = (frame+1) %4;
            view.setViewport(new Rectangle2D(frame*spriteWidth, direction*spriteHeight, spriteWidth, spriteHeight));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView view, int newDirection){
        if(direction != newDirection){
            direction = newDirection; frame = 0;
            view.setViewport(new Rectangle2D(0, direction*spriteHeight, spriteWidth, spriteHeight));
        }
    }

    public static void main(String[] args){ launch(); }
}