package com.RPG.TheLastRoar;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Gerencia os inimigos presentes no mapa atual:
 * carregamento, posicionamento, animação e remoção após derrota.
 */
public class EnemyManager {

    private static final int SPRITE_WIDTH  = 128;
    private static final int SPRITE_HEIGHT = 128;

    private final Pane gameRoot;
    private final double screenW;
    private final double screenH;

    private final List<Monsters>   monstros = new ArrayList<>();
    private final List<ImageView>  views    = new ArrayList<>();

    private final boolean[][] inimigosDerrotados;
    private int indiceMapa = 0;

    public EnemyManager(Pane gameRoot, double screenW, double screenH,
                         boolean[][] inimigosDerrotados) {
        this.gameRoot            = gameRoot;
        this.screenW             = screenW;
        this.screenH             = screenH;
        this.inimigosDerrotados  = inimigosDerrotados;
    }

    // ----------------------------------------------------------
    // Configuração por mapa
    // ----------------------------------------------------------

    /**
     * Limpa os inimigos atuais e adiciona os do mapa indicado.
     */
    public void configurarParaMapa(int mapa) {
        this.indiceMapa = mapa;
        gameRoot.getChildren().removeAll(views);
        monstros.clear();
        views.clear();

        switch (mapa) {
            case 0 -> {
                adicionar(new Goblin(),     6, screenW * 0.3, screenH * 0.1);
                adicionar(new Goblin(),     1, screenW * 0.7, screenH * 0.2);
            }
            case 1 -> {
                adicionar(new GoblinExp(),  0, screenW * 0.5, screenH * 0.1);
                adicionar(new Goblin(),     1, screenW * 0.2, screenH * 0.15);
            }
            case 2 -> {
                adicionar(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1);
            }
        }
    }

    private void adicionar(Monsters monstro, int idUnico, double x, double y) {
        if (inimigosDerrotados[indiceMapa][idUnico]) return;

        String path = monstro.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) {
            System.err.println("ERRO: Imagem não encontrada: " + path);
            return;
        }

        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        view.setViewport(new Rectangle2D(0, 0, SPRITE_WIDTH, SPRITE_HEIGHT));
        view.setFitWidth(80);
        view.setFitHeight(80);
        view.setX(x);
        view.setY(y);
        view.getProperties().put("idNoMapa", idUnico);
        view.getProperties().put("dirMove",  1.0);

        monstros.add(monstro);
        views.add(view);
        gameRoot.getChildren().add(view);
    }

    // ----------------------------------------------------------
    // Animação e movimento (chamado pelo timer de IA)
    // ----------------------------------------------------------

    /**
     * Atualiza posição e frame de animação de todos os inimigos.
     * Retorna o índice do inimigo em colisão com o jogador, ou -1.
     *
     * @param playerX        Posição X atual do jogador
     * @param playerY        Posição Y atual do jogador
     * @param enemyFrame     Frame de animação atual
     */
    public int atualizar(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView ev     = views.get(i);
            double dirMove   = (double) ev.getProperties().get("dirMove");

            // Movimenta horizontalmente
            ev.setX(ev.getX() + dirMove);

            // Animação de direção
            int eDir = (dirMove > 0) ? 2 : 1;
            ev.setViewport(new Rectangle2D(
                enemyFrame * SPRITE_WIDTH,
                eDir * SPRITE_HEIGHT,
                SPRITE_WIDTH, SPRITE_HEIGHT
            ));

            // Inverte nas bordas
            if (ev.getX() > screenW - 150) ev.getProperties().put("dirMove", -1.0);
            if (ev.getX() < 50)            ev.getProperties().put("dirMove",  1.0);

            // Detecção de colisão
            double dx = playerX - ev.getX();
            double dy = playerY - ev.getY();
            if (Math.sqrt(dx * dx + dy * dy) < 50) {
                return i;
            }
        }
        return -1;
    }

    // ----------------------------------------------------------
    // Pós-batalha
    // ----------------------------------------------------------

    /**
     * Marca o inimigo como derrotado e remove da tela.
     */
    public void removerInimigo(int index) {
        ImageView view = views.get(index);
        int idNoMapa   = (int) view.getProperties().get("idNoMapa");
        inimigosDerrotados[indiceMapa][idNoMapa] = true;

        gameRoot.getChildren().remove(view);
        views.remove(index);
        monstros.remove(index);
    }

    // ----------------------------------------------------------
    // Getters
    // ----------------------------------------------------------

    public Monsters getMonstro(int index)   { return monstros.get(index); }
    public ImageView getView(int index)     { return views.get(index); }
    public boolean isEmpty()                { return views.isEmpty(); }
    public int getTotalMapas()              { return inimigosDerrotados.length; }
}
