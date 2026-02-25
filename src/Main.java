import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== TESTE VS CODE JAVA ===");

        System.out.print("Digite seu nome: ");
        String nome = sc.nextLine();

        System.out.print("Digite um número: ");
        int numero = sc.nextInt();

        Aluno a1 = new Aluno(nome,numero);
        a1.dobro();

        sc.close();
    }
}