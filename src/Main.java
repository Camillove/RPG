import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== A SOMBRA DE VALDORIA ===");
        System.out.println("\nVocê acorda em uma vila destruída pelo caos.");
        System.out.println("O céu está vermelho e o cheiro de fumaça domina o ar.");
        System.out.println("Um velho mercante se aproxima lentamente...");

        Sword espadaInicial = new Sword("Espada longa", 50, 5, "comum", 5);
        Character heroi = new Character("Herói", 30, 3, espadaInicial);

        heroi.coin = 20;

// =========================
// MERCANTE
// =========================
System.out.println("\nMercante: Jovem guerreiro... tenho itens para ajudá-lo.");
System.out.println("Você possui " + heroi.coin + " moedas.");
System.out.println("1 - Poção (10 moedas)");
System.out.println("2 - Melhorar espada (+5 dano) (15 moedas)");
System.out.println("3 - Trocar de espada");
System.out.println("4 - Sair");

int escolhaMercante = sc.nextInt();

if (escolhaMercante == 1 && heroi.coin >= 10) {
    Item pocao = new Item("Poção", 20, 2);
    heroi.inventory.addItem(pocao);
    heroi.coin -= 10;
    System.out.println("Você comprou uma poção.");
} 
else if (escolhaMercante == 2 && heroi.coin >= 15) {
    heroi.sword.damage += 5;
    heroi.coin -= 15;
    System.out.println("Sua espada brilha com novo poder!");
} 
else if (escolhaMercante == 3) {
    System.out.println("\nEspadas disponíveis:");
    Sword adaga = new Sword("Adaga", 40, 4, "comum", 4);
    Sword espada2 = new Sword("Espada larga", 60, 6, "rara", 6);
    System.out.println("1 - " + adaga.name + " (Dano: " + adaga.damage + ")");
    System.out.println("2 - " + espada2.name + " (Dano: " + espada2.damage + ")");
    int escolhaEspada = sc.nextInt();

    if (escolhaEspada == 1) {
        heroi.sword = adaga;
    } else if (escolhaEspada == 2) {
        heroi.sword = espada2;
    }
    System.out.println("Você agora está usando: " + heroi.sword.name);
} 
else {
    System.out.println("Você segue seu caminho.");
}

        // =========================
        // PRIMEIRA LUTA
        // =========================
        System.out.println("\nUm Goblin salta das sombras!");

        Monsters goblin = new Goblin();

        batalha(heroi, goblin, sc);

        if (heroi.life <= 0) {
            System.out.println("\nVocê caiu na batalha...");
            return;
        }

        heroi.coin += goblin.dropCoin;
        heroi.earnXp(goblin.dropXp);

        // =========================
        // ESCOLHA DE CAMINHO
        // =========================
        System.out.println("\nApós a batalha, você encontra dois caminhos:");
        System.out.println("1 - Floresta Sombria");
        System.out.println("2 - Ruínas Antigas");

        int caminho = sc.nextInt();

        Monsters inimigo;

        if (caminho == 1) {
            System.out.println("\nVocê entra na Floresta Sombria...");
            inimigo = new Goblin_exp();
        } else {
            System.out.println("\nVocê explora as Ruínas Antigas...");
            inimigo = new Goblin_exp();
        }

        batalha(heroi, inimigo, sc);

        if (heroi.life <= 0) {
            System.out.println("\nSeu corpo cai ao chão frio...");
            return;
        }

        heroi.coin += inimigo.dropCoin;
        heroi.earnXp(inimigo.dropXp);

        // =========================
        // CHEFE FINAL
        // =========================
        System.out.println("\nUma presença monstruosa surge...");
        System.out.println("O Senhor das Sombras aparece!");

        Monsters boss = new Goblin_boss();

        batalha(heroi, boss, sc);

        if (heroi.life > 0) {
            System.out.println("\nVocê derrotou o Senhor das Sombras!");
            System.out.println("Valdoria está salva.");
            System.out.println("Você se tornou uma lenda.");
        } else {
            System.out.println("\nO mundo mergulha na escuridão...");
        }

        sc.close();
    }

    // =========================
    // MÉTODO DE BATALHA
    // =========================
    public static void batalha(Character heroi, Monsters monstro, Scanner sc) {

        while (monstro.life > 0 && heroi.life > 0) {

            System.out.println("\n1 - Atacar");
            System.out.println("2 - Fugir");

            int escolha = sc.nextInt();

            if (escolha == 1) {

                heroi.atack(monstro);

                if (monstro.life > 0) {
                    heroi.life -= monstro.damage;
                    System.out.println(monstro.name + " atacou! Você tem "
                            + heroi.life + " de vida.");
                }

            } else {

                if (heroi.leave()) {
                    break;
                } else {
                    heroi.life -= monstro.damage;
                    System.out.println("Você falhou ao fugir!");
                }
            }
        }
    }
}