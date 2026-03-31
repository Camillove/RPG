package com.RPG.TheLastRoar;

import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;

/**
 * ============================================================
 * Character.java — Personagem principal do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Controla TODAS as propriedades do herói: vida, XP, level, inventário,
 * equipamentos (sword/armor), economia (moedas), combate.
 *
 * SISTEMA DE RESISTÊNCIA ATUALIZADO:
 * - resistance (base) + equippedArmor.getResistance() = total
 * - Usado em Battle.java, HudManager.java, ShopNPC.java
 *
 * DEPENDENCIES:
 * - Inventory.java, Sword.java, Armor.java, Monsters.java (combate)
 * - HudManager.java (display), Battle.java (turnos)
 */
public class Character {

    // Informações básicas do personagem
    private String name;
    private int life;
    private int maxLife;
    private int coin;
    private int resistance;           // Resistência BASE do personagem (sem armadura)
    private Inventory inventory;

    // Sistema de leveling
    private int nivel = 1;
    private int xp = 0;
    private int xpNecessary = 10;

    // Equipamento do personagem
    private Sword sword;
    private Armor equippedArmor;      // ← NOVO: armadura que está equipada no momento

    // Utilitários
    private static final Random random = new Random();
    private Image sprite;
    private Image battleSprite;

    /**
     * CONSTRUTOR
     */
    public Character(String name, int life, int resistance, Sword sword, Image sprite, Image battleSprite) {
        this.name = name;
        this.life = life;
        this.maxLife = life;
        this.resistance = resistance;
        this.coin = 0;
        this.inventory = new Inventory();
        this.sword = sword;
        this.sprite = sprite;
        this.battleSprite = battleSprite;
        this.equippedArmor = null;    // Inicia sem armadura equipada
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName() { return name; }
    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }
    public int getCoin() { return coin; }
    public Inventory getInventory() { return inventory; }
    public int getNivel() { return nivel; }
    public int getXp() { return xp; }
    public int getXpNecessary() { return xpNecessary; }
    public int getMaxXp() { return xpNecessary; }
    public Sword getSword() { return sword; }
    public Image getSprite() { return sprite; }
    public Image getBattleSprite() { return battleSprite; }

    /**
     * NOVO GETTER: retorna a armadura equipada atualmente (pode ser null).
     */
    public Armor getEquippedArmor() {
        return equippedArmor;
    }

    /**
     * getResistance() — ATUALIZADO
     *
     * Agora retorna a resistência TOTAL do personagem:
     * resistência base + resistência da armadura equipada (se houver).
     *
     * Exemplo: resistência base = 2, armadura = 5 → total = 7
     */
    public int getResistance() {
        int resistenciaTotal = this.resistance; // Começa com a resistência base
        if (equippedArmor != null) {
            resistenciaTotal += equippedArmor.getResistance(); // Soma a armadura
        }
        return resistenciaTotal;
    }

    // =========================================================================
    // SETTERS
    // =========================================================================

    public void setNivel(int nivel) { this.nivel = nivel; }
    public void setCoin(int coin)   { this.coin = coin; }
    public void setXp(int xp)       { this.xp = xp; }

    public void setSword(Sword sword) {
        // Permite setar null (para desequipar a espada)
        this.sword = sword;
    }

    /**
     * NOVO SETTER: equipa ou desequipa uma armadura.
     *
     * @param armor A armadura a ser equipada, ou null para desequipar.
     */
    public void setEquippedArmor(Armor armor) {
        this.equippedArmor = armor; // null = sem armadura equipada
    }

    // =========================================================================
    // STATUS E CONDIÇÕES
    // =========================================================================

    public boolean isAlive() {
        return this.life > 0;
    }

    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    public void heal(int amount) {
        if (amount > 0) {
            setLife(this.life + amount);
        }
    }

    // =========================================================================
    // SISTEMA ECONÔMICO
    // =========================================================================

    public void addCoin(int amount) {
        if (amount > 0) { this.coin += amount; }
    }

    public boolean removeCoin(int amount) {
        if (amount > 0 && this.coin >= amount) {
            this.coin -= amount;
            return true;
        }
        return false;
    }

    // =========================================================================
    // SISTEMA DE XP E LEVEL
    // =========================================================================

    public boolean earnXp(int quantidade) {
        if (quantidade <= 0) return false;
        xp += quantidade;
        return calculateLevel();
    }

    private boolean calculateLevel() {
        boolean subiuDeNivel = false;
        while (xp >= xpNecessary && nivel < 10) {
            xp -= xpNecessary;
            nivel++;
            xpNecessary = (int)(xpNecessary * 1.5);
            life = maxLife;
            if (nivel == 5 || nivel == 10) {
                inventory.increaseSpace(5);
            }
            subiuDeNivel = true;
        }
        if (nivel == 10 && xp >= xpNecessary) {
            xp = xpNecessary;
        }
        return subiuDeNivel;
    }

    // =========================================================================
    // COMBATE
    // =========================================================================

    /**
     * Ataca um monstro com a espada equipada.
     * Se não houver espada equipada, o ataque retorna 0.
     */
    public int attack(Monsters alvo) {
        if (alvo == null || alvo.getLife() <= 0) return 0;
        if (sword == null) return 0; // Sem espada = sem ataque

        int danoFinal = Math.max(0, sword.calculateDamage() - alvo.getResistance());
        alvo.setLife(alvo.getLife() - danoFinal);
        return danoFinal;
    }

    public int attackArea(List<Monsters> alvos) {
        if (alvos == null || alvos.isEmpty() || sword == null) return 0;

        int danoBase = sword.calculateDamage() / 2;
        int totalDanoCausado = 0;

        for (int i = 0; i < alvos.size(); i++) {
            Monsters alvo = alvos.get(i);
            int danoFinal = Math.max(0, danoBase - alvo.getResistance());
            alvo.setLife(alvo.getLife() - danoFinal);
            totalDanoCausado += danoFinal;
            if (alvo.getLife() <= 0) {
                alvos.remove(i);
                i--;
            }
        }
        return totalDanoCausado;
    }

    public boolean leave() {
        int dado = random.nextInt(20) + 1;
        return dado > 10;
    }
}
