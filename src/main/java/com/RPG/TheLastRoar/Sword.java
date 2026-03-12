package com.RPG.TheLastRoar;

import java.util.Random;

/**
 * Sword.java - SISTEMA DE ARMAS
 * 
 * O QUE FAZ:
 * Define como calcular o dano das armas durante uma batalha
 * Cada arma tem uma mecânica DIFERENTE de ataque
 * 
 * TIPOS DE ARMAS:
 * 
 * 1. ADAGA (Espada Curta)
 *    - Ataca 2 vezes em um turno
 *    - Cada ataque: dano base OU 2x dano base se crítico
 *    - Exemplo: dano=5 -> pode fazer 5+5=10 OU 10+10=20 com sorte
 * 
 * 2. KATANA (Espada Samurai)
 *    - Ataque único mas com crítico PODEROSO
 *    - Crítico só se rolar 20 (5% de chance)
 *    - Multiplicador varia por raridade:
 *      - Comum: 2x = 10 dano base = 20 em crítico
 *      - Rara: 3x = 10 dano base = 30 em crítico
 *      - Lendária: 4x = 10 dano base = 40 em crítico
 * 
 * 3. ESPADA LONGA
 *    - Ataque único com múltiplas chances de crítico
 *    - Chance varia por raridade:
 *      - Comum: crítico só com 20
 *      - Rara: crítico com 18, 19 ou 20 (15% de chance)
 *      - Lendária: crítico com 15-20 (30% de chance)
 *    - Crítico = 2x dano
 * 
 * 4. OUTRAS ARMAS
 *    - Uma única chance de crítico (com 20)
 *    - Se crítico, faz 2x dano
 * 
 * ===== COMO MODIFICAR =====
 * 
 * Aumentar dano de ADAGA:
 * - Altere o "dano" (damage) no construtor
 * 
 * Aumentar crítico de KATANA:
 * - Mude case "Rara" -> 3 para case "Rara" -> 4
 * 
 * Aumentar chance de crítico de ESPADA LONGA:
 * - Mude case "Lendaria" -> 15 para case "Lendaria" -> 10
 *   (número menor = mais fácil de crítico)
 * 
 * ADICIONAR NOVA ARMA:
 * - Crie um novo case "NovaArma" com a lógica de dano
 * 
 *   case "NovaArma" -> {
 *       int danoFinal = damage;
 *       if (dado >= 19) {  // Crítico com 19 ou 20
 *           danoFinal *= 3;
 *       }
 *       danoTotal = danoFinal;
 *   }
 */
public class Sword extends Item {

    // ===== PROPRIEDADES DE COMBATE =====
    
    // Dano base (o dano mínimo do ataque)
    // Exemplo: dano = 5 significa que a espada faz no mínimo 5 de dano
    private int damage;
    
    // Tipo/Raridade da espada
    // Exemplos: "Comum", "Rara", "Lendária"
    // Raridades mais altas têm críticos mais fortes
    private String type;
    
    // Objeto para gerar números aleatórios (para rolar o dado 1-20)
    private static final Random random = new Random();

    /**
     * CONSTRUTOR - Sword()
     * 
     * Cria uma nova espada com as propriedades fornecidas
     * 
     * Parâmetros:
     * - name: Nome da espada (ex: "Adaga", "Katana", "Espada Longa")
     * - value: Preço/Valor da espada
     * - damage: Dano base na batalha
     * - type: Tipo/Raridade (ex: "Comum", "Rara", "Lendária")
     * - size: Espaço que ocupa na mochila
     * 
     * Herança:
     * Chama super() para herdar nome, valor e tamanho da classe Item
     * 
     * Exemplo de uso:
     * Sword adaga = new Sword("Adaga", 100, 5, "Comum", 2);
     * Sword katana = new Sword("Katana", 500, 10, "Rara", 3);
     */
    public Sword(String name, int value, int damage, String type, int size) {
        super(name, value, size);  // Herda de Item (nome, valor, tamanho)
        this.damage = damage;      // Define o dano base
        this.type = type;          // Define o tipo/raridade
    }

    /**
     * ===== MÉTODOS GETTER (Retornam informações) =====
     */
    
    /**
     * getDamage()
     * Retorna o dano base da espada
     * Use quando precisar saber quanto de dano a arma faz
     */
    public int getDamage() {
        return damage;  // Retorna o dano armazenado
    }

    /**
     * getType()
     * Retorna o tipo/raridade da espada
     * Use para saber se é Comum, Rara ou Lendária
     */
    public String getType() {
        return type;  // Retorna Comum, Rara, Lendária, etc
    }

    /**
     * ===== MÉTODOS SETTER (Modificam informações) =====
     */
    
    /**
     * setDamage()
     * Muda o dano da espada
     * Valida para garantir que o dano não é zero ou negativo
     * 
     * Parâmetro:
     * - damage: O novo valor de dano
     */
    public void setDamage(int damage) {
        if (damage > 0) {  // Valida: só aceita valores positivos
            this.damage = damage;  // Atualiza o dano
        }
    }

    /**
     * setType()
     * Muda o tipo/raridade da espada
     * Valida para garantir que o tipo é válido (não nulo, não vazio)
     * 
     * Parâmetro:
     * - type: O novo tipo (ex: "Rara")
     */
    public void setType(String type) {
        if (type != null && !type.isEmpty()) {  // Valida: não nulo e não vazio
            this.type = type;  // Atualiza o tipo
        }
    }

    /**
     * calculateDamage()
     * 
     * CALCULA O DANO TOTAL DE UM ATAQUE COM ESTA ESPADA
     * 
     * PASSO A PASSO:
     * 1. Rola um dado virtual de 1 a 20 (para sorte/crítico)
     * 2. Pega o nome da espada
     * 3. Verifica qual tipo de espada é
     * 4. Calcula o dano com a mecânica específica
     * 5. Retorna o dano total
     * 
     * O DADO:
     * Números de 1 a 20 (como um D20 em RPGs)
     * 1-19 = sem crítico (normal)
     * 20 = crítico (muita sorte!)
     * 
     * O dano varia muito porque cada arma tem mecânicas diferentes!
     * 
     * Retorno:
     * Um número inteiro com o dano total do ataque
     * 
     * Exemplo:
     * Adaga com dano=5:
     * - Se rolar 1-19: 5+5 = 10 dano total
     * - Se rolar 20: (5*2)+(5*2) = 20 dano total
     */
    public int calculateDamage() {

        // PREPARA AS VARIÁVEIS
        int danoTotal = 0;  // Armazenará o dano total do ataque
        int dado = random.nextInt(20) + 1;  // Rola um dado de 1 a 20
        String weaponName = getName();  // Descobre qual é a espada

        // PROCESSA CADA TIPO DE ARMA DIFERENTEMENTE
        // switch = escolha dependendo do nome da arma
        switch (weaponName) {

            // ==================== ADAGA ====================
            // Ataca duas vezes, cada ataque pode ser crítico
            case "Adaga" -> {
                // PRIMEIRO ATAQUE
                for (int i = 0; i < 2; i++) {  // Repete 2 vezes (2 ataques)

                    int danoFinal = damage;  // Começa com dano base

                    // VERIFICA CRÍTICO
                    // Se rolar 20, este ataque faz 2x o dano
                    if (dado == 20) {
                        danoFinal *= 2;  // Dobra o dano (multiplicação)
                    }

                    // SOMA AO TOTAL
                    danoTotal += danoFinal;  // Adiciona este ataque ao total
                }
            }

            // ==================== KATANA ====================
            // Um ataque com crítico devastador (depende da raridade)
            case "Katana" -> {
                int danoFinal = damage;  // Começa com dano base

                // DEFINE O MULTIPLICADOR DE CRÍTICO POR RARIDADE
                // Quanto melhor a raridade, mais forte o crítico
                int multiplicadorCritico = switch (type) {
                    case "Rara" -> 3;           // Rara: 3x dano em crítico
                    case "Lendaria" -> 4;       // Lendária: 4x dano em crítico
                    default -> 2;               // Comum: 2x dano em crítico
                };

                // VERIFICA E APLICA CRÍTICO
                if (dado == 20) {  // Se rolar 20
                    danoFinal *= multiplicadorCritico;  // Multiplica o dano
                }

                danoTotal = danoFinal;  // Atribui o dano total
            }

            // ==================== ESPADA LONGA ====================
            // Um ataque com chance de crítico que varia por raridade
            case "Espada Longa" -> {
                int danoFinal = damage;  // Começa com dano base

                // DEFINE A CHANCE DE CRÍTICO POR RARIDADE
                // Número MENOR = maior chance (mais números fazem crítico)
                int chanceCritico = switch (type) {
                    case "Rara" -> 18;        // Rara: crítico com 18+ (3/20 = 15%)
                    case "Lendaria" -> 15;    // Lendária: crítico com 15+ (6/20 = 30%)
                    default -> 20;            // Comum: crítico com 20 (1/20 = 5%)
                };

                // VERIFICA E APLICA CRÍTICO
                // Se o dado é MAIOR ou IGUAL ao chanceCritico, faz crítico
                if (dado >= chanceCritico) {
                    danoFinal *= 2;  // Dobra o dano (crítico)
                }

                danoTotal = danoFinal;  // Atribui o dano total
            }

            // ==================== OUTRAS ARMAS ====================
            // Qualquer arma que não é Adaga, Katana ou Espada Longa
            default -> {
                int danoFinal = damage;  // Começa com dano base

                // Crítico genérico: só com 20
                if (dado == 20) {
                    danoFinal *= 2;  // Dobra o dano
                }

                danoTotal = danoFinal;  // Atribui o dano total
            }
        }

        return danoTotal;  // Retorna o dano total do ataque
    }
}