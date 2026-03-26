package com.RPG.TheLastRoar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Gerencia o sistema de save/load do jogo.
 * Salva e carrega o estado da partida em um arquivo de texto local.
 */
public class SaveManager {

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
     * Salva o estado atual do jogo no disco.
     */
    public static void salvar(String arquivo, int indiceMapa, double posX, double posY,
                              Character player, boolean[][] inimigosDerrotados) {
        try {
            // 1. Monta a string da matriz de inimigos (ex: false,true,false;true,false,false)
            StringBuilder sbInimigos = new StringBuilder();
            if (inimigosDerrotados != null) {
                for (int i = 0; i < inimigosDerrotados.length; i++) {
                    for (int j = 0; j < inimigosDerrotados[i].length; j++) {
                        sbInimigos.append(inimigosDerrotados[i][j]);
                        if (j < inimigosDerrotados[i].length - 1) sbInimigos.append(",");
                    }
                    if (i < inimigosDerrotados.length - 1) sbInimigos.append(";");
                }
            }

            // 2. Usamos Properties do Java para garantir que o formato nunca quebre
            Properties props = new Properties();
            props.setProperty("mapaAtual", String.valueOf(indiceMapa));
            props.setProperty("posicaoX", String.valueOf(posX));
            props.setProperty("posicaoY", String.valueOf(posY));
            props.setProperty("vidaPlayer", String.valueOf(player.getLife()));
            props.setProperty("levelPlayer", String.valueOf(player.getNivel()));
            props.setProperty("ouroPlayer", String.valueOf(player.getCoin()));
            props.setProperty("inimigosMortos", sbInimigos.toString());

            // 3. Escreve no arquivo
            StringWriter writer = new StringWriter();
            props.store(writer, "Save Data - The Last Roar");
            Files.writeString(Paths.get(arquivo), writer.getBuffer().toString());

            System.out.println("[SaveManager] Jogo salvo com sucesso em " + arquivo + "!");

        } catch (IOException e) {
            System.err.println("[SaveManager] Erro ao salvar o jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carrega o estado do jogo do arquivo no disco.
     */
    public static SaveData carregar(String arquivo, int totalMapas) {
        if (!existe(arquivo)) {
            System.out.println("[SaveManager] Arquivo " + arquivo + " não encontrado! Iniciando novo jogo.");
            return null;
        }

        try {
            // 1. Lê o arquivo como texto e carrega no Properties
            String conteudo = Files.readString(Paths.get(arquivo));
            Properties props = new Properties();
            props.load(new StringReader(conteudo));

            SaveData data = new SaveData();

            // 2. Extrai os dados básicos com valores padrão de segurança caso algo falte
            data.mapa  = Integer.parseInt(props.getProperty("mapaAtual", "0"));
            data.posX  = Double.parseDouble(props.getProperty("posicaoX", "0.0"));
            data.posY  = Double.parseDouble(props.getProperty("posicaoY", "0.0"));
            data.vida  = Integer.parseInt(props.getProperty("vidaPlayer", "100"));
            data.level = Integer.parseInt(props.getProperty("levelPlayer", "1"));
            data.ouro  = Integer.parseInt(props.getProperty("ouroPlayer", "0"));

            // 3. Reconstrói a matriz de inimigos derrotados
            // Assume 10 inimigos por mapa como padrão, ajustável conforme sua necessidade
            data.inimigosDerrotados = new boolean[totalMapas][10]; 
            
            String inimigosSalvos = props.getProperty("inimigosMortos", "");
            
            if (!inimigosSalvos.isEmpty()) {
                String[] mapasString = inimigosSalvos.split(";");
                // Garante que não vai estourar o limite de mapas se o save for antigo
                int mapasLimite = Math.min(mapasString.length, totalMapas); 
                
                for (int i = 0; i < mapasLimite; i++) {
                    String[] inims = mapasString[i].split(",");
                    int inimigosLimite = Math.min(inims.length, data.inimigosDerrotados[i].length);
                    
                    for (int j = 0; j < inimigosLimite; j++) {
                        data.inimigosDerrotados[i][j] = Boolean.parseBoolean(inims[j]);
                    }
                }
            }

            System.out.println("[SaveManager] Jogo carregado com sucesso de " + arquivo + "!");
            return data;

        } catch (Exception e) {
            System.err.println("[SaveManager] O arquivo de save está corrompido ou em formato inválido.");
            e.printStackTrace();
            return null;
        }
    }

    /** Retorna true se o arquivo de save existe no disco local. */
    public static boolean existe(String arquivo) {
        return Files.exists(Paths.get(arquivo));
    }
}