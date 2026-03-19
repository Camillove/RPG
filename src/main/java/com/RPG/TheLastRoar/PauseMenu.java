package com.RPG.TheLastRoar;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;

/**
 * Monta e gerencia o menu de pausa do jogo.
 * Expõe callbacks para que o App conecte as ações de salvar, carregar e sair.
 */
public class PauseMenu {

    private final VBox layout;

    private Button btnLoadSlot1;
    private Button btnLoadSlot2;
    private Button btnLoadSlot3;
    private Button btnLoadMenu;

    // Callbacks injetados pelo App
    private final Runnable onResume;
    private final Runnable onSaveSlot1;
    private final Runnable onSaveSlot2;
    private final Runnable onSaveSlot3;
    private final Runnable onLoadSlot1;
    private final Runnable onLoadSlot2;
    private final Runnable onLoadSlot3;
    private final Runnable onExit;

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

        layout = construir();
    }

    private VBox construir() {
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        menu.setVisible(false);

        Label title = new Label("JOGO PAUSADO");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));

        // Botão Voltar
        Button btnResume = criarBotao("Voltar ao Jogo", "/images/resume_icon.png",
            "-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        btnResume.setOnAction(e -> onResume.run());

        // Slots de Salvar
        HBox boxSalvar = new HBox(10);
        boxSalvar.setAlignment(Pos.CENTER);
        boxSalvar.setVisible(false);

        Button s1 = criarBotao("Salvar Slot 1", "/images/save_icon.png", null);
        s1.setOnAction(e -> onSaveSlot1.run());
        Button s2 = criarBotao("Salvar Slot 2", "/images/save_icon.png", null);
        s2.setOnAction(e -> onSaveSlot2.run());
        Button s3 = criarBotao("Salvar Slot 3", "/images/save_icon.png", null);
        s3.setOnAction(e -> onSaveSlot3.run());
        boxSalvar.getChildren().addAll(s1, s2, s3);

        Button btnSave = criarBotao("Salvar Jogo", "/images/save_menu_icon.png",
            "-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnSave.setOnAction(e -> boxSalvar.setVisible(!boxSalvar.isVisible()));

        // Slots de Carregar
        HBox boxCarregar = new HBox(10);
        boxCarregar.setAlignment(Pos.CENTER);
        boxCarregar.setVisible(false);

        btnLoadSlot1 = criarBotao("Carregar Slot 1", "/images/load_icon.png", null);
        btnLoadSlot1.setOnAction(e -> onLoadSlot1.run());
        btnLoadSlot2 = criarBotao("Carregar Slot 2", "/images/load_icon.png", null);
        btnLoadSlot2.setOnAction(e -> onLoadSlot2.run());
        btnLoadSlot3 = criarBotao("Carregar Slot 3", "/images/load_icon.png", null);
        btnLoadSlot3.setOnAction(e -> onLoadSlot3.run());
        boxCarregar.getChildren().addAll(btnLoadSlot1, btnLoadSlot2, btnLoadSlot3);

        btnLoadMenu = criarBotao("Carregar Jogo", "/images/load_menu_icon.png",
            "-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        btnLoadMenu.setOnAction(e -> boxCarregar.setVisible(!boxCarregar.isVisible()));

        // Botão Sair
        Button btnExit = criarBotao("Sair para o Menu", "/images/air.png",
            "-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        btnExit.setOnAction(e -> onExit.run());

        menu.getChildren().addAll(title, btnResume, btnSave, boxSalvar, btnLoadMenu, boxCarregar, btnExit);
        return menu;
    }

    /**
     * Atualiza o estado dos botões de Load conforme os saves existentes.
     */
    public void atualizarBotoesLoad(boolean slot1, boolean slot2, boolean slot3) {
        btnLoadSlot1.setDisable(!slot1);
        btnLoadSlot1.setText(slot1 ? "Carregar Slot 1" : "Slot 1 (Vazio)");
        btnLoadSlot2.setDisable(!slot2);
        btnLoadSlot2.setText(slot2 ? "Carregar Slot 2" : "Slot 2 (Vazio)");
        btnLoadSlot3.setDisable(!slot3);
        btnLoadSlot3.setText(slot3 ? "Carregar Slot 3" : "Slot 3 (Vazio)");

        btnLoadMenu.setDisable(!slot1 && !slot2 && !slot3);
        btnLoadMenu.setText((!slot1 && !slot2 && !slot3) ? "Carregar Jogo (Vazio)" : "Carregar Jogo");
    }

    public void setVisible(boolean visible) {
        layout.setVisible(visible);
    }

    public VBox getLayout() {
        return layout;
    }

    // -------------------------------------------------------
    // Utilitário interno de criação de botão com ícone
    // -------------------------------------------------------

    private Button criarBotao(String texto, String imagePath, String estiloExtra) {
        Button btn = new Button(texto);

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                ImageView icon = new ImageView(img);
                icon.setFitWidth(24);
                icon.setFitHeight(24);
                btn.setGraphic(icon);
            } catch (Exception e) {
                System.out.println("Aviso: Imagem do botão não encontrada: " + imagePath);
            }
        }

        String estiloBase = "-fx-font-size: 14px; -fx-padding: 5px 15px;" +
                            "-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;";
        btn.setStyle(estiloExtra != null ? estiloExtra : estiloBase);
        return btn;
    }
}
