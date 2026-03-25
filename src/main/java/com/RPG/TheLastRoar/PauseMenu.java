package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * ============================================================
 * PauseMenu.java — Menu de pausa estilo "Ruined King"
 * ============================================================
 *
 * VISUAL: Overlay escuro semitransparente sobre o jogo +
 *         título "PAUSADO" em dourado + lista vertical de
 *         opções idêntica ao StartScreen (seta ciano + hover).
 *
 * ESTRUTURA DO LAYOUT (camadas no StackPane do App):
 *
 *   ┌──────────────────────────────────────┐
 *   │  [JOGO — visível mas escurecido]     │  ← overlay rgba(0,0,0,0.75)
 *   │                                      │
 *   │  ── PAUSADO ───────────────────────  │  ← título + linha decorativa
 *   │                                      │
 *   │   › VOLTAR AO JOGO                   │  ← item ativo (ciano)
 *   │     SALVAR JOGO                      │
 *   │       [Slot 1] [Slot 2] [Slot 3]     │  ← sub-slots recolhidos
 *   │     CARREGAR JOGO                    │
 *   │       [Slot 1] [Slot 2] [Slot 3]     │
 *   │     SAIR PARA O MENU                 │
 *   └──────────────────────────────────────┘
 *
 * MUDANÇAS em relação à versão anterior:
 *   - Removidos botões coloridos (verde/azul/laranja/vermelho)
 *   - Adotada a paleta monocromática ciano/escuro do Ruined King
 *   - Animação de fade ao expandir/recolher sub-slots
 *   - Overlay mais sólido (0.75 em vez de 0.8, com leve blur visual)
 */
public class PauseMenu {

    // ─── Referências aos botões de Load (para habilitar/desabilitar) ──────────
    private Button btnLoadSlot1;
    private Button btnLoadSlot2;
    private Button btnLoadSlot3;

    // ─── Layout raiz do menu de pausa ─────────────────────────────────────────
    private final VBox layout;

    // ─── Callbacks injetados pelo App ─────────────────────────────────────────
    private final Runnable onResume;
    private final Runnable onSaveSlot1;
    private final Runnable onSaveSlot2;
    private final Runnable onSaveSlot3;
    private final Runnable onLoadSlot1;
    private final Runnable onLoadSlot2;
    private final Runnable onLoadSlot3;
    private final Runnable onExit;

    // ─── Paleta (igual ao StartScreen para consistência) ─────────────────────
    private static final Color COR_TEXTO_NORMAL   = Color.web("#D8D0C0");
    private static final Color COR_TEXTO_DISABLED = Color.web("#555040");

    // =========================================================================
    // CONSTRUTOR
    // =========================================================================

    /**
     * Constrói o PauseMenu injetando todos os callbacks.
     * O layout resultante deve ser adicionado ao StackPane do App.
     */
    public PauseMenu(
            Runnable onResume,
            Runnable onSaveSlot1, Runnable onSaveSlot2, Runnable onSaveSlot3,
            Runnable onLoadSlot1, Runnable onLoadSlot2, Runnable onLoadSlot3,
            Runnable onExit) {

        this.onResume    = onResume;
        this.onSaveSlot1 = onSaveSlot1;
        this.onSaveSlot2 = onSaveSlot2;
        this.onSaveSlot3 = onSaveSlot3;
        this.onLoadSlot1 = onLoadSlot1;
        this.onLoadSlot2 = onLoadSlot2;
        this.onLoadSlot3 = onLoadSlot3;
        this.onExit      = onExit;

        this.layout = construir();
    }

    // =========================================================================
    // MONTAGEM DO LAYOUT
    // =========================================================================

    /**
     * Constrói e retorna o VBox principal do menu de pausa.
     * Este VBox é invisível por padrão (setVisible(false) no App).
     */
    private VBox construir() {
        // ── VBox raiz: ocupa toda a tela (via StackPane) ───────────────────────
        VBox menu = new VBox(0);
        menu.setAlignment(Pos.CENTER_LEFT);

        // Overlay escuro semitransparente — deixa o jogo visível ao fundo
        menu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.76);");
        menu.setVisible(false);

        // Painel interno com o conteúdo real (título + botões)
        // Deslocado para a esquerda como no Ruined King
        VBox painel = new VBox(0);
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setPadding(new Insets(0, 0, 0, 120)); // margem esquerda

        // ── Título "PAUSADO" ──────────────────────────────────────────────────
        Text titulo = new Text("PAUSADO");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 52));
        titulo.setFill(Color.web("#F0E6C0"));

        DropShadow sombraTitulo = new DropShadow();
        sombraTitulo.setColor(Color.web("#C8A000", 0.8));
        sombraTitulo.setRadius(24);
        sombraTitulo.setSpread(0.12);
        titulo.setEffect(sombraTitulo);

        VBox.setMargin(titulo, new Insets(0, 0, 6, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa dourada ──────────────────────────────────────────
        Line separador = new Line(0, 0, 260, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new Insets(4, 0, 28, 0));
        painel.getChildren().add(separador);

        // ── Botão: VOLTAR AO JOGO ─────────────────────────────────────────────
        Button btnVoltar = criarBotaoMenu("VOLTAR AO JOGO", true);
        btnVoltar.setOnAction(e -> onResume.run());
        painel.getChildren().add(btnVoltar);

        // ── Botão: SALVAR JOGO + sub-slots ────────────────────────────────────
        HBox boxSalvar = criarBoxSlotsSalvar();
        boxSalvar.setVisible(false);

        Button btnSalvar = criarBotaoMenu("SALVAR JOGO", false);
        btnSalvar.setOnAction(e -> toggleVisibilidade(boxSalvar));
        painel.getChildren().add(btnSalvar);
        painel.getChildren().add(boxSalvar);

        // ── Botão: CARREGAR JOGO + sub-slots ──────────────────────────────────
        HBox boxCarregar = criarBoxSlotsCarregar();
        boxCarregar.setVisible(false);

        Button btnCarregar = criarBotaoMenu("CARREGAR JOGO", false);
        btnCarregar.setOnAction(e -> toggleVisibilidade(boxCarregar));
        painel.getChildren().add(btnCarregar);
        painel.getChildren().add(boxCarregar);

        // ── Botão: SAIR PARA O MENU ───────────────────────────────────────────
        Button btnSair = criarBotaoMenu("SAIR PARA O MENU", false);
        btnSair.setOnAction(e -> onExit.run());
        painel.getChildren().add(btnSair);

        menu.getChildren().add(painel);
        return menu;
    }

    // =========================================================================
    // HELPERS DE CRIAÇÃO DE BOTÕES
    // =========================================================================

    /**
     * Cria um botão do menu de pausa no estilo Ruined King.
     * Idêntico ao StartScreen.criarBotaoMenu().
     *
     * @param texto     Rótulo em caixa alta
     * @param destaque  Se true, já começa com o visual de "selecionado" (ciano)
     */
    private static Button criarBotaoMenu(String texto, boolean destaque) {
        Button btn = new Button((destaque ? "›  " : "   ") + texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 22));
        btn.setPrefWidth(380);
        btn.setPrefHeight(52);
        btn.setAlignment(Pos.CENTER_LEFT);

        // ── Estilos reutilizáveis ─────────────────────────────────────────────
        String estiloNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + toHex(COR_TEXTO_NORMAL) + ";" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 22px;";

        String estiloHover =
            "-fx-background-color: rgba(0, 210, 210, 0.15);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-left-color: #00E5E5;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-font-size: 22px;";

        // Estado inicial
        btn.setStyle(destaque ? estiloHover : estiloNormal);

        // ── Interações de hover ───────────────────────────────────────────────
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(estiloHover);
                // Adiciona a seta se ainda não estiver
                String t = btn.getText().strip().replace("›", "").strip();
                btn.setText("›  " + t);
                btn.setTextFill(Color.WHITE);
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(estiloNormal);
                String t = btn.getText().strip().replace("›", "").strip();
                btn.setText("   " + t);
                btn.setTextFill(COR_TEXTO_NORMAL);
            }
        });

        return btn;
    }

    /**
     * Cria o painel horizontal com os 3 botões de SALVAR (Slot 1, 2, 3).
     * Sempre habilitados (o jogador pode sobrescrever qualquer slot).
     */
    private HBox criarBoxSlotsSalvar() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(4, 0, 8, 64)); // indentação para indicar sub-menu

        String[] labels   = {"Slot 1", "Slot 2", "Slot 3"};
        Runnable[] acoes  = {onSaveSlot1, onSaveSlot2, onSaveSlot3};

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Button slot = criarBotaoSlot(labels[i], true);
            slot.setOnAction(e -> acoes[idx].run());
            box.getChildren().add(slot);
        }

        return box;
    }

    /**
     * Cria o painel horizontal com os 3 botões de CARREGAR (Slot 1, 2, 3).
     * Os botões são guardados como campos para poder habilitá-los/desabilitá-los
     * depois via {@link #atualizarBotoesLoad}.
     */
    private HBox criarBoxSlotsCarregar() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(4, 0, 8, 64));

        // Cria e guarda as referências para atualização posterior
        btnLoadSlot1 = criarBotaoSlot("Slot 1", false); // começa desabilitado
        btnLoadSlot2 = criarBotaoSlot("Slot 2", false);
        btnLoadSlot3 = criarBotaoSlot("Slot 3", false);

        btnLoadSlot1.setOnAction(e -> onLoadSlot1.run());
        btnLoadSlot2.setOnAction(e -> onLoadSlot2.run());
        btnLoadSlot3.setOnAction(e -> onLoadSlot3.run());

        box.getChildren().addAll(btnLoadSlot1, btnLoadSlot2, btnLoadSlot3);
        return box;
    }

    /**
     * Cria um botão de slot (pequeno, estilo ciano/escuro).
     *
     * @param label    Texto do botão (ex: "Slot 1")
     * @param ativo    Se true, começa habilitado com borda ciano
     */
    private static Button criarBotaoSlot(String label, boolean ativo) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        btn.setDisable(!ativo);

        String corBorda = ativo ? "#00E5E5" : "#443E30";
        String corTexto = ativo ? "#C0B890" : "#443E30";

        String estiloBase =
            "-fx-background-color: rgba(0,0,0,0.4);" +
            "-fx-border-color: " + corBorda + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-text-fill: " + corTexto + ";" +
            "-fx-padding: 6 16;" +
            "-fx-cursor: " + (ativo ? "hand" : "default") + ";";

        btn.setStyle(estiloBase);

        if (ativo) {
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0, 210, 210, 0.2);" +
                "-fx-border-color: #00E5E5;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 3;" +
                "-fx-background-radius: 3;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 6 16;" +
                "-fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(estiloBase));
        }

        return btn;
    }

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    /**
     * Atualiza o estado visual dos botões de carregar conforme os saves existentes.
     * Chamado pelo App antes de exibir o menu de pausa.
     *
     * @param slot1 true se save1.json existe
     * @param slot2 true se save2.json existe
     * @param slot3 true se save3.json existe
     */
    public void atualizarBotoesLoad(boolean slot1, boolean slot2, boolean slot3) {
        atualizarSlotLoad(btnLoadSlot1, "Slot 1", slot1);
        atualizarSlotLoad(btnLoadSlot2, "Slot 2", slot2);
        atualizarSlotLoad(btnLoadSlot3, "Slot 3", slot3);
    }

    /**
     * Atualiza visualmente um único botão de slot de carregamento.
     */
    private static void atualizarSlotLoad(Button btn, String label, boolean existe) {
        btn.setDisable(!existe);
        btn.setText(existe ? label : label + " (Vazio)");

        String corBorda = existe ? "#00E5E5" : "#443E30";
        String corTexto = existe ? "#C0B890" : "#443E30";

        String estilo =
            "-fx-background-color: rgba(0,0,0,0.4);" +
            "-fx-border-color: " + corBorda + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-text-fill: " + corTexto + ";" +
            "-fx-padding: 6 16;" +
            "-fx-cursor: " + (existe ? "hand" : "default") + ";";

        btn.setStyle(estilo);

        // Re-registra hover apenas se existir save
        if (existe) {
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0, 210, 210, 0.2);" +
                "-fx-border-color: #00E5E5;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 3;" +
                "-fx-background-radius: 3;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 6 16;" +
                "-fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(estilo));
        }
    }

    /**
     * Exibe ou esconde o menu de pausa.
     *
     * @param visible true = mostrar, false = esconder
     */
    public void setVisible(boolean visible) {
        if (visible) {
            // Aparece com fade in suave
            layout.setOpacity(0);
            layout.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(250), layout);
            ft.setToValue(1);
            ft.play();
        } else {
            layout.setVisible(false);
        }
    }

    /**
     * Retorna o VBox raiz para ser adicionado ao StackPane do App.
     */
    public VBox getLayout() {
        return layout;
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    /**
     * Alterna visibilidade de um painel de slots com animação de fade.
     */
    private static void toggleVisibilidade(HBox box) {
        if (box.isVisible()) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), box);
            ft.setToValue(0);
            ft.setOnFinished(e -> box.setVisible(false));
            ft.play();
        } else {
            box.setOpacity(0);
            box.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), box);
            ft.setToValue(1);
            ft.play();
        }
    }

    /**
     * Converte Color JavaFX para string hex CSS.
     */
    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }
}
