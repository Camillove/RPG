public class Sword {

    String name;
    int damage;
    String type;

    public Sword(String name, int damage, String type) {
        this.name = name;
        this.damage = damage;
        this.type = type;
    }

    public int calcularDano(int danoBase) {

        java.util.Random random = new java.util.Random();
        int danoTotal = 0;

        if (name.equals("Adaga")) {

            for (int i = 0; i < 2; i++) {

                int danoFinal = danoBase + this.damage;
                int dado = random.nextInt(20) + 1;

                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                danoTotal += danoFinal;
            }
        }

        else if (name.equals("Katana")) {

            int danoFinal = danoBase + this.damage;
            int dado = random.nextInt(20) + 1;

            int multiplicadorCritico = 2;

            if (type.equals("Rara")) {
                multiplicadorCritico = 3;
            }
            if (type.equals("Lendaria")) {
                multiplicadorCritico = 4;
            }

            if (dado == 20) {
                System.out.println("CRÍTICO!");
                danoFinal *= multiplicadorCritico;
            }

            danoTotal = danoFinal;
        }

        else if (name.equals("Espada Longa")) {

            int danoFinal = danoBase + this.damage;
            int dado = random.nextInt(20) + 1;

            int chanceCritico = 20; 

            if (type.equals("Rara")) {
                chanceCritico = 18;
            }
            if (type.equals("Lendaria")) {
                chanceCritico = 15; 
            }

            if (dado >= chanceCritico) {
                System.out.println("CRÍTICO!");
                danoFinal *= 2;
            }

            danoTotal = danoFinal;
        }


        else {

            int danoFinal = danoBase + this.damage;
            int dado = random.nextInt(20) + 1;

            if (dado == 20) {
                System.out.println("CRÍTICO!");
                danoFinal *= 2;
            }

            danoTotal = danoFinal;
        }

        return danoTotal;
    }
}