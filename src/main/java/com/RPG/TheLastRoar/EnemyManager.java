package com.RPG.TheLastRoar;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class EnemyManager {

    // Tamanho padrão para inimigos comuns
    private static final int DEFAULT_SPRITE_SIZE = 128;

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

    public void configurarParaMapa(int mapa) {
        this.indiceMapa = mapa;
        gameRoot.getChildren().removeAll(views);
        monstros.clear();
        views.clear();

        switch (mapa) {
            case 0 -> {
                // Goblin normal: Corte 128x128, Exibe 80x80
                adicionar(new Goblin(), 6, screenW * 0.3, screenH * 0.1, 128, 80);
                adicionar(new Goblin(), 1, screenW * 0.7, screenH * 0.2, 128, 80);
            }
            case 1 -> {
                adicionar(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1, 128, 80);
                adicionar(new GoblinExp(), 1, screenW * 0.2, screenH * 0.15, 128, 80);
            }
            case 2 -> {
                // BOSS: Se a sua folha (spritesheet) do Boss tem frames de 256x256, 
                // usamos 256 para o corte E 256 para a exibição.
                adicionar(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1, 256, 256);
            }
        }
    }

    // Agora recebe spriteSize (tamanho do corte na imagem) e displaySize (tamanho na tela)
    private void adicionar(Monsters monstro, int idUnico, double x, double y, int spriteSize, int displaySize) {
        if (inimigosDerrotados[indiceMapa][idUnico]) return;

        String path = monstro.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) return;

        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        
        // Ajusta o corte (Viewport) de acordo com o tamanho real do frame na sheet do Boss
        view.setViewport(new Rectangle2D(0, 0, spriteSize, spriteSize));
        
        // Ajusta o tamanho visual
        view.setFitWidth(displaySize);
        view.setFitHeight(displaySize);
        
        view.setX(x);
        view.setY(y);
        
        // Guardamos o spriteSize nas propriedades para a animação não bugar depois
        view.getProperties().put("idNoMapa", idUnico);
        view.getProperties().put("dirMove",  1.0);
        view.getProperties().put("spriteSize", spriteSize); 
        view.getProperties().put("displaySize", (double)displaySize);

        monstros.add(monstro);
        views.add(view);
        gameRoot.getChildren().add(view);
    }

    public int atualizar(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView ev = views.get(i);
            double dirMove = (double) ev.getProperties().get("dirMove");
            int sSize = (int) ev.getProperties().get("spriteSize"); // Pega o tamanho do corte deste monstro específico
            double dSize = (double) ev.getProperties().get("displaySize");

            ev.setX(ev.getX() + dirMove);

            int eDir = (dirMove > 0) ? 2 : 1;
            
            // O segredo está aqui: multiplicamos o frame pelo tamanho real do frame desse monstro (sSize)
            ev.setViewport(new Rectangle2D(
                enemyFrame * sSize,
                eDir * sSize,
                sSize, 
                sSize
            ));

            if (ev.getX() > screenW - dSize - 50) ev.getProperties().put("dirMove", -1.0);
            if (ev.getX() < 50) ev.getProperties().put("dirMove", 1.0);

            // Colisão baseada no tamanho de exibição
            double dx = (playerX + 32) - (ev.getX() + dSize/2);
            double dy = (playerY + 32) - (ev.getY() + dSize/2);
            if (Math.sqrt(dx * dx + dy * dy) < (dSize * 0.4)) {
                return i;
            }
        }
        return -1;
    }

    public void removerInimigo(int index) {
        ImageView view = views.get(index);
        int idNoMapa = (int) view.getProperties().get("idNoMapa");
        inimigosDerrotados[indiceMapa][idNoMapa] = true;
        gameRoot.getChildren().remove(view);
        views.remove(index);
        monstros.remove(index);
    }

    public Monsters getMonstro(int index)   { return monstros.get(index); }
    public ImageView getView(int index)     { return views.get(index); }
    public boolean isEmpty()                { return views.isEmpty(); }
}