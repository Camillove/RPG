package com.RPG.TheLastRoar;

/**
 * Armor.java
 * Representa uma armor/armadura (herda de Item)
 * Fornece resistência adicional ao personagem ao ser equipada
 */
public class Armor extends Item {

    // Valor de resistência que a armadura fornece
    private int resistance;  // Pontos de resistência que a armadura adiciona

    /**
     * CONSTRUTOR - Armor()
     * Cria uma nova armadura
     * 
     * Parâmetros:
     * - name: Nome da armadura (ex: "Coraza de Ferro", "Prá de Aço")
     * - value: Preço da armadura
     * - size: Espaço que ocupa na mochila
     * - resistance: Pontos de resistência que fornece
     */
    public Armor(String name, int value, int size, int resistance) {
        super(name, value, size);  // Chama construtor da classe Item
        this.resistance = resistance;  // Define o valor de resistência
    }

    /**
     * GETTER
     * getResistance()
     * Retorna quantos pontos de resistência a armadura fornece
     */
    public int getResistance() {
        return resistance;
    }

    /**
     * SETTER
     * setResistance(int resistance)
     * Muda quantos pontos de resistência a armadura fornece
     * Valida para garantir que não é negativo
     */
    public void setResistance(int resistance) {
        if (resistance >= 0) {  // Garante que é zero ou positivo
            this.resistance = resistance;
        }
    }
}