package com.RPG.TheLastRoar;

import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;

/**
 * Character.java
 * Representa o personagem do jogador
 * Controla vida, XP, level, inventário, armas e resistência
 */
public class Character {

    // Informações básicas do personagem
    private String name;              // Nome do personagem
    private int life;                 // Pontos de vida atuais
    private int maxLife;              // Pontos de vida máximos
    private int coin;                 // Dinheiro do personagem
    private int resistance;           // Resistência a dano do personagem
    private Inventory inventory;      // Mochila que armazena itens
    
    // Sistema de leveling
    private int nivel = 1;            // Nível atual (1-10)
    private int xp = 0;               // Experiência atual
    private int xpNecessary = 10;     // Experiência necessária para o próximo level
    
    // Equipamento do personagem
    private Sword sword;              // Arma equipada
    
    // Utilitários
    private static final Random random = new Random();  // Para gerar números aleatórios
    private Image sprite;             // Imagem do personagem no mapa
    private Image battleSprite;       // Imagem do personagem em batalha

    /**
     * CONSTRUTOR - Character()
     * Inicializa um novo personagem com as propriedades fornecidas
     * 
     * Parâmetros:
     * - name: Nome do personagem
     * - life: Vida máxima (vida atual também começa com este valor)
     * - resistance: Resistência a dano
     * - sword: Espada inicial do personagem
     * - sprite: Imagem para mostrar no mapa
     * - battleSprite: Imagem para mostrar em batalhas
     */
    public Character(String name, int life, int resistance, Sword sword, Image sprite, Image battleSprite) {
        this.name = name;
        this.life = life;              // Vida atual começa no máximo
        this.maxLife = life;           // Armazena o máximo
        this.resistance = resistance;  // Define resistência
        this.coin = 0;                 // Começa sem dinheiro
        this.inventory = new Inventory();  // Cria uma nova mochila vazia
        this.sword = sword;            // Equipa a espada inicial
        this.sprite = sprite;          // Define imagem do mapa
        this.battleSprite = battleSprite;  // Define imagem de batalha
    }

    /**
     * GETTERS (Métodos que retornam as informações)
     * Use esses métodos para obter informações do personagem
     */
    public String getName() { return name; }              // Retorna o nome
    public int getLife() { return life; }                 // Retorna vida atual
    public int getMaxLife() { return maxLife; }           // Retorna vida máxima
    public int getCoin() { return coin; }                 // Retorna dinheiro
    public int getResistance() { return resistance; }     // Retorna resistência
    public Inventory getInventory() { return inventory; } // Retorna a mochila
    public int getNivel() { return nivel; }               // Retorna level atual
    public int getXp() { return xp; }                     // Retorna XP atual
    public int getXpNecessary() { return xpNecessary; }   // Retorna XP necessário para level up
    public Sword getSword() { return sword; }             // Retorna a espada equipada

    /**
     * CONTROLE DE VIDA
     * setLife(int life)
     * Define a vida do personagem
     * Garante que a vida nunca seja menor que 0 ou maior que maxLife
     */
    public void setLife(int life) {
        // Math.max(0, ...) garante que vida não seja negativa
        // Math.min(..., maxLife) garante que vida não ultrapasse o máximo
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    /**
     * addCoin(int amount)
     * Adiciona dinheiro ao personagem
     * Só adiciona se o valor for positivo (para evitar bugs)
     */
    public void addCoin(int amount) {
        if (amount > 0) {  // Verifica se é um valor válido
            this.coin += amount;  // Adiciona o valor
        }
    }

    /**
     * setSword(Sword sword)
     * Equipa uma nova espada
     * Valida para garantir que não é nula
     */
    public void setSword(Sword sword) {
        if (sword != null) {  // Verifica se a espada é válida
            this.sword = sword;  // Equipa a nova espada
        }
    }

    public Image getSprite() {
        return sprite;
    }

    public Image getBattleSprite() {
        return battleSprite;
    }
    /**
     * SISTEMA DE XP E LEVEL
     * O personagem pode atingir no máximo level 10
     * A cada level up, ganha vida e espaço na mochila
     */
    
    /**
     * earnXp(int quantidade)
     * Ganha experiência ao derrotar inimigos
     * Se tiver XP suficiente, sobe de nível automaticamente
     */
    public void earnXp(int quantidade) {
        if (quantidade <= 0) return;  // Ignora valores inválidos

        xp += quantidade;  // Adiciona XP
        calculateLevel();  // Verifica se pode subir de nível
    }

    /**
     * calculateLevel()
     * Verifica se o personagem tem XP suficiente para subir de nível
     * Se sim, realiza o level up e ajusta as estatísticas
     */
    private void calculateLevel() {

        // Sobe de nível enquanto tiver XP suficiente e não estiver no level máximo (10)
        while (xp >= xpNecessary && nivel < 10) {

            xp -= xpNecessary;      // Remove o XP gasto
            nivel++;                // Incrementa o nível
            xpNecessary += 5;       // Próximo level requer mais XP (começa em 10, depois 15, 20, etc)

            maxLife += 2;           // Ganha 2 de vida
            life = maxLife;         // Recupera toda a vida

            // A cada 5 níveis, ganha espaço na mochila
            if (nivel == 5 || nivel == 10) {
                inventory.increaseSpace(5);  // Aumenta espaço em 5
            }
        }

        // Se chegou no máximo level (10), garante que XP não passe do necessário
        if (nivel == 10 && xp >= xpNecessary) {
            xp = xpNecessary;  // Limita ao máximo
        }
    }

    // ATAQUE ÚNICO
    public void attack(Monsters alvo) {

        if (alvo == null) return;

        int danoFinal = Math.max(0, sword.calculateDamage() - alvo.getResistance());

        alvo.setLife(alvo.getLife() - danoFinal);
    }

    // ATAQUE EM ÁREA
    public void attackArea(List<Monsters> alvos) {

        if (alvos == null || alvos.isEmpty()) return;

        int danoBase = sword.calculateDamage() / 2;

        for (int i = 0; i < alvos.size(); i++) {

            Monsters alvo = alvos.get(i);

            int danoFinal = Math.max(0, danoBase - alvo.getResistance());

            alvo.setLife(alvo.getLife() - danoFinal);

            if (alvo.getLife() <= 0) {
                alvos.remove(i);
                i--;
            }
        }
    }

    // TENTAR FUGIR
    public boolean leave() {

        int dado = random.nextInt(20) + 1;

        return dado > 18;
    }
}