package com.RPG.TheLastRoar;

/**
 * ============================================================
 * Item.java — Classe Base para Todos os Itens do Jogo
 * ============================================================
 * 
 * HIERARQUIA:
 * Item ← Sword, Armor, Potion
 * 
 * RESPONSABILIDADE:
 * - name, value, size (com validação nos setters)
 * - Base para Inventory.java, ShopNPC.java
 * 
 * USAGE:
 * new Sword("Adaga", 10, 5, "Comum", 1)  // herda Item props
 */
public class Item {

    // Propriedades do item
    private String name;   // Nome do item (ex: "Poção de Vida", "Moeda")
    private int value;     // Valor em moedas (quanto vale ou custa)
    private int size;      // Tamanho em unidades de espaço (quanto ocupa na mochila)

    /**
     * CONSTRUTOR - Item()
     * Cria um novo item com as propriedades fornecidas
     * 
     * Parâmetros:
     * - name: Nome do item
     * - value: Valor/Preço do item
     * - size: Tamanho (espaço que ocupa)
     */
    public Item(String name, int value, int size) {
        this.name = name;
        this.value = value;
        this.size = size;
    }

    /**
     * GETTERS (Métodos que retornam as informações do item)
     */
    public String getName() {
        return name;  // Retorna o nome do item
    }

    public int getValue() {
        return value;  // Retorna o valor/preço do item
    }

    public int getSize() {
        return size;  // Retorna o tamanho/espaço que ocupa
    }

    /**
     * SETTERS (Métodos que modificam as informações do item)
     * Todos validam os dados antes de modificar para evitar valores inválidos
     */
    
    /**
     * setName(String name)
     * Muda o nome do item
     * Valida para garantir que o nome não é nulo ou vazio
     */
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {  // Verifica se é um nome válido
            this.name = name;  // Atualiza o nome
        }
    }

    /**
     * setValue(int value)
     * Muda o valor/preço do item
     * Valida para garantir que o valor não é negativo
     */
    public void setValue(int value) {
        if (value >= 0) {  // Verifica se é um valor válido (não negativo)
            this.value = value;  // Atualiza o valor
        }
    }

    /**
     * setSize(int size)
     * Muda o tamanho/espaço que o item ocupa
     * Valida para garantir que o tamanho é positivo
     */
    public void setSize(int size) {
        if (size > 0) {  // Verifica se é um tamanho válido (maior que 0)
            this.size = size;  // Atualiza o tamanho
        }
    }
}