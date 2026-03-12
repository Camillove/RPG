package com.RPG.TheLastRoar;

/**
 * Goblin.java - UM TIPO DE INIMIGO
 * 
 * O QUE FAZ:
 * Define as propriedades de um Goblin (um tipo fraco de inimigo)
 * Herda todas as comportamentos de Monsters (ataque, defesa, etc)
 * Só precisa definir os valores específicos do Goblin
 * 
 * COMO USAR:
 * Goblin goblin = new Goblin();  // Cria um novo goblin
 * 
 * MODIFICAR:
 * Para mudar a dificuldade, altere os números no super()
 * Exemplo: mudar 8 para 15 faz o goblin ter mais vida
 * 
 * ADICIONAR NOVO INIMIGO:
 * Crie uma nova classe parecida com esta, por exemplo:
 * 
 *   public class Orc extends Monsters {
 *       public Orc() {
 *           super("Orc", 20, 5, 5, 3, 50, 1);
 *       }
 *   }
 * 
 * Os números significam:
 * "Orc" = nome do inimigo
 * 20 = vida máxima
 * 5 = dano que faz
 * 5 = moedas que o jogador ganha ao derrotar
 * 3 = nível do inimigo
 * 50 = XP que o jogador ganha
 * 1 = resistência/defesa
 */
public class Goblin extends Monsters{
    
    /**
     * CONSTRUTOR - Goblin()
     * 
     * Cria um novo Goblin com valores pré-definidos
     * 
     * Os valores passados para o construtor da classe Monsters são:
     * - Nome: "Goblin"
     * - Vida: 8 pontos
     * - Dano: 2 pontos
     * - Moedas: 3 moedas que o jogador ganha ao derrotar
     * - XP: 2 pontos de experiência que o jogador ganha
     * - Velocidade: 20 (velocidade na tela)
     * - Resistência: 0 (não tem defesa)
     * 
     * MODIFICAR:
     * Aumente qualquer número para aumentar a dificuldade
     * Exemplo:
     * - Mudar 8 para 20 = goblin mais forte
     * - Mudar 2 para 5 = goblin que faz mais dano
     * - Mudar 0 para 3 = goblin que defende melhor
     */
    public Goblin() {
        super("Goblin", 8, 2, 3, 2, 20, 0);
        //     nome     vida dano moedas xp velocidade resistencia
    }
}
