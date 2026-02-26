

public class Monsters {
    //Definição das variavei padrões dos montros
    String name;
    int life;
    int damage;
    int dropCoin;
    int dropXp;
    int speed;
    int resistance;

    public Monsters(String name, int life, int damage, int dropCoin, int dropXp, int speed,int resistance) {
        this.name = name;
        this.life = life;
        this.damage = damage;
        this.dropCoin = dropCoin;
        this.dropXp = dropXp;
        this.speed = speed;
        this.resistance = resistance;
    }
    public void atack(Character alvo) {

        int danoFinal = this.damage - alvo.resistance;

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
//So vai poder colcar nome o resto é padrão
class Goblin extends Monsters {
    public Goblin(String name) {
        super(name, 8, 2, 3, 2, 20,0);
    }
}
//So vai poder colcar nome o resto é padrão
class Goblin_exp extends Monsters{
    public Goblin_exp(String name) {
        super(name, 12, 3, 5, 4, 18,0);
    }
}
//So vai poder colcar nome o resto é padrão ... falta adicionar o drop da poção
class Goblin_boss extends Monsters{
    public Goblin_boss(String name) {
        super(name, 25, 5, 10, 10, 16,4);
    }
}
