package com.RPG.TheLastRoar;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * StartScreen.java - A TELA INICIAL DO JOGO
 * 
 * O QUE FAZ:
 * Cria e mostra a tela de menu inicial
 * - Mostra uma imagem de fundo
 * - Mostra o título do jogo em letras grandes
 * - Mostra um botão "INICIAR" para começar o jogo
 * - Se adapta automaticamente ao tamanho da tela do usuário (responsivo)
 * 
 * COMO USAR:
 * StartScreen menu = new StartScreen();
 * Scene cenaMenu = menu.createStartScene(stage, mapScene);
 * 
 * MODIFICAR:
 * - Título: Mude "THE LAST ROAR" para outro nome
 * - Cores: Mude a cor do texto ou do botão
 * - Fundo: Mude o arquivo da imagem (start.png)
 * - Posição: Mude o Pos.CENTER para Pos.TOP, Pos.BOTTOM, etc
 */
public class StartScreen {

    /**
     * createStartScene()
     * Cria a cena do menu inicial
     * 
     * Parâmetros:
     * - stage: A janela principal (usada para trocar de cena)
     * - mapScene: A cena do mapa (mostrada quando clicar em "INICIAR")
     * 
     * Retorno:
     * - Uma Scene (cena) pronta para ser exibida
     * 
     * Exemplo:
     * Scene menu = startScreen.createStartScene(stage, mapScene);
     * stage.setScene(menu);
     */
    public Scene createStartScene(Stage stage, Scene mapScene) {

        // =========== DETECTA A RESOLUÇÃO DA TELA ==========
        // Pega informações da tela do usuário
        Rectangle2D screen = Screen.getPrimary().getBounds();
        double screenWidth = screen.getWidth();   // Largura da tela (ex: 1920)
        double screenHeight = screen.getHeight(); // Altura da tela (ex: 1080)

        // =========== CONTÁINER PRINCIPAL ==========
        // StackPane coloca elementos um em cima do outro
        // Primeiro elemento fica atrás, último fica na frente
        StackPane root = new StackPane();

        /**
         * FUNDO DA TELA
         * Carrega a imagem que fica nos fundos
         * 
         * MODIFICAR:
         * Mude "/images/start.png" para o nome de outra imagem
         */
        ImageView fundo = new ImageView(
                new Image(getClass().getResourceAsStream("/images/start.png"))
        );
        // Dimensiona a imagem para preencher toda a tela
        fundo.setFitWidth(screenWidth);    // Preenche a largura
        fundo.setFitHeight(screenHeight);  // Preenche a altura

        /**
         * TÍTULO DO JOGO
         * Texto grande e chamativo com efeito de sombra
         * 
         * MODIFICAR:
         * Mude "THE LAST ROAR" para outro título
         */
        Label titulo = new Label("THE LAST ROAR");
        
        // CSS para estilizar o título (cor, tamanho, sombra)
        titulo.setStyle(
                // Tamanho da fonte (10% da altura da tela)
                // Se a tela tem 1080px, o texto fica com ~108px
                "-fx-font-size: " + (screenHeight * 0.10) + "px;" +
                // Cor do texto (branco)
                "-fx-text-fill: white;" +
                // Texto em negrito
                "-fx-font-weight: bold;" +
                // Efeito de sombra (drop shadow)
                "-fx-effect: dropshadow(gaussian, black, 20, 0.8, 0, 0);"
        );

        /**
         * BOTÃO INICIAR
         * Botão azul com estilo personalizado
         * 
         * MODIFICAR:
         * Mude "INICIAR" para outro texto
         * Mude as cores (linear-gradient, -fx-text-fill)
         */
        Button startButton = new Button("INICIAR");

        // Define o tamanho do botão (percentual da tela)
        startButton.setPrefWidth(screenWidth * 0.15);   // 15% da largura da tela
        startButton.setPrefHeight(screenHeight * 0.07); // 7% da altura da tela

        // CSS para estilizar o botão
        startButton.setStyle(
                // Tamanho da fonte do botão (2.5% da altura)
                "-fx-font-size: " + (screenHeight * 0.025) + "px;" +
                // Cor do fundo (gradiente azul do claro para escuro)
                "-fx-background-color: linear-gradient(#5f9cff,#1e5fbf);" +
                // Cor do texto (branco)
                "-fx-text-fill: white;" +
                // Botões com cantos arredondados
                "-fx-background-radius: 10;"
        );

        /**
         * AÇÃO DO BOTÃO
         * O que acontece quando o jogador clica em "INICIAR"
         */
        startButton.setOnAction(e -> {
            // Troca para a cena do mapa
            stage.setScene(mapScene);
            
            // Dá foco para o mapa para que ele receba os input de teclado
            mapScene.getRoot().requestFocus();
        });

        /**
         * LAYOUT DO MENU
         * Organiza o título e botão no centro da tela
         */
        VBox menu = new VBox(40);  // VBox com 40px de espaço entre elementos
        menu.setAlignment(Pos.CENTER);  // Centraliza o conteúdo (horizontalmente e verticalmente)
        
        // Adiciona o título e o botão à VBox
        menu.getChildren().addAll(titulo, startButton);

        // Adiciona o fundo e o menu ao root (StackPane)
        // Ordem: fundo fica atrás, menu fica na frente
        root.getChildren().addAll(fundo, menu);

        // =========== CRIA A CENA ==========
        // Scene = a "tela" que vai ser mostrada na janela
        Scene scene = new Scene(root, screenWidth, screenHeight);

        /**
         * RESPONSIVIDADE
         * Faz a tela adaptar ao tamanho da janela
         * Se o usuário redimensionar a janela, tudo adapta automaticamente
         */
        // Bind = "liga" a largura do fundo à largura da cena
        fundo.fitWidthProperty().bind(scene.widthProperty());
        
        // Bind = "liga" a altura do fundo à altura da cena
        fundo.fitHeightProperty().bind(scene.heightProperty());
        
        // Permite que a imagem seja deformada se necessário
        fundo.setPreserveRatio(false);

        return scene;  // Retorna a cena pronta para ser usada
    }
}