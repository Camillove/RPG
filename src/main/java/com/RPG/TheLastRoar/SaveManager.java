package com.RPG.TheLastRoar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Gerencia o sistema de save/load do jogo.
 * Lê e escreve arquivos JSON simples com o estado da partida.
 */
public class SaveManager {

    /**
     * Dados carregados de um arquivo de save.
     */
    public static class SaveData {
        public int mapa;
        public double posX;
        public double posY;
        public int vida;
        public int level;
        public int ouro;
        public boolean[][] inimigosDerrotados;
    }

    /**
     * Salva o estado atual do jogo em um arquivo JSON.
     *
     * @param arquivo             Nome do arquivo (ex: "save1.json")
     * @param indiceMapa          Índice do mapa atual
     * @param posX                Posição X do jogador
     * @param posY                Posição Y do jogador
     * @param player              Objeto do jogador
     * @param inimigosDerrotados  Matriz de inimigos derrotados por mapa
     */
    public static void salvar(String arquivo, int indiceMapa, double posX, double posY,
                               Character player, boolean[][] inimigosDerrotados) {
        try {
            StringBuilder sbInimigos = new StringBuilder();
            for (int i = 0; i < inimigosDerrotados.length; i++) {
                for (int j = 0; j < inimigosDerrotados[i].length; j++) {
                    sbInimigos.append(inimigosDerrotados[i][j]);
                    if (j < inimigosDerrotados[i].length - 1) sbInimigos.append(",");
                }
                if (i < inimigosDerrotados.length - 1) sbInimigos.append(";");
            }

            int levelPlayer = player.getNivel();
            int ouroPlayer  = player.getCoin();

            String json = "{\n" +
                "  \"mapaAtual\": " + indiceMapa + ",\n" +
                "  \"posicaoX\": " + posX + ",\n" +
                "  \"posicaoY\": " + posY + ",\n" +
                "  \"vidaPlayer\": " + player.getLife() + ",\n" +
                "  \"levelPlayer\": " + levelPlayer + ",\n" +
                "  \"ouroPlayer\": " + ouroPlayer + ",\n" +
                "  \"inimigosMortos\": \"" + sbInimigos + "\"\n" +
                "}";

            Files.write(Paths.get(arquivo), json.getBytes());
            System.out.println("Jogo salvo com sucesso em " + arquivo + "!");

        } catch (IOException e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    /**
     * Carrega o estado do jogo de um arquivo JSON.
     *
     * @param arquivo             Nome do arquivo a carregar
     * @param totalMapas          Quantidade total de mapas (para dimensionar a matriz)
     * @return SaveData com os dados lidos, ou null se o arquivo não existir
     */
    public static SaveData carregar(String arquivo, int totalMapas) {
        try {
            if (!Files.exists(Paths.get(arquivo))) {
                System.out.println("Arquivo " + arquivo + " não encontrado!");
                return null;
            }

            String conteudoJson = new String(Files.readAllBytes(Paths.get(arquivo)));
            String textoLimpo = conteudoJson.replaceAll("[\\{\\}\"\\s]", "");

            SaveData data = new SaveData();
            data.mapa  = 0;
            data.posX  = 0;
            data.posY  = 0;
            data.vida  = 100;
            data.level = 1;
            data.ouro  = 0;
            data.inimigosDerrotados = new boolean[totalMapas][10];

            String inimigosSalvos = "";
            String[] atributos = textoLimpo.split("[:,]");

            for (int i = 0; i < atributos.length; i++) {
                switch (atributos[i]) {
                    case "mapaAtual"     -> data.mapa  = Integer.parseInt(atributos[i + 1]);
                    case "posicaoX"      -> data.posX  = Double.parseDouble(atributos[i + 1]);
                    case "posicaoY"      -> data.posY  = Double.parseDouble(atributos[i + 1]);
                    case "vidaPlayer"    -> data.vida  = Integer.parseInt(atributos[i + 1]);
                    case "levelPlayer"   -> data.level = Integer.parseInt(atributos[i + 1]);
                    case "ouroPlayer"    -> data.ouro  = Integer.parseInt(atributos[i + 1]);
                    case "inimigosMortos" -> inimigosSalvos =
                        textoLimpo.substring(textoLimpo.indexOf("inimigosMortos:") + 15);
                }
            }

            if (!inimigosSalvos.isEmpty()) {
                String[] mapasString = inimigosSalvos.split(";");
                for (int i = 0; i < mapasString.length; i++) {
                    String[] inims = mapasString[i].split(",");
                    for (int j = 0; j < inims.length; j++) {
                        data.inimigosDerrotados[i][j] = Boolean.parseBoolean(inims[j]);
                    }
                }
            }

            System.out.println("Jogo carregado de " + arquivo + "!");
            return data;

        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
            return null;
        }
    }

    /** Retorna true se o arquivo de save existe. */
    public static boolean existe(String arquivo) {
        return Files.exists(Paths.get(arquivo));
    }
}
