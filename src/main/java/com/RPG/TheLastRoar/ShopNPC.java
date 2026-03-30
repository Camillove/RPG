package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * ============================================================
 * ShopNPC.java — NPC vendedor no mapa 0
 * ============================================================
 *
 * COMO FUNCIONA:
 * 1. O NPC é um ImageView posicionado fixo no mapa 0
 * 2. O App verifica colisão com ele no timer playerMovement
 * 3. Ao colidir, abre uma tela de loja (overlay estilo PauseMenu)
 * 4. O jogador compra itens com as moedas que tem
 * 5. Itens comprados vão direto pro inventário
 *
 * ITENS À VENDA:
 * - Adaga         (10 moedas) — espada rápida, 2 ataques
 * - Espada Longa  (25 moedas) — espada com crítico variável
 * - Katana Rara   (50 moedas) — crítico 3x
 * - Poção Pequena  (5 moedas) — cura 30 HP
 * - Poção Grande  (15 moedas) — cura 70 HP
 *
 * ESTRUTURA DO LAYOUT DA LOJA:
 *  ┌──────────────────────────────────────────────┐
 *  │  LOJA DO FERREIRO ───────────────────────    │
 *  │                                              │
 *  │  Ouro: 42 🪙                                 │
 *  │                                              │
 *  │  [⚔ Adaga]        Dano: 5 · 10 moedas       │
 *  │  [⚔ Espada Longa] Dano: 8 · 25 moedas       │
 *  │  [⚔ Katana Rara]  Dano:10 · 50 moedas       │
 *  │  [🧪 Poção Pequena]  +30 HP · 5 moedas      │
 *  │  [🧪 Poção Grande]   +70 HP · 15 moedas     │
 *  │                                              │
 *  │                          [FECHAR]            │
 *  └──────────────────────────────────────────────┘
 */
public class ShopNPC {

    // ── Posição do NPC no mapa (ajuste conforme seu mapa) ─────────────────
    public static final double NPC_X = 200;  // pixels da esquerda
    public static final double NPC_Y = 100;  // pixels do topo
    public static final double NPC_SIZE = 70; // tamanho do sprite no mapa

    // ── Raio de colisão (em pixels) para abrir a loja ─────────────────────
    private static final double RAIO_COLISAO = 80;

    // ── Flag para não abrir a loja duas vezes ─────────────────────────────
    private static boolean lojaAberta = false;

    // =========================================================================
    // CRIAÇÃO DO SPRITE DO NPC NO MAPA
    // =========================================================================

    /**
     * Cria e retorna o ImageView do NPC para ser adicionado ao gameRoot.
     * Se a imagem não existir, usa um placeholder colorido.
     */
    public static ImageView criarSprite() {
        ImageView npcView = new ImageView();

        // Tenta carregar o sprite do NPC
        // Coloque uma imagem em /images/npc_vendedor.png no seu projeto
        // Se não tiver, o NPC aparece como um retângulo (placeholder)
        try {
            var url = ShopNPC.class.getResource("/images/npc_vendedor.png");
            if (url != null) {
                npcView.setImage(new Image(url.toExternalForm()));
            } else {
                System.out.println("[ShopNPC] Sprite npc_vendedor.png nao encontrado, usando placeholder.");
                // Placeholder: um ícone de "loja" gerado em texto (sem imagem)
            }
        } catch (Exception e) {
            System.out.println("[ShopNPC] Erro ao carregar sprite: " + e.getMessage());
        }

        // Posição e tamanho fixos no mapa
        npcView.setFitWidth(NPC_SIZE);
        npcView.setFitHeight(NPC_SIZE);
        npcView.setX(NPC_X);
        npcView.setY(NPC_Y);
        npcView.setPreserveRatio(true);

        // Marcador de ID para identificar este nó depois
        npcView.setId("npc_vendedor");

        return npcView;
    }

    // =========================================================================
    // VERIFICAÇÃO DE COLISÃO
    // =========================================================================

    /**
     * Verifica se o jogador está próximo o suficiente do NPC para abrir a loja.
     *
     * @param playerX  Posição X do jogador
     * @param playerY  Posição Y do jogador
     * @return true se o jogador está dentro do raio de colisão
     */
    public static boolean verificarColisao(double playerX, double playerY) {
        // Centro do NPC
        double npcCentroX = NPC_X + NPC_SIZE / 2;
        double npcCentroY = NPC_Y + NPC_SIZE / 2;

        // Centro do jogador (aproximado como 32x32 dentro do sprite)
        double playerCentroX = playerX + 40;
        double playerCentroY = playerY + 40;

        // Distância euclidiana entre os dois centros
        double dx = playerCentroX - npcCentroX;
        double dy = playerCentroY - npcCentroY;
        return Math.sqrt(dx * dx + dy * dy) < RAIO_COLISAO;
    }

    // =========================================================================
    // ABERTURA DA LOJA
    // =========================================================================

    /**
     * Abre a tela da loja como overlay sobre o jogo.
     *
     * @param mainLayout  StackPane principal do jogo
     * @param player      Personagem do jogador (para checar moedas e inventário)
     * @param hudManager  HudManager para atualizar a HUD após a compra
     * @param onClose     Callback chamado ao fechar a loja (retoma timers)
     */
    public static void abrirLoja(StackPane mainLayout, Character player,
                                  HudManager hudManager, Runnable onClose) {
        // Evita abrir duas vezes
        if (lojaAberta) return;
        lojaAberta = true;

        // Constrói o painel da loja
        VBox painel = construirPainelLoja(player, hudManager, () -> {
            lojaAberta = false;
            fechar(mainLayout, onClose);
        });

        // Overlay escuro semitransparente (igual ao InventoryScreen)
        StackPane overlay = new StackPane(painel);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.82);");
        overlay.setAlignment(Pos.CENTER_LEFT);

        // Fade in
        overlay.setOpacity(0);
        mainLayout.getChildren().add(overlay);
        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setToValue(1.0);
        ft.play();
    }

    // =========================================================================
    // CONSTRUÇÃO DO PAINEL DA LOJA
    // =========================================================================

    private static VBox construirPainelLoja(Character player, HudManager hudManager,
                                             Runnable onClose) {
        VBox painel = new VBox(0);
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setPadding(new Insets(0, 0, 0, 120)); // Alinhamento estilo Ruined King

        // ── Título ────────────────────────────────────────────────────────────
        Text titulo = new Text("LOJA DO FERREIRO");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 44));
        titulo.setFill(Color.web("#F0E6C0"));

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#C8A000", 0.8));
        sombra.setRadius(20);
        titulo.setEffect(sombra);
        VBox.setMargin(titulo, new Insets(0, 0, 6, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa ──────────────────────────────────────────────────
        Line sep = new Line(0, 0, 320, 0);
        sep.setStroke(Color.web("#B8960C", 0.6));
        sep.setStrokeWidth(1.0);
        VBox.setMargin(sep, new Insets(4, 0, 16, 0));
        painel.getChildren().add(sep);

        // ── Ouro do jogador ───────────────────────────────────────────────────
        Label lblOuro = new Label("Ouro:  " + player.getCoin() + "  \uD83E\uDE99");
        lblOuro.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblOuro.setTextFill(Color.GOLD);
        VBox.setMargin(lblOuro, new Insets(0, 0, 20, 4));
        painel.getChildren().add(lblOuro);

        // ── Label da seção de armas ───────────────────────────────────────────
        Text lblArmas = new Text("ARMAS");
        lblArmas.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblArmas.setFill(Color.web("#888070"));
        VBox.setMargin(lblArmas, new Insets(0, 0, 8, 4));
        painel.getChildren().add(lblArmas);

        // ── Itens à venda — Armas ─────────────────────────────────────────────
        // Cada item: (objeto Item, preço em moedas, descrição extra)
        adicionarItemLoja(painel, new Sword("Adaga", 10, 5, "Comum", 2),
                10, "Ataca 2x por turno", player, lblOuro, hudManager);

        adicionarItemLoja(painel, new Sword("Espada Longa", 25, 8, "Rara", 3),
                25, "Critico com 18+", player, lblOuro, hudManager);

        adicionarItemLoja(painel, new Sword("Katana", 50, 10, "Rara", 3),
                50, "Critico 3x", player, lblOuro, hudManager);

        // ── Label da seção de poções ──────────────────────────────────────────
        Text lblPocoes = new Text("POCOES");
        lblPocoes.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblPocoes.setFill(Color.web("#888070"));
        VBox.setMargin(lblPocoes, new Insets(14, 0, 8, 4));
        painel.getChildren().add(lblPocoes);

        adicionarItemLoja(painel, new Potion("Pocao Pequena", 5, 1, 30),
                5, "Restaura 30 HP", player, lblOuro, hudManager);

        adicionarItemLoja(painel, new Potion("Pocao Grande", 15, 1, 70),
                15, "Restaura 70 HP", player, lblOuro, hudManager);

        // ── Botão Fechar ──────────────────────────────────────────────────────
        Button btnFechar = criarBotaoFechar();
        VBox.setMargin(btnFechar, new Insets(20, 0, 0, 0));
        btnFechar.setOnAction(e -> onClose.run());
        painel.getChildren().add(btnFechar);

        return painel;
    }

    // =========================================================================
    // LINHA DE ITEM DA LOJA
    // =========================================================================

    /**
     * Adiciona uma linha de item comprável ao painel da loja.
     * Cada linha tem: ícone + nome + descrição + preço + botão COMPRAR.
     */
    private static void adicionarItemLoja(VBox painel, Item item, int preco,
                                           String descricao, Character player,
                                           Label lblOuro, HudManager hudManager) {
        HBox linha = new HBox(14);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMaxWidth(560);
        linha.setStyle(
            "-fx-background-color: rgba(20,20,20,0.6);" +
            "-fx-border-color: #443E30;" +
            "-fx-border-width: 0 0 0 2;" +
            "-fx-padding: 10 16 10 12;" +
            "-fx-background-radius: 6;"
        );
        VBox.setMargin(linha, new Insets(0, 0, 6, 0));

        // Ícone do tipo de item
        Label icone = new Label(item instanceof Sword ? "\u2694" : "\uD83E\uDDEA");
        icone.setFont(Font.font("Segoe UI Emoji", 20));

        // Nome e descrição
        VBox infoBox = new VBox(2);
        Label nome = new Label(item.getName());
        nome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        nome.setTextFill(Color.web("#E8DFC0"));

        // Atributo principal
        String atributo = "";
        if (item instanceof Sword s) atributo = "Dano: " + s.getDamage() + "  |  " + descricao;
        else if (item instanceof Potion p) atributo = "+" + p.getHealedLife() + " HP  |  " + descricao;

        Label desc = new Label(atributo);
        desc.setFont(Font.font("Segoe UI", 12));
        desc.setTextFill(Color.web("#888070"));
        infoBox.getChildren().addAll(nome, desc);

        // Espaçador
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Preço
        Label lblPreco = new Label(preco + " \uD83E\uDE99");
        lblPreco.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblPreco.setTextFill(Color.GOLD);
        lblPreco.setMinWidth(70);

        // Botão COMPRAR
        Button btnComprar = new Button("COMPRAR");
        btnComprar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        String estNormal =
            "-fx-background-color: rgba(0,180,180,0.15);" +
            "-fx-border-color: #00E5E5;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: #00E5E5;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;";
        String estHover =
            "-fx-background-color: rgba(0,200,200,0.30);" +
            "-fx-border-color: #00E5E5;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;";
        String estDisable =
            "-fx-background-color: rgba(20,20,20,0.4);" +
            "-fx-border-color: #443E30;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: #443E30;" +
            "-fx-padding: 6 14;";

        btnComprar.setStyle(estNormal);
        btnComprar.setOnMouseEntered(e -> { if (!btnComprar.isDisabled()) btnComprar.setStyle(estHover); });
        btnComprar.setOnMouseExited(e ->  { if (!btnComprar.isDisabled()) btnComprar.setStyle(estNormal); });

        // Desabilita o botão se o jogador não tem moedas suficientes
        if (player.getCoin() < preco) {
            btnComprar.setDisable(true);
            btnComprar.setStyle(estDisable);
        }

        // ── Lógica de compra ──────────────────────────────────────────────────
        btnComprar.setOnAction(e -> {
            // Verifica novamente se tem moedas (pode ter mudado)
            if (player.getCoin() < preco) {
                nome.setTextFill(Color.web("#FF6666")); // Flash de erro: nome fica vermelho
                javafx.animation.PauseTransition flash =
                    new javafx.animation.PauseTransition(Duration.millis(600));
                flash.setOnFinished(ev -> nome.setTextFill(Color.web("#E8DFC0")));
                flash.play();
                return;
            }

            // Tenta adicionar ao inventário (pode estar cheio)
            boolean adicionado = player.getInventory().addItem(item);
            if (!adicionado) {
                nome.setTextFill(Color.web("#FF9944")); // Flash laranja: inventário cheio
                javafx.animation.PauseTransition flash =
                    new javafx.animation.PauseTransition(Duration.millis(600));
                flash.setOnFinished(ev -> nome.setTextFill(Color.web("#E8DFC0")));
                flash.play();
                desc.setText("Inventario cheio!");
                return;
            }

            // Desconta as moedas
            player.removeCoin(preco);

            // Atualiza o display de ouro na loja e na HUD
            lblOuro.setText("Ouro:  " + player.getCoin() + "  \uD83E\uDE99");
            if (hudManager != null) hudManager.atualizar(player);

            // Feedback visual: botão vira "COMPRADO!" brevemente
            btnComprar.setText("COMPRADO!");
            btnComprar.setStyle(
                "-fx-background-color: rgba(0,180,80,0.25);" +
                "-fx-border-color: #00CC66;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-text-fill: #00CC66;" +
                "-fx-padding: 6 14;"
            );

            // Após 1.5s, volta ao normal (ou desabilita se não tem mais moedas)
            javafx.animation.PauseTransition reset =
                new javafx.animation.PauseTransition(Duration.millis(1500));
            reset.setOnFinished(ev -> {
                btnComprar.setText("COMPRAR");
                if (player.getCoin() < preco) {
                    btnComprar.setDisable(true);
                    btnComprar.setStyle(estDisable);
                } else {
                    btnComprar.setStyle(estNormal);
                }
            });
            reset.play();
        });

        linha.getChildren().addAll(icone, infoBox, spacer, lblPreco, btnComprar);
        painel.getChildren().add(linha);
    }

    // =========================================================================
    // BOTÃO FECHAR
    // =========================================================================

    private static Button criarBotaoFechar() {
        Button btn = new Button("   FECHAR");
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);

        String estNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D8D0C0;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 20px;";
        String estHover =
            "-fx-background-color: rgba(0,210,210,0.15);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 20px;";

        btn.setStyle(estNormal);
        btn.setOnMouseEntered(e -> { btn.setStyle(estHover); btn.setText("\u203a  FECHAR"); });
        btn.setOnMouseExited(e ->  { btn.setStyle(estNormal); btn.setText("   FECHAR"); });
        return btn;
    }

    // =========================================================================
    // FECHAR COM FADE
    // =========================================================================

    private static void fechar(StackPane mainLayout, Runnable onClose) {
        if (!mainLayout.getChildren().isEmpty()) {
            javafx.scene.Node overlay =
                mainLayout.getChildren().get(mainLayout.getChildren().size() - 1);
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                mainLayout.getChildren().remove(overlay);
                onClose.run();
            });
            ft.play();
        } else {
            onClose.run();
        }
    }
}
