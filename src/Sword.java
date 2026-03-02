public class Sword extends Item {
    
    int damage;
    String type;

    // Construtor da espada
    public Sword(String name,int valor ,int damage, String type,int size) {
        super(name,valor,size);
        this.damage = damage;
        this.type = type;
    }

    // Método responsável por calcular o dano final da arma
    public int calculateDamage() {

        // Gerador de número aleatório (simula dado RPG)
        java.util.Random random = new java.util.Random();

        // Variável que armazenará o dano total final
        int danoTotal = 0;

        switch (name) {

            case "Adaga" -> {
                for (int i = 0; i < 2; i++) {

                    int danoFinal = this.damage;
                    int dado = random.nextInt(20) + 1;

                    if (dado == 20) {
                        System.out.println("CRÍTICO!");
                        danoFinal *= 2;
                    }

                    danoTotal += danoFinal;
                }
            }

            case "Katana" -> {
                int danoFinal = this.damage;
                int dado = random.nextInt(20) + 1;

                int multiplicadorCritico = switch (type) {
                    case "Rara" -> 3;
                    case "Lendaria" -> 4;
                    default -> 2;
                };

                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= multiplicadorCritico;
                }

                danoTotal = danoFinal;
            }

            case "Espada Longa" -> {
                int danoFinal = this.damage;
                int dado = random.nextInt(20) + 1;

                int chanceCritico = switch (type) {
                    case "Rara" -> 18;
                    case "Lendaria" -> 15;
                    default -> 20;
                };

                if (dado >= chanceCritico) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                danoTotal = danoFinal;
            }

            default -> {
                int danoFinal = this.damage;
                int dado = random.nextInt(20) + 1;

                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                danoTotal = danoFinal;
        }
}

        // Retorna o dano total calculado
        return danoTotal;
    }
}