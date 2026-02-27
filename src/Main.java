import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Sword katana = new Sword("Katana", 5, "Rara");
        Character heroi = new Character("Herói", 20, 0, 10, katana);

        Scanner sc = new Scanner(System.in);

        System.out.println("=== MODO FARM INFINITO DE GOBLINS ===");

        while (heroi.life > 0 && heroi.nivel < 10) {

            Goblin goblin = new Goblin("Goblin");

            System.out.println("\nUm novo Goblin apareceu!");

            while (goblin.life > 0 && heroi.life > 0) {

                System.out.println("\nSua vida: " + heroi.life+"/"+heroi.maxLife);
                System.out.println("Vida do Goblin: " + goblin.life+"/"+goblin.maxLife);

                System.out.println("1 - Atacar");
                int escolha = sc.nextInt();

                if (escolha == 1) {
                    heroi.atack(goblin);
                }

                if (goblin.life > 0) {
                    goblin.atack(heroi);
                }
            }

            if (heroi.life > 0) {
                System.out.println("Você derrotou o Goblin!");
                heroi.earnXp(goblin.dropXp); 
                heroi.coin += 5;

                System.out.println("Moedas atuais: " + heroi.coin);
                System.out.println("Nivel atual: " + heroi.nivel);
            }
        }


        sc.close();
    }
}