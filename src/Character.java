public class Character {
    String name;
    int life;
    int coin;
    int resistance;
    int damage;
    int speed;
    int inventory;
    public Character(String name, int life, int damage, int resistance, int speed) {
        this.name = name;
        this.life = life;
        this.damage = damage;
        this.resistance = resistance;
        this.speed = speed;
        this.coin = 0;
        this.inventory = 0;
    }
    public void atacar(Character alvo) {

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
