package com.RPG.TheLastRoar;

import java.io.File;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class StartScreen {

    public static StackPane createLayout(Runnable onNewGame, Runnable onContinue, Consumer<String> onLoadSlot) {
        StackPane root = new StackPane();
        
        Text title = new Text("THE LAST ROAR");
        title.setStyle("-fx-font-size: 60px; -fx-fill: white; -fx-font-weight: bold;");
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        title.setTranslateY(80);

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setTranslateY(50);

        Button btnNovo = criarBotao("NOVO JOGO", "#4CAF50");
        btnNovo.setOnAction(e -> onNewGame.run());

        Button btnContinuar = criarBotao("CONTINUAR", "#2196F3");
        if (getUltimoSave() == null) {
            btnContinuar.setDisable(true);
            btnContinuar.setText("CONTINUAR (Vazio)");
        } else {
            btnContinuar.setOnAction(e -> onContinue.run());
        }

        Button btnCarregar = criarBotao("CARREGAR JOGO", "#FF9800");

        HBox slotsBox = new HBox(15);
        slotsBox.setAlignment(Pos.CENTER);
        slotsBox.setVisible(false);

        // Verifica se o arquivo existe antes de habilitar o botão
        Button s1 = criarBotaoSlot("Slot 1"); 
        if (!new File("save1.json").exists()) { s1.setDisable(true); s1.setText("Slot 1 (Vazio)"); }
        s1.setOnAction(e -> onLoadSlot.accept("save1.json"));

        Button s2 = criarBotaoSlot("Slot 2"); 
        if (!new File("save2.json").exists()) { s2.setDisable(true); s2.setText("Slot 2 (Vazio)"); }
        s2.setOnAction(e -> onLoadSlot.accept("save2.json"));

        Button s3 = criarBotaoSlot("Slot 3"); 
        if (!new File("save3.json").exists()) { s3.setDisable(true); s3.setText("Slot 3 (Vazio)"); }
        s3.setOnAction(e -> onLoadSlot.accept("save3.json"));

        slotsBox.getChildren().addAll(s1, s2, s3);

        // Se não existir NENHUM save, bloqueia o botão pai também
        if (getUltimoSave() == null) {
            btnCarregar.setDisable(true);
            btnCarregar.setText("CARREGAR JOGO (Vazio)");
        } else {
            btnCarregar.setOnAction(e -> slotsBox.setVisible(!slotsBox.isVisible()));
        }

        Button btnSair = criarBotao("SAIR", "#f44336");
        btnSair.setOnAction(e -> javafx.application.Platform.exit());

        menuBox.getChildren().addAll(btnNovo, btnContinuar, btnCarregar, slotsBox, btnSair);

        root.getChildren().addAll(title, menuBox);
        root.setStyle("-fx-background-color: #2c3e50;");

        return root;
    }

    private static Button criarBotao(String texto, String corHex) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-font-size: 22px; -fx-padding: 10px 40px; -fx-background-color: " + corHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setPrefWidth(300);
        return btn;
    }

    private static Button criarBotaoSlot(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }

    public static String getUltimoSave() {
        File[] saves = {new File("save1.json"), new File("save2.json"), new File("save3.json")};
        File maisRecente = null;
        long maxTime = 0;
        for (File f : saves) {
            if (f.exists() && f.lastModified() > maxTime) {
                maxTime = f.lastModified();
                maisRecente = f;
            }
        }
        return maisRecente != null ? maisRecente.getName() : null;
    }
}