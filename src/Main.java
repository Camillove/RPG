import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        
        
        Sword katana = new Sword("Katana", 5, "Rara");
        Scanner sc = new Scanner(System.in);

        Character heroi = new Character("Herói", 30, 0, 10,katana);
        Goblin goblin = new Goblin("Goblin Sombrio");

        System.out.println("⚔ BATALHA COMEÇOU ⚔");

        while (heroi.life > 0 && goblin.life > 0) {

            System.out.println("\nSua vida: " + heroi.life);
            System.out.println("Vida do inimigo: " + goblin.life);

            System.out.println("1 - Atacar");
            System.out.println("2 - Fugir");

            int escolha = sc.nextInt();
            switch (escolha) {

                case 1:
                    heroi.atack(goblin);
                    break;

                case 2:
                    if (heroi.fugir()) {
                        break;
                    }
                    break;

                default:
                    System.out.println("Opção inválida!");
                    break;
            }

            if (goblin.life > 0) {
                goblin.atack(heroi);
            }
        }

        if (heroi.life <= 0) {
            System.out.println("Você morreu... Fim de jogo.");
        } else if (goblin.life <= 0) {
            System.out.println("Você venceu!");
            heroi.coin += goblin.dropCoin;
            System.out.println("Você ganhou " + goblin.dropCoin + " moedas!");
        }

        sc.close();
    }
}