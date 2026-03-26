package com.RPG.TheLastRoar;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * EnemyManager.java
 * Gere a criação, movimentação, animação, colisão e remoção dos inimigos no mapa.
 */
public class EnemyManager {

    // ==========================================
    // 1. VARIÁVEIS DE CONTROLO
    // ==========================================
    private final Pane gameRoot;
    private final double screenW;
    private final double screenH;

    // Listas que guardam os dados e as imagens dos monstros ativos no mapa
    private final List<Monsters>   monstros = new ArrayList<>();
    private final List<ImageView>  views    = new ArrayList<>();

    // Matriz para guardar quais inimigos já foram mortos (evita que renasçam ao voltar ao mapa)
    private final boolean[][] inimigosDerrotados;
    private int indiceMapa = 0;

    // ==========================================
    // 2. CONSTRUTOR
    // ==========================================
    public EnemyManager(Pane gameRoot, double screenW, double screenH, boolean[][] inimigosDerrotados) {
        this.gameRoot            = gameRoot;
        this.screenW             = screenW;
        this.screenH             = screenH;
        this.inimigosDerrotados  = inimigosDerrotados;
    }

    // ==========================================
    // 3. CONFIGURAÇÃO DE MAPAS E INIMIGOS
    // ==========================================
    
    /**
     * Limpa o mapa atual e gera os monstros corretos para o novo cenário.
     */
    public void configurarParaMapa(int mapa) {
        this.indiceMapa = mapa;
        
        // Limpa os monstros do cenário anterior
        gameRoot.getChildren().removeAll(views);
        monstros.clear();
        views.clear();

        // Adiciona os monstros dependendo do mapa escolhido
        switch (mapa) {
            case 0 -> {
                // Goblin normal: Corte 128x128 na imagem, Exibe 80x80 no ecrã
                adicionar(new Goblin(), 6, screenW * 0.3, screenH * 0.1, 128, 80);
                adicionar(new Goblin(), 1, screenW * 0.7, screenH * 0.2, 128, 80);
            }
            case 1 -> {
                adicionar(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1, 128, 80);
                adicionar(new GoblinExp(), 1, screenW * 0.2, screenH * 0.15, 128, 80);
            }
            case 2 -> {
                // BOSS: Tamanho de corte e exibição maiores (256x256)
                adicionar(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1, 256, 256);
            }
        }
    }

    /**
     * Instancia a imagem do monstro no mapa e guarda as suas propriedades.
     */
    private void adicionar(Monsters monstro, int idUnico, double x, double y, int spriteSize, int displaySize) {
        // Se este monstro específico já foi derrotado anteriormente, ignora-o
        if (inimigosDerrotados[indiceMapa][idUnico]) return;

        String path = monstro.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) {
            System.err.println("ERRO: Imagem do monstro não encontrada -> " + path);
            return;
        }

        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        
        // Ajusta o corte inicial (Viewport)
        view.setViewport(new Rectangle2D(0, 0, spriteSize, spriteSize));
        
        // Ajusta o tamanho visual no ecrã
        view.setFitWidth(displaySize);
        view.setFitHeight(displaySize);
        
        // Define a posição inicial
        view.setX(x);
        view.setY(y);
        
        // Guarda as propriedades vitais dentro da própria view para a animação
        view.getProperties().put("idNoMapa", idUnico);
        view.getProperties().put("dirMove",  1.0); // 1.0 = Direita, -1.0 = Esquerda
        view.getProperties().put("spriteSize", spriteSize); 
        view.getProperties().put("displaySize", (double)displaySize);

        // Adiciona às listas ativas e ao cenário
        monstros.add(monstro);
        views.add(view);
        gameRoot.getChildren().add(view);
    }

    // ==========================================
    // 4. LÓGICA DE JOGO (ATUALIZAÇÃO E COLISÃO)
    // ==========================================
    
    /**
     * Move os monstros, anima os sprites e verifica se o jogador chocou com algum deles.
     * Retorna o índice do monstro colidido ou -1 se não houver colisão.
     */
    public int atualizar(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView ev = views.get(i);
            
            // Recupera as propriedades guardadas
            double dirMove = (double) ev.getProperties().get("dirMove");
            int sSize = (int) ev.getProperties().get("spriteSize"); 
            double dSize = (double) ev.getProperties().get("displaySize");

            // Movimenta o inimigo horizontalmente
            ev.setX(ev.getX() + dirMove);

            // Determina a direção (linha da sprite sheet)
            int eDir = (dirMove > 0) ? 2 : 1;
            
            // Atualiza a animação usando o frame atual sincronizado com o jogo
            ev.setViewport(new Rectangle2D(
                enemyFrame * sSize,
                eDir * sSize,
                sSize, 
                sSize
            ));

            // Lógica de "Bate e Volta" nos limites do ecrã
            if (ev.getX() > screenW - dSize - 50) ev.getProperties().put("dirMove", -1.0);
            if (ev.getX() < 50) ev.getProperties().put("dirMove", 1.0);

            // Verificação de Colisão (Fórmula da Distância Euclidiana)
            double dx = (playerX + 32) - (ev.getX() + dSize / 2);
            double dy = (playerY + 32) - (ev.getY() + dSize / 2);
            
            if (Math.sqrt(dx * dx + dy * dy) < (dSize * 0.4)) {
                return i; // Bateu! Retorna o índice deste monstro.
            }
        }
        return -1; // Não bateu em ninguém
    }
    
    /**
     * Remove permanentemente um inimigo do jogo após ser derrotado em batalha.
     * Esta é a função vital que impede o "Rollback" fantasma.
     */
    public void removerInimigo(int index) {
        if (index < 0 || index >= views.size()) return;

        ImageView view = views.get(index);
        
        // 1. Marca como derrotado no sistema de saves/persistência
        Object idObj = view.getProperties().get("idNoMapa");
        if (idObj != null) {
            int idNoMapa = (int) idObj;
            inimigosDerrotados[indiceMapa][idNoMapa] = true;
        }

        // 2. Remove o monstro visualmente do cenário
        gameRoot.getChildren().remove(view);

        // 3. Remove das listas internas (impede que o loop continue a processar este monstro)
        views.remove(index);
        monstros.remove(index);
        
        System.out.println("[EnemyManager] Inimigo removido da memória. Restam: " + monstros.size());
    }

    // ==========================================
    // 5. MÉTODOS AUXILIARES (GETTERS)
    // ==========================================
    
    public Monsters getMonstro(int index)   { return monstros.get(index); }
    public ImageView getView(int index)     { return views.get(index); }
    public boolean isEmpty()                { return views.isEmpty(); }
}