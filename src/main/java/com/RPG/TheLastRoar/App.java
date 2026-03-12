package com.RPG.TheLastRoar;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends javafx.application.Application {

    private Goblin goblin = new Goblin();
    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;
    private Pane gameRoot;
    private ImageView enemyView;
    private StackPane mainLayout;

    private long lastFrameTime = 0;
    private int direction = 0;
    private final long frameDelay = 200_000_000;
    private double speed = 3;
    private int frame = 0;
    private final int spriteWidth = 64;
    private final int spriteHeight = 64;

    private int enemyFrame = 0;
    private long lastEnemyFrameTime = 0;
    private int enemyDirection = 2;
    private final int enemySpriteWidth = 128;
    private final int enemySpriteHeight = 128;

    private static boolean up, down, left, right;

    public static void resetMovement(){ up = down = left = right = false; }

    public void resumeTimers(){
        resetMovement();
        if(goblin.getLife() <= 0) {
            if (enemyView != null) gameRoot.getChildren().remove(enemyView);
            if(enemyAI != null) enemyAI.stop();
        } else {
            if(enemyAI != null) enemyAI.start();
        }
        
        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
        
        if(playerMovement != null) playerMovement.start();
    }

    private void animate(ImageView playerView) {
        long now = System.nanoTime();
        if(now - lastFrameTime > frameDelay){
            frame = (frame+1) %4;
            playerView.setViewport(new Rectangle2D(frame*spriteWidth, direction*spriteHeight, spriteWidth, spriteHeight));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView playerView, int newDirection){
        if(direction != newDirection){
            direction = newDirection;
            frame = 0;
            playerView.setViewport(new Rectangle2D(0, direction*spriteHeight, spriteWidth, spriteHeight));
        }
    }

    private void animateEnemy(ImageView enemy){
        long now = System.nanoTime();
        if(now - lastEnemyFrameTime > frameDelay){
            enemyFrame = (enemyFrame + 1) % 4;
            enemy.setViewport(new Rectangle2D(enemyFrame*enemySpriteWidth, enemyDirection*enemySpriteHeight, enemySpriteWidth, enemySpriteHeight));
            lastEnemyFrameTime = now;
        }
    }

    @Override
    public void start(Stage stage){
        // Camada de baixo: O Mapa que estica
        ImageView mapView = new ImageView(new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));
        mapView.fitWidthProperty().bind(stage.widthProperty());
        mapView.fitHeightProperty().bind(stage.heightProperty());

        // Camada de cima: O Jogo com tamanho normal (800x600)
        gameRoot = new Pane();
        gameRoot.setPrefSize(800, 600);
        gameRoot.setMaxSize(800, 600);

        Character player = new Character("Hero", 100, 2, new Sword("Madeira",3,6,"Comum",4), 
                                        new Image(getClass().getResource("/images/12.png").toExternalForm()), 
                                        new Image(getClass().getResource("/images/guts.png").toExternalForm()));

        ImageView playerView = new ImageView(player.getSprite());
        playerView.setViewport(new Rectangle2D(0,0,spriteWidth,spriteHeight));
        playerView.setFitWidth(64); playerView.setFitHeight(64);
        playerView.setX(400); playerView.setY(300);

        enemyView = new ImageView(new Image(getClass().getResource("/images/vv.png").toExternalForm()));
        enemyView.setViewport(new Rectangle2D(0,0,128,128));
        enemyView.setFitWidth(80); enemyView.setFitHeight(80);
        enemyView.setX(200); enemyView.setY(200);

        gameRoot.getChildren().addAll(enemyView, playerView);
        
        // StackPane junta tudo e centraliza o gameRoot
        mainLayout = new StackPane(mapView, gameRoot);
        mainLayout.setStyle("-fx-background-color: black;");
        
        Scene scene = new Scene(mainLayout, 800, 600);

        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = true;
            if(e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = true;
            if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = true;
            if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = true;
        });

        scene.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = false;
            if(e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = false;
            if(e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = false;
            if(e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = false;
        });

playerMovement = new AnimationTimer(){
            @Override
            public void handle(long now){
                // Barreiras para o eixo Y (Cima e Baixo)
                if(up && playerView.getY() > 0){ 
                    playerView.setY(playerView.getY() - speed); 
                    setDirection(playerView, 3); 
                    animate(playerView);
                }
                else if(down && playerView.getY() < 600 - playerView.getFitHeight()){ 
                    playerView.setY(playerView.getY() + speed); 
                    setDirection(playerView, 0); 
                    animate(playerView);
                }
                
                // Barreiras para o eixo X (Esquerda e Direita)
                if(left && playerView.getX() > 0){ 
                    playerView.setX(playerView.getX() - speed); 
                    setDirection(playerView, 1); 
                    animate(playerView);
                }
                else if(right && playerView.getX() < 800 - playerView.getFitWidth()){ 
                    playerView.setX(playerView.getX() + speed); 
                    setDirection(playerView, 2); 
                    animate(playerView);
                }
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer(){
            double dirMove = 1;
            @Override
            public void handle(long now){
                if(goblin.getLife() <= 0) return;
                enemyView.setX(enemyView.getX() + dirMove);
                animateEnemy(enemyView);
                if(enemyView.getX() > 600){ dirMove = -1; enemyDirection = 1; }
                if(enemyView.getX() < 100){ dirMove = 1; enemyDirection = 2; }

                double dx = playerView.getX() - enemyView.getX();
                double dy = playerView.getY() - enemyView.getY();
                double distance = Math.sqrt(dx*dx + dy*dy);
                
                if(distance < 50){
                    playerMovement.stop(); enemyAI.stop();
                    playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40));
                    
                    FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                    ft.setFromValue(1); ft.setToValue(0);
                    ft.setOnFinished(ev -> Battle.startBattle(stage, scene, player, goblin, playerView, enemyView, App.this));
                    ft.play();
                }
            }
        };
        enemyAI.start();

        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){ launch(); }
}