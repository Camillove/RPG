package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

/**
 * ============================================================
 * InventoryScreen.java — Tela de Inventário do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Exibe os itens do inventário do jogador em uma grade visual,
 * permitindo equipar e desequipar espadas e armaduras.
 *
 * FLUXO DE USO:
 * 1. Jogador clica no ícone 🎒 (canto inferior direito)
 * 2. App.java chama InventoryScreen.open(...)
 * 3. Os timers do jogo são pausados automaticamente
 * 4. O jogador interage com os itens
 * 5. Ao fechar, App retoma os timers
 *
 * ESTRUTURA DO LAYOUT:
 *  ┌──────────────────────────────────────┐
 *  │  INVENTÁRIO ───────────────────────  │  ← título + linha dourada
 *  │                                      │
 *  │  [EQUIPADO]   Espada de Madeira      │  ← slot de espada atual
 *  │  [EQUIPADO]   (sem armadura)         │  ← slot de armadura atual
 *  │                                      │
 *  │  ITENS NO INVENTÁRIO                 │  ← grade de itens
 *  │  [Adaga] [Espada Longa] [Armadura]   │
 *  │                                      │
 *  │                    [FECHAR]          │
 *  └──────────────────────────────────────┘
 */
public class InventoryScreen {

    // =========================================================================
    // MÉTODO PRINCIPAL: Abre a tela de inventário como overlay
    // =========================================================================

    /**
     * Abre a tela de inventário sobre o jogo.
     *
     * @param mainLayout  O StackPane principal do jogo (onde o overlay é adicionado)
     * @param player      O personagem do jogador (para ler e modificar equipamentos)
     * @param onClose     Callback chamado quando o jogador fecha o inventário
     */
    public static void open(StackPane mainLayout, Character player, Runnable onClose) {

        // ── Constrói o painel de inventário ──────────────────────────────────
        VBox painelInventario = construirPainel(player, () -> {
            // Ao fechar, remove o overlay do layout principal
            fechar(mainLayout, onClose);
        });

        // ── Cria o overlay escuro semitransparente ────────────────────────────
        // O overlay ocupa toda a tela e escurece o jogo ao fundo
        StackPane overlay = new StackPane(painelInventario);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.80);");
        overlay.setAlignment(Pos.CENTER_LEFT); // Painel alinhado à esquerda (estilo Ruined King)

        // ── Animação de entrada com fade ──────────────────────────────────────
        overlay.setOpacity(0);
        mainLayout.getChildren().add(overlay); // Adiciona o overlay ao topo da pilha

        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setToValue(1.0);
        ft.play();
    }

    // =========================================================================
    // CONSTRUÇÃO DO PAINEL PRINCIPAL
    // =========================================================================

    /**
     * Constrói e retorna o VBox com todo o conteúdo do inventário.
     */
    private static VBox construirPainel(Character player, Runnable onClose) {

        // ── Painel externo: margens e posicionamento à esquerda ───────────────
        VBox painel = new VBox(0);
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setPadding(new Insets(0, 0, 0, 120)); // Margem esquerda (estilo Ruined King)

        // ── Título "INVENTÁRIO" ───────────────────────────────────────────────
        Text titulo = new Text("INVENTÁRIO");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 48));
        titulo.setFill(Color.web("#F0E6C0"));

        // Sombra dourada no título (igual ao PauseMenu)
        DropShadow sombraTitulo = new DropShadow();
        sombraTitulo.setColor(Color.web("#C8A000", 0.8));
        sombraTitulo.setRadius(24);
        sombraTitulo.setSpread(0.12);
        titulo.setEffect(sombraTitulo);
        VBox.setMargin(titulo, new Insets(0, 0, 6, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa dourada ──────────────────────────────────────────
        Line separador = new Line(0, 0, 280, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new Insets(4, 0, 24, 0));
        painel.getChildren().add(separador);

        // ── Seção: Equipamentos Atuais ────────────────────────────────────────
        Text labelEquipados = new Text("EQUIPADO");
        labelEquipados.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelEquipados.setFill(Color.web("#888070"));
        VBox.setMargin(labelEquipados, new Insets(0, 0, 8, 0));
        painel.getChildren().add(labelEquipados);

        // ── Slot de Espada Equipada ────────────────────────────────────────────
        // Mostra a espada que o jogador está usando atualmente
        HBox slotEspada = criarSlotEquipado(
            "⚔  Espada",                    // Tipo do slot
            player.getSword() != null        // Nome do item equipado (ou "Nenhuma")
                ? player.getSword().getName()
                : "Nenhuma",
            "#C8A000"                        // Cor dourada para armas
        );
        VBox.setMargin(slotEspada, new Insets(0, 0, 6, 0));
        painel.getChildren().add(slotEspada);

        // ── Slot de Armadura Equipada ──────────────────────────────────────────
        // Mostra a armadura equipada (pode ser nula no início)
        HBox slotArmadura = criarSlotEquipado(
            "🛡  Armadura",
            player.getEquippedArmor() != null
                ? player.getEquippedArmor().getName()
                : "Nenhuma",
            "#4A90D9"                        // Cor azul para armaduras
        );
        VBox.setMargin(slotArmadura, new Insets(0, 0, 24, 0));
        painel.getChildren().add(slotArmadura);

        // ── Seção: Itens no Inventário ────────────────────────────────────────
        Text labelItens = new Text("ITENS NO INVENTÁRIO");
        labelItens.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelItens.setFill(Color.web("#888070"));
        VBox.setMargin(labelItens, new Insets(0, 0, 10, 0));
        painel.getChildren().add(labelItens);

        // ── Grade de Itens ────────────────────────────────────────────────────
        // Exibe os itens do inventário em uma grade de 4 colunas
        GridPane grade = construirGradeItens(player, slotEspada, slotArmadura);
        VBox.setMargin(grade, new Insets(0, 0, 28, 0));
        painel.getChildren().add(grade);

        // ── Botão: FECHAR ─────────────────────────────────────────────────────
        Button btnFechar = criarBotaoFechar();
        btnFechar.setOnAction(e -> onClose.run());
        painel.getChildren().add(btnFechar);

        return painel;
    }

    // =========================================================================
    // GRADE DE ITENS DO INVENTÁRIO
    // =========================================================================

    /**
     * Constrói a grade visual com todos os itens do inventário.
     * Cada item é um botão clicável que permite equipar/desequipar.
     *
     * @param player       O personagem (fonte dos itens)
     * @param slotEspada   HBox do slot de espada equipada (para atualizar o texto)
     * @param slotArmadura HBox do slot de armadura equipada (para atualizar o texto)
     */
    private static GridPane construirGradeItens(Character player, HBox slotEspada, HBox slotArmadura) {
        GridPane grade = new GridPane();
        grade.setHgap(12); // Espaço horizontal entre os itens
        grade.setVgap(12); // Espaço vertical entre os itens

        List<Item> itens = player.getInventory().getItems();

        if (itens.isEmpty()) {
            // ── Mensagem caso o inventário esteja vazio ───────────────────────
            Label vazio = new Label("O inventário está vazio.");
            vazio.setFont(Font.font("Segoe UI", 16));
            vazio.setTextFill(Color.web("#888070"));
            grade.add(vazio, 0, 0);
            return grade;
        }

        // ── Cria um botão/card para cada item ────────────────────────────────
        for (int i = 0; i < itens.size(); i++) {
            Item item = itens.get(i);

            // Determina se o item já está equipado (para destacá-lo visualmente)
            boolean estaEquipado = verificarEquipado(player, item);

            // Cria o card visual do item
            VBox card = criarCardItem(item, estaEquipado);

            // ── Lógica de clique: equipar ou desequipar ───────────────────────
            card.setOnMouseClicked(e -> {
                if (item instanceof Sword espada) {
                    // --- É uma espada ---
                    if (player.getSword() == espada) {
                        // Já está equipada: DESEQUIPA (volta para null)
                        player.setSword(null);
                        atualizarTextoSlot(slotEspada, "Nenhuma");
                        card.setStyle(estiloCardNormal(item)); // Remove destaque
                    } else {
                        // Não está equipada: EQUIPA
                        player.setSword(espada);
                        atualizarTextoSlot(slotEspada, espada.getName());
                        // Remove destaque de todas as espadas antes de aplicar neste
                        resetarDestaqueEspadasNaGrade(grade, player);
                        card.setStyle(estiloCardEquipado(item));
                    }

                } else if (item instanceof Armor armadura) {
                    // --- É uma armadura ---
                    if (player.getEquippedArmor() == armadura) {
                        // Já está equipada: DESEQUIPA
                        player.setEquippedArmor(null);
                        atualizarTextoSlot(slotArmadura, "Nenhuma");
                        card.setStyle(estiloCardNormal(item));
                    } else {
                        // Não está equipada: EQUIPA
                        player.setEquippedArmor(armadura);
                        atualizarTextoSlot(slotArmadura, armadura.getName());
                        // Remove destaque de todas as armaduras antes de aplicar neste
                        resetarDestaqueArmadurasNaGrade(grade, player);
                        card.setStyle(estiloCardEquipado(item));
                    }
                }
            });

            // Posicionamento na grade: 4 itens por linha
            int coluna = i % 4;
            int linha  = i / 4;
            grade.add(card, coluna, linha);
        }

        return grade;
    }

    // =========================================================================
    // HELPERS DE VISUAL DOS CARDS
    // =========================================================================

    /**
     * Verifica se um item está atualmente equipado pelo jogador.
     */
    private static boolean verificarEquipado(Character player, Item item) {
        if (item instanceof Sword && player.getSword() == item) return true;
        if (item instanceof Armor && player.getEquippedArmor() == item) return true;
        return false;
    }

    /**
     * Cria o card visual de um item do inventário.
     * Cada card mostra o ícone do tipo, o nome e os atributos principais.
     */
    private static VBox criarCardItem(Item item, boolean equipado) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setPrefSize(130, 90);
        card.setMaxSize(130, 90);
        card.setStyle(equipado ? estiloCardEquipado(item) : estiloCardNormal(item));
        card.setCursor(javafx.scene.Cursor.HAND);

        // ── Ícone do tipo de item (emoji como texto) ──────────────────────────
        Label icone = new Label(obterIconeItem(item));
        icone.setFont(Font.font("Segoe UI Emoji", 22));

        // ── Nome do item ──────────────────────────────────────────────────────
        Label nome = new Label(item.getName());
        nome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nome.setTextFill(Color.web("#E8DFC0"));
        nome.setWrapText(true);
        nome.setMaxWidth(110);
        nome.setAlignment(Pos.CENTER);

        // ── Atributo principal (dano ou resistência) ──────────────────────────
        String atributo = obterAtributoItem(item);
        Label lblAtributo = new Label(atributo);
        lblAtributo.setFont(Font.font("Segoe UI", 11));
        lblAtributo.setTextFill(Color.web("#888070"));

        // ── Badge "EQUIPADO" (visível apenas se equipado) ─────────────────────
        if (equipado) {
            Label badge = new Label("EQUIPADO");
            badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            badge.setTextFill(Color.web("#00E5E5"));
            badge.setStyle("-fx-background-color: rgba(0,200,200,0.15); -fx-padding: 2 6; -fx-background-radius: 4;");
            card.getChildren().addAll(icone, nome, lblAtributo, badge);
        } else {
            card.getChildren().addAll(icone, nome, lblAtributo);
        }

        // ── Efeito de hover ───────────────────────────────────────────────────
        String estiloBase = equipado ? estiloCardEquipado(item) : estiloCardNormal(item);
        String estiloHover = estiloCardHover(item);
        card.setOnMouseEntered(e -> { if (!card.getStyle().equals(estiloCardEquipado(item))) card.setStyle(estiloHover); });
        card.setOnMouseExited(e ->  { card.setStyle(verificarEquipado(null, item) ? estiloCardEquipado(item) : estiloBase); });
        // Nota: hover não remove o destaque "equipado"
        card.setOnMouseEntered(e -> card.setOpacity(0.85));
        card.setOnMouseExited(e ->  card.setOpacity(1.0));

        return card;
    }

    /** Retorna o emoji/ícone correspondente ao tipo de item. */
    private static String obterIconeItem(Item item) {
        if (item instanceof Sword)  return "⚔";
        if (item instanceof Armor)  return "🛡";
        if (item instanceof Potion) return "🧪";
        return "📦";
    }

    /** Retorna uma string com o atributo principal do item (ex: "Dano: 8"). */
    private static String obterAtributoItem(Item item) {
        if (item instanceof Sword s)  return "Dano: " + s.getDamage() + " | " + s.getType();
        if (item instanceof Armor a)  return "Defesa: " + a.getResistance();
        if (item instanceof Potion p) return "Cura: " + p.getHealedLife() + " HP";
        return "Valor: " + item.getValue();
    }

    // ─── Estilos dos cards ─────────────────────────────────────────────────

    private static String estiloCardNormal(Item item) {
        String corBorda = corBordaItem(item);
        return "-fx-background-color: rgba(20,20,20,0.75);" +
               "-fx-border-color: " + corBorda + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;";
    }

    private static String estiloCardEquipado(Item item) {
        return "-fx-background-color: rgba(0,180,180,0.18);" +
               "-fx-border-color: #00E5E5;" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;";
    }

    private static String estiloCardHover(Item item) {
        return "-fx-background-color: rgba(50,50,50,0.85);" +
               "-fx-border-color: #888070;" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;";
    }

    /** Cor da borda do card de acordo com o tipo do item. */
    private static String corBordaItem(Item item) {
        if (item instanceof Sword)  return "#8B7028"; // Dourado para espadas
        if (item instanceof Armor)  return "#2A5A8B"; // Azul para armaduras
        if (item instanceof Potion) return "#2A7A3A"; // Verde para poções
        return "#443E30";
    }

    // ─── Reset de destaque na grade (remove borda ciano de itens desequipados) ──

    /**
     * Percorre a grade e remove o destaque de todos os cards de espada
     * exceto o que foi recém-equipado. Chamado antes de equipar uma nova espada.
     */
    private static void resetarDestaqueEspadasNaGrade(GridPane grade, Character player) {
        grade.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                // Obtém o item correspondente a este card via tag de usuário
                Object userData = card.getUserData();
                if (userData instanceof Sword espada && espada != player.getSword()) {
                    card.setStyle(estiloCardNormal(espada));
                }
            }
        });
    }

    /** Mesmo que resetarDestaqueEspadasNaGrade, mas para armaduras. */
    private static void resetarDestaqueArmadurasNaGrade(GridPane grade, Character player) {
        grade.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                Object userData = card.getUserData();
                if (userData instanceof Armor armadura && armadura != player.getEquippedArmor()) {
                    card.setStyle(estiloCardNormal(armadura));
                }
            }
        });
    }

    // =========================================================================
    // SLOT DE ITEM EQUIPADO (MOSTRA O QUE ESTÁ EQUIPADO NO MOMENTO)
    // =========================================================================

    /**
     * Cria uma linha visual mostrando o slot de equipamento atual.
     *
     * @param tipoSlot   Texto do tipo (ex: "⚔  Espada")
     * @param nomeItem   Nome do item equipado (ou "Nenhuma")
     * @param corDestaque Cor da borda lateral do slot
     */
    private static HBox criarSlotEquipado(String tipoSlot, String nomeItem, String corDestaque) {
        HBox slot = new HBox(14);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setStyle(
            "-fx-background-color: rgba(20,20,20,0.7);" +
            "-fx-border-color: " + corDestaque + " transparent transparent transparent;" + // Apenas borda esquerda
            "-fx-border-width: 0 0 0 3;" +
            "-fx-padding: 10 16;" +
            "-fx-background-radius: 6;"
        );
        slot.setMaxWidth(380);

        // ── Nome do tipo de slot (ex: "⚔  Espada") ───────────────────────────
        Label lblTipo = new Label(tipoSlot);
        lblTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTipo.setTextFill(Color.web("#888070"));
        lblTipo.setMinWidth(110);

        // ── Nome do item equipado ─────────────────────────────────────────────
        Label lblNome = new Label(nomeItem);
        lblNome.setId("nomeEquipado"); // ID para atualizar o texto depois
        lblNome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblNome.setTextFill(Color.web("#E8DFC0"));

        slot.getChildren().addAll(lblTipo, lblNome);
        return slot;
    }

    /**
     * Atualiza o texto do nome do item em um slot de equipamento.
     * Busca o Label pelo ID "nomeEquipado" dentro do HBox do slot.
     */
    private static void atualizarTextoSlot(HBox slot, String novoNome) {
        slot.getChildren().stream()
            .filter(n -> n instanceof Label lbl && "nomeEquipado".equals(lbl.getId()))
            .map(n -> (Label) n)
            .findFirst()
            .ifPresent(lbl -> lbl.setText(novoNome));
    }

    // =========================================================================
    // BOTÃO FECHAR
    // =========================================================================

    /** Cria e estiliza o botão de fechar o inventário. */
    private static Button criarBotaoFechar() {
        Button btn = new Button("   FECHAR");
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);

        String estiloNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D8D0C0;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 20px;";

        String estiloHover =
            "-fx-background-color: rgba(0, 210, 210, 0.15);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-left-color: #00E5E5;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-font-size: 20px;";

        btn.setStyle(estiloNormal);
        btn.setOnMouseEntered(e -> { btn.setStyle(estiloHover); btn.setText("›  FECHAR"); });
        btn.setOnMouseExited(e ->  { btn.setStyle(estiloNormal); btn.setText("   FECHAR"); });

        return btn;
    }

    // =========================================================================
    // FECHAR COM ANIMAÇÃO
    // =========================================================================

    /**
     * Fecha o inventário com animação de fade out e remove o overlay do layout.
     *
     * @param mainLayout O StackPane principal (para remover o overlay)
     * @param onClose    Callback chamado após o fechamento (para retomar os timers)
     */
    private static void fechar(StackPane mainLayout, Runnable onClose) {
        // O overlay é sempre o último filho adicionado ao mainLayout
        if (!mainLayout.getChildren().isEmpty()) {
            javafx.scene.Node overlay = mainLayout.getChildren().get(mainLayout.getChildren().size() - 1);

            FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                mainLayout.getChildren().remove(overlay); // Remove o overlay
                onClose.run();                            // Retoma os timers do jogo
            });
            ft.play();
        } else {
            onClose.run();
        }
    }
}
