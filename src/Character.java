import java.util.Random;

public class Character {
    String name;
    int life;
    int coin;
    int resistance;
    int speed;
    int inventory;
    Sword sword;
    public Character(String name, int life, int resistance, int speed,Sword sword) {
        this.name = name;
        this.life = life;
        this.resistance = resistance;
        this.speed = speed;
        this.coin = 0;
        this.inventory = 0;
        this.sword = sword;
    }
    
public void atack(Monsters alvo) {

    System.out.println("\n" + this.name + " atacou " + alvo.name + "!");

    int danoFinal = sword.calcularDano() - alvo.resistance;

    if (danoFinal < 0) {
        danoFinal = 0;
    }

    alvo.life -= danoFinal;

    System.out.println("Causou " + danoFinal + " de dano!");

    if (alvo.life <= 0) {
        alvo.life = 0;
        System.out.println(alvo.name + " morreu!");
    } else {
        System.out.println(alvo.name + " agora tem " + alvo.life + " de vida.");
    }
}
    
    public void atack(Monsters[] alvos){
        for (Monsters alvo : alvos) {

            int danoFinal = sword.damage - alvos.length;

            if (danoFinal < 0) {
                danoFinal = 0;
            }

            alvo.life -= danoFinal;

            System.out.println(this.name + " atacou " + alvo.name + " causando " + danoFinal + " de dano!");

            if (alvo.life <= 0) {
                System.out.println(alvo.name + " morreu!");
            } else {
                System.out.println(alvo.name + " agora tem " + alvo.life + " de vida.");
            }
        }

    }
    
    public boolean fugir() {
        Random random = new Random();
        int dado = random.nextInt(20) + 1;

        if (dado > 18) {
            System.out.println("Você fugiu!!");
            return true;
        } else {
            System.out.println("Você não conseguiu fugir!");
            return false;
        }
    }

}
