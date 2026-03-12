package com.RPG.TheLastRoar;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory.java
 * Gerencia a mochila do personagem
 * Controla quantos itens cabe, quanto espaço está sendo usado
 * e permite adicionar ou remover itens
 */
public class Inventory {

    // Espaço disponível
    private int maxSpace = 20;      // Espaço máximo (20 por padrão)
    private int usedSpace = 0;      // Espaço atualmente usado
    private List<Item> items = new ArrayList<>();  // Lista que armazena os itens

    /**
     * addItem(Item item)
     * Tenta adicionar um item à mochila
     * Retorna true se conseguir, false se não houver espaço ou item for nulo
     * 
     * Explicação:
     * - Primeiro verifica se o item é válido (não nulo)
     * - Depois verifica se há espaço suficiente
     * - Se tiver espaço, adiciona o item e atualiza o espaço usado
     */
    public boolean addItem(Item item) {

        if (item == null) return false;  // Item inválido, não adiciona

        // Verifica se tem espaço para o item
        if (usedSpace + item.getSize() <= maxSpace) {
            items.add(item);               // Adiciona o item à lista
            usedSpace += item.getSize();   // Atualiza o espaço usado
            return true;                   // Retorna sucesso
        } else {
            return false;                  // Mochila cheia, não conseguiu adicionar
        }
    }

    /**
     * increaseSpace(int quantidade)
     * Aumenta o espaço máximo da mochila
     * Usado quando o personagem sobe de nível (levels 5 e 10)
     */
    public void increaseSpace(int quantidade) {
        if (quantidade > 0) {     // Valida que é um aumento positivo
            maxSpace += quantidade;  // Aumenta o espaço máximo
        }
    }

    /**
     * removeItem(Item item)
     * Remove um item da mochila
     * Retorna true se conseguir remover, false se item não estiver na mochila
     */
    public boolean removeItem(Item item) {
        if (items.remove(item)) {      // Tenta remover o item
            usedSpace -= item.getSize();  // Se removeu, libera o espaço
            return true;                  // Retorna sucesso
        }
        return false;                   // Item não estava na mochila
    }

    /**
     * GETTERS (Métodos que retornam as informações da mochila)
     */
    
    /**
     * getMaxSpace()
     * Retorna o espaço máximo disponível
     */
    public int getMaxSpace() {
        return maxSpace;
    }

    /**
     * getUsedSpace()
     * Retorna quanto espaço está sendo usado
     * Para saber quanto espaço livre há, use: maxSpace - usedSpace
     */
    public int getUsedSpace() {
        return usedSpace;
    }

    /**
     * getItems()
     * Retorna a lista de itens na mochila
     */
    public List<Item> getItems() {
        return items;
    }
}