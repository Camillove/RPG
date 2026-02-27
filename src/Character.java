import java.util.Random;

public class Character {

    // Nome do personagem
    String name;

    // Vida atual do personagem
    int life;

    // Vida máxima que ele pode ter
    int maxLife;

    // Quantidade de moedas
    int coin;

    // Redução de dano
    int resistance;

    // Velocidade do personagem
    int speed;

    // Espaço disponível no inventário
    int inventory;

    // Nível atual (começa no 1)
    int nivel = 1;

    // XP atual
    int xp = 0;

    // XP necessário para subir de nível
    int xpNecessary = 10;

    // Arma equipada
    Sword sword;

    // Construtor do personagem
    public Character(String name, int life, int resistance, int speed, Sword sword) {
        this.name = name;
        this.life = life;          // Vida atual inicial
        this.maxLife = life;       // Vida máxima começa igual à vida inicial
        this.resistance = resistance;
        this.speed = speed;
        this.coin = 0;             // Começa sem moedas
        this.inventory = 0;        // Inventário começa vazio
        this.sword = sword;        // Arma equipada
    }

    // Método responsável por verificar e aplicar o level up
    public void calculateLevel() {

        // Só permite evoluir até o nível 10
        if (nivel <= 10) {

            // Enquanto tiver XP suficiente e não estiver no nível 10
            while (xp >= xpNecessary && nivel < 10) {

                nivel++;                 // Sobe o nível
                xp -= xpNecessary;       // Remove o XP usado
                xpNecessary += 5;        // Aumenta o XP necessário para o próximo nível

                System.out.println("Voce subiu de nivel!! Você está nivel " + nivel);

                maxLife += 2;            // Aumenta a vida máxima

                // Bônus especial nos níveis 5 e 10
                if (nivel == 5 || nivel == 10) {
                    inventory += 5;
                    System.out.println("Você ganhou +5 espaços no inventário!");
                }
            }

        } else {
            System.out.println("Você chegou no nivel maximo!");
        }
    }

    // Método para ganhar XP
    public void earnXp(int quantidade) {

        xp += quantidade;  // Adiciona XP

        System.out.println(name + " ganhou " + quantidade + " XP!");
        System.out.println("XP atual: " + xp + "/" + xpNecessary);

        calculateLevel();  // Verifica se pode subir de nível
    }

    // Ataque contra um único monstro
    public void atack(Monsters alvo) {

        System.out.println("\n" + this.name + " atacou " + alvo.name + "!");

        // Calcula dano da espada menos resistência do inimigo
        int danoFinal = sword.calcularDano() - alvo.resistance;

        // Impede dano negativo
        if (danoFinal < 0) {
            danoFinal = 0;
        }

        // Aplica dano
        alvo.life -= danoFinal;

        System.out.println("Causou " + danoFinal + " de dano!");

        // Verifica se o inimigo morreu
        if (alvo.life <= 0) {
            alvo.life = 0;
            System.out.println(alvo.name + " morreu!");
        } else {
            System.out.println(alvo.name + " agora tem " + alvo.life + " de vida.");
        }
    }

    // Ataque contra múltiplos monstros (ataque em área)
    public void atack(Monsters[] alvos) {

        for (Monsters alvo : alvos) {

            // Divide o dano baseado na quantidade de inimigos
            int danoFinal = sword.damage - alvos.length;

            if (danoFinal < 0) {
                danoFinal = 0;
            }

            alvo.life -= danoFinal;

            System.out.println(this.name + " atacou " + alvo.name +
                               " causando " + danoFinal + " de dano!");

            if (alvo.life <= 0) {
                System.out.println(alvo.name + " morreu!");
            } else {
                System.out.println(alvo.name + " agora tem " + alvo.life + " de vida.");
            }
        }
    }

    // Método para tentar fugir da batalha
    public boolean leave() {

        Random random = new Random();

        // Gera número de 1 a 20 (tipo dado RPG)
        int dado = random.nextInt(20) + 1;

        // Só foge se tirar 19 ou 20
        if (dado > 18) {
            System.out.println("Você fugiu!!");
            return true;
        } else {
            System.out.println("Você não conseguiu fugir!");
            return false;
        }
    }
}