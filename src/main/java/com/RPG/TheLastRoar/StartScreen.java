package com.RPG.TheLastRoar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class StartScreen {

    private static final Color COR_TEXTO_ATIVO = Color.web("#00E5E5");
    private static final Color COR_TEXTO_NORMAL = Color.web("#D8D0C0");
    private static final Color COR_TEXTO_DISABLED = Color.web("#555040");

    // Duração da animação de zoom/fade do fundo
    private static final Duration DURACO_TRANSICAO_FUNDO = Duration.millis(1200);

    public static StackPane createLayout(
            Runnable onNewGame,
            Runnable onContinue,
            Consumer<String> onLoadSlot) {

        StackPane root = new StackPane();
        // Garante que o root preencha a tela para o clip funcionar
        root.setPrefSize(1280, 720); 

        // Cria um clip para garantir que o zoom da imagem não transborde a janela
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        // =====================================================================
        // PREPARAÇÃO DOS FUNDOS (IMAGEVIEWS PARA ANIMAÇÃO)
        // =====================================================================
        
        ImageView fundoMenu = new ImageView();
        ImageView fundoSplash = new ImageView();

        // Carrega as imagens
        try {
            Image imgSplash = new Image(StartScreen.class.getResourceAsStream("/images/background.png"));
            Image imgMenu = new Image(StartScreen.class.getResourceAsStream("/images/background_menu.png"));
            
            fundoSplash.setImage(imgSplash);
            fundoMenu.setImage(imgMenu);
        } catch (Exception e) {
            System.out.println("Erro ao carregar imagens de fundo. Verifique /images/background.png e background_menu.png");
            e.printStackTrace();
        }

        // Configura ambos para preencherem a tela (comportamento 'cover')
        configurarImageViewCover(fundoSplash, root);
        configurarImageViewCover(fundoMenu, root);

        // Adiciona os fundos: Menu fica atrás, Splash fica na frente
        root.getChildren().addAll(fundoMenu, fundoSplash);

        // =====================================================================
        // ESTADO 1: TELA DE SPLASH (Texto movido para baixo)
        // =====================================================================
        VBox splashScreenContainer = new VBox();
        splashScreenContainer.setAlignment(Pos.BOTTOM_CENTER); // Alinha o conteúdo no fundo
        splashScreenContainer.setPadding(new Insets(0, 0, 300, 0)); // Margem de 100px do bottom
        splashScreenContainer.setMouseTransparent(true); // Permite clicar através do VBox vazio

        Text pressAnyKey = new Text("Pressione qualquer tecla para continuar");
        pressAnyKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        pressAnyKey.setFill(Color.web("#FFFFFF", 0.8));
        
        // Efeito pulsante no texto
        FadeTransition pulsar = new FadeTransition(Duration.seconds(1.2), pressAnyKey);
        pulsar.setFromValue(0.8);
        pulsar.setToValue(0.2);
        pulsar.setAutoReverse(true);
        pulsar.setCycleCount(Animation.INDEFINITE);
        pulsar.play();

        splashScreenContainer.getChildren().add(pressAnyKey);

        // =====================================================================
        // ESTADO 2: MENU INICIAL (Fica escondido no início)
        // =====================================================================
        VBox conteudoMenu = new VBox(0);
        conteudoMenu.setAlignment(Pos.CENTER_LEFT);
        conteudoMenu.setTranslateX(80);
        conteudoMenu.setTranslateY(-30);
        
        conteudoMenu.setOpacity(0); 
        conteudoMenu.setVisible(false);
        conteudoMenu.setDisable(true); 

        Line separador = new Line(0, 0, 280, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new javafx.geometry.Insets(10, 0, 30, 0));
        conteudoMenu.getChildren().add(separador);

        List<Button> menuItens = new ArrayList<>();
        int[] indexSelecionado = {0}; 
        boolean[] inSplashScreen = {true}; 

        Button btnNovo = criarBotaoMenu("NOVO JOGO");
        btnNovo.setOnAction(e -> onNewGame.run());
        menuItens.add(btnNovo);

        boolean temSave = (getUltimoSave() != null);
        Button btnContinuar = criarBotaoMenu("CONTINUAR");
        if (!temSave) btnContinuar.setDisable(true);
        else btnContinuar.setOnAction(e -> onContinue.run());
        menuItens.add(btnContinuar);

        HBox boxSlots = criarBoxSlots(onLoadSlot);
        boxSlots.setVisible(false);

        Button btnCarregar = criarBotaoMenu("CARREGAR JOGO");
        if (!temSave) btnCarregar.setDisable(true);
        else btnCarregar.setOnAction(e -> toggleVisibilidade(boxSlots));
        menuItens.add(btnCarregar);

        Button btnSair = criarBotaoMenu("SAIR");
        btnSair.setOnAction(e -> javafx.application.Platform.exit());
        menuItens.add(btnSair);

        conteudoMenu.getChildren().addAll(btnNovo, btnContinuar, btnCarregar, boxSlots, btnSair);

        Runnable atualizarVisual = () -> {
            for (int i = 0; i < menuItens.size(); i++) {
                Button b = menuItens.get(i);
                aplicarEstiloBotao(b, i == indexSelecionado[0]);
            }
        };

        for (int i = 0; i < menuItens.size(); i++) {
            int currentIndex = i;
            Button b = menuItens.get(i);
            b.setOnMouseEntered(e -> {
                if (!b.isDisabled() && !inSplashScreen[0]) {
                    indexSelecionado[0] = currentIndex;
                    atualizarVisual.run();
                }
            });
            b.setOnMousePressed(e -> {
                if (!b.isDisabled() && !inSplashScreen[0]) b.setTranslateY(2);
            });
            b.setOnMouseReleased(e -> {
                if (!b.isDisabled() && !inSplashScreen[0]) b.setTranslateY(0);
            });
        }

        HBox rodape = criarRodape();
        rodape.setOpacity(0);
        rodape.setVisible(false);

        // =====================================================================
        // LÓGICA DE TRANSIÇÃO (Com animação de Zoom no fundo)
        // =====================================================================
        EventHandler<InputEvent> splashHandler = e -> {
            if (inSplashScreen[0]) {
                inSplashScreen[0] = false; 

                // 1. Animação do Fundo (Zoom In + Fade Out da Splash)
                FadeTransition fadeFundo = new FadeTransition(DURACO_TRANSICAO_FUNDO, fundoSplash);
                fadeFundo.setToValue(0);

                ScaleTransition zoomFundo = new ScaleTransition(DURACO_TRANSICAO_FUNDO, fundoSplash);
                zoomFundo.setFromX(1.0);
                zoomFundo.setFromY(1.0);
                zoomFundo.setToX(1.15); // Aumenta 15% (efeito mini-zoom)
                zoomFundo.setToY(1.15);

                ParallelTransition transicaoFundo = new ParallelTransition(fadeFundo, zoomFundo);
                transicaoFundo.setOnFinished(ev -> {
                    root.getChildren().remove(fundoSplash); // Remove para otimizar
                });
                transicaoFundo.play();

                // 2. Animação da UI (Original)
                iniciarTransicaoMenu(splashScreenContainer, conteudoMenu, rodape);
                
                e.consume(); 
            }
        };

        root.addEventFilter(MouseEvent.MOUSE_PRESSED, splashHandler);
        root.addEventFilter(KeyEvent.KEY_PRESSED, splashHandler);

        // =====================================================================
        // NAVEGAÇÃO DO MENU (Original)
        // =====================================================================
        root.setFocusTraversable(true); 
        root.setOnKeyPressed(e -> {
            if (inSplashScreen[0]) return; 

            int max = menuItens.size();
            switch (e.getCode()) {
                case UP:
                case W:
                    do {
                        indexSelecionado[0] = (indexSelecionado[0] - 1 + max) % max;
                    } while (menuItens.get(indexSelecionado[0]).isDisabled());
                    atualizarVisual.run();
                    break;
                case DOWN:
                case S:
                    do {
                        indexSelecionado[0] = (indexSelecionado[0] + 1) % max;
                    } while (menuItens.get(indexSelecionado[0]).isDisabled());
                    atualizarVisual.run();
                    break;
                case ENTER:
                case SPACE:
                    Button selecionado = menuItens.get(indexSelecionado[0]);
                    if (!selecionado.isDisabled()) {
                        selecionado.setTranslateY(2);
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(ev -> selecionado.setTranslateY(0));
                        pause.play();
                        selecionado.fire(); 
                    }
                    break;
                default:
                    break;
            }
        });

        while(menuItens.get(indexSelecionado[0]).isDisabled() && indexSelecionado[0] < menuItens.size() - 1) {
            indexSelecionado[0]++;
        }
        atualizarVisual.run();

        // Adiciona a UI sobre os fundos
        root.getChildren().addAll(conteudoMenu, rodape, splashScreenContainer);

        StackPane.setAlignment(conteudoMenu, Pos.CENTER_LEFT);
        StackPane.setAlignment(rodape, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(rodape, new javafx.geometry.Insets(0, 40, 30, 0));

        Platform.runLater(root::requestFocus);

        return root;
    }

    // =========================================================================
    // HELPERS PARA IMAGEM
    // =========================================================================
    
    // Simula o comportamento "background-size: cover" do CSS para ImageView
    // Simula o comportamento "background-size: cover" do CSS para ImageView
    private static void configurarImageViewCover(ImageView iv, StackPane container) {
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);

        // AS DUAS LINHAS DE .BIND() FORAM REMOVIDAS DAQUI PARA PARAR O ERRO

        // Lógica para garantir que a imagem sempre cubra a área, cortando excessos se necessário
        container.widthProperty().addListener((obs, oldVal, newVal) -> forceCover(iv, container));
        container.heightProperty().addListener((obs, oldVal, newVal) -> forceCover(iv, container));
        
        // Força a primeira atualização quando a tela for criada
        Platform.runLater(() -> forceCover(iv, container));
    }
    private static void forceCover(ImageView iv, StackPane container) {
        if (iv.getImage() == null) return;
        
        double containerW = container.getWidth();
        double containerH = container.getHeight();
        double imgW = iv.getImage().getWidth();
        double imgH = iv.getImage().getHeight();

        if (containerW == 0 || containerH == 0) return;

        double containerRatio = containerW / containerH;
        double imgRatio = imgW / imgH;

        if (containerRatio >= imgRatio) {
            // Container é mais largo que a imagem, fixa largura, corta altura
            iv.setFitWidth(containerW);
            iv.setFitHeight(0); // preserve ratio automaticamente
        } else {
            // Container é mais alto que a imagem, fixa altura, corta largura
            iv.setFitHeight(containerH);
            iv.setFitWidth(0); // preserve ratio automaticamente
        }
    }

    // =========================================================================
    // ANIMAÇÃO DE TRANSIÇÃO UI (Original - Apenas ajustado nomes)
    // =========================================================================
    private static void iniciarTransicaoMenu(VBox splashContainer, VBox menu, HBox rodape) {
        // Desaparece com o texto "Pressione..."
        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), splashContainer);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            splashContainer.setVisible(false); 
            
            // Libera o menu para ser clicado e visto
            menu.setVisible(true);
            menu.setDisable(false);
            rodape.setVisible(true);

            FadeTransition fadeInMenu = new FadeTransition(Duration.millis(800), menu);
            fadeInMenu.setFromValue(0);
            fadeInMenu.setToValue(1);
            fadeInMenu.play();

            FadeTransition fadeInRodape = new FadeTransition(Duration.millis(800), rodape);
            fadeInRodape.setFromValue(0);
            fadeInRodape.setToValue(1);
            fadeInRodape.play();
        });
        fadeOut.play();
    }

    // =========================================================================
    // HELPERS PRIVADOS (Mantidos exatamente iguais)
    // =========================================================================

    private static Rectangle criarOverlay() {
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.web("#000000", 0.5));
        overlay.widthProperty().bind(new javafx.beans.property.SimpleDoubleProperty(9999));  
        overlay.heightProperty().bind(new javafx.beans.property.SimpleDoubleProperty(9999));
        return overlay;
    }

    private static Text criarTitulo(int tamanhoFonte) {
        Text titulo = new Text("THE LAST ROAR");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, tamanhoFonte));
        titulo.setFill(Color.web("#F0E6C0"));

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#C8A000", 0.85));
        sombra.setRadius(30);
        sombra.setSpread(0.15);
        sombra.setOffsetX(0);
        sombra.setOffsetY(2);
        titulo.setEffect(sombra);

        VBox.setMargin(titulo, new javafx.geometry.Insets(0, 0, 5, 0));
        return titulo;
    }

    private static Button criarBotaoMenu(String texto) {
        Button btn = new Button("   " + texto);
        btn.setUserData(texto); 
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 22));
        btn.setPrefWidth(380);
        btn.setPrefHeight(52);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private static void aplicarEstiloBotao(Button btn, boolean isSelecionado) {
        String textoOriginal = (String) btn.getUserData();

        if (btn.isDisabled()) {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toHex(COR_TEXTO_DISABLED) + ";" +
                "-fx-padding: 0 0 0 20;" +
                "-fx-cursor: default;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 22px;"
            );
            btn.setText("   " + textoOriginal);
            btn.setTextFill(COR_TEXTO_DISABLED);

        } else if (isSelecionado) {
            btn.setStyle(
                "-fx-background-color: rgba(0, 210, 210, 0.15);" +
                "-fx-border-color: transparent;" +
                "-fx-border-left-color: #00E5E5;" +
                "-fx-border-width: 0 0 0 3;" +
                "-fx-padding: 0 0 0 20;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 22px;"
            );
            btn.setText("›  " + textoOriginal);
            btn.setTextFill(Color.WHITE);

        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toHex(COR_TEXTO_NORMAL) + ";" +
                "-fx-padding: 0 0 0 20;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 22px;"
            );
            btn.setText("   " + textoOriginal);
            btn.setTextFill(COR_TEXTO_NORMAL);
        }
    }

    private static HBox criarBoxSlots(Consumer<String> onLoadSlot) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new javafx.geometry.Insets(4, 0, 8, 44)); 

        String[] arquivos = {"save1.json", "save2.json", "save3.json"};
        String[] labels   = {"Slot 1", "Slot 2", "Slot 3"};

        for (int i = 0; i < 3; i++) {
            final String arquivo = arquivos[i];
            boolean existe = new File(arquivo).exists();

            Button slot = new Button(existe ? labels[i] : labels[i] + " (Vazio)");
            slot.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            slot.setDisable(!existe);
            slot.setFocusTraversable(false); 

            String corBorda = existe ? "#00E5E5" : "#443E30";
            String corTexto = existe ? "#C0B890" : "#443E30";

            slot.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                "-fx-border-color: " + corBorda + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 3;" +
                "-fx-background-radius: 3;" +
                "-fx-text-fill: " + corTexto + ";" +
                "-fx-padding: 6 16;" +
                "-fx-cursor: " + (existe ? "hand" : "default") + ";"
            );

            if (existe) {
                slot.setOnAction(e -> onLoadSlot.accept(arquivo));

                slot.setOnMouseEntered(ev -> slot.setStyle(
                    "-fx-background-color: rgba(0, 210, 210, 0.2);" +
                    "-fx-border-color: #00E5E5;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 3;" +
                    "-fx-background-radius: 3;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 6 16;" +
                    "-fx-cursor: hand;"
                ));
                slot.setOnMouseExited(ev -> slot.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.4);" +
                    "-fx-border-color: " + corBorda + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 3;" +
                    "-fx-background-radius: 3;" +
                    "-fx-text-fill: " + corTexto + ";" +
                    "-fx-padding: 6 16;" +
                    "-fx-cursor: hand;"
                ));
                
                slot.setOnMousePressed(e -> slot.setTranslateY(1));
                slot.setOnMouseReleased(e -> slot.setTranslateY(0));
            }

            box.getChildren().add(slot);
        }

        return box;
    }

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

    private static HBox criarRodape() {
        HBox rodape = new HBox(20);
        rodape.setAlignment(Pos.BOTTOM_RIGHT);
        rodape.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);

        Text dica1 = criarTextoDica("W/S ou Setas", " Navegar");
        Text dica2 = criarTextoDica("Mouse/ENTER", " Selecionar");
        Text dica3 = criarTextoDica("ESC", " Sair");

        rodape.getChildren().addAll(dica1, dica2, dica3);
        return rodape;
    }

    private static Text criarTextoDica(String tecla, String acao) {
        Text t = new Text(tecla + acao);
        t.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        t.setFill(Color.web("#8A8070", 0.9));
        return t;
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }

    public static String getUltimoSave() {
        File[] saves = {
            new File("save1.json"),
            new File("save2.json"),
            new File("save3.json")
        };

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