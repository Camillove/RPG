package com.RPG.TheLastRoar;

/**
 * Potion.java
 * Representa uma poção de cura (herda de Item)
 * É um item consumível que recupera pontos de vida do personagem
 */
public class Potion extends Item {

    // Quantidade de vida que a poção recupera
    private int healedLife;  // Pontos de vida curados ao usar a poção

    /**
     * CONSTRUTOR - Potion()
     * Cria uma nova poção de cura
     * 
     * Parâmetros:
     * - name: Nome da poção (ex: "Poção Pequena", "Poção Grande")
     * - value: Preço da poção
     * - size: Espaço que ocupa na mochila
     * - healedLife: Quantos pontos de vida a poção cura
     */
    public Potion(String name, int value, int size, int healedLife) {
        super(name, value, size);  // Chama construtor da classe Item
        this.healedLife = healedLife;  // Define quanto de vida cura
    }

    /**
     * GETTER
     * getHealedLife()
     * Retorna quantos pontos de vida a poção cura
     */
    public int getHealedLife() {
        return healedLife;
    }

    /**
     * SETTER
     * setHealedLife(int healedLife)
     * Muda quanto de vida a poção cura
     * Valida para garantir que é um valor positivo
     */
    public void setHealedLife(int healedLife) {
        if (healedLife > 0) {  // Garante que é um valor válido
            this.healedLife = healedLife;
        }
    }

}