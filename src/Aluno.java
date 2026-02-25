public class Aluno {
    String nome;
    int numero;
    public Aluno(String nome,int numero){
        this.nome = nome;
        this.numero = numero;
    }

    public void dobro(){
        System.out.println("Tem nome "+nome+" e o numero "+numero);
    }
}
