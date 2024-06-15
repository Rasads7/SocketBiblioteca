package Server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String SERVIDOR_IP = "localhost";
    private static final int SERVIDOR_PORTA = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVIDOR_IP, SERVIDOR_PORTA);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            System.out.println("Conectado ao servidor.");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                exibirMenu();
                String escolha = scanner.nextLine().trim();

                switch (escolha) {
                    case "1":
                        out.writeObject("listar");
                        exibirResposta(in);
                        break;
                    case "2":
                        alugarLivro(out, in, scanner);
                        break;
                    case "3":
                        devolverLivro(out, scanner);
                        break;
                    case "4":
                        cadastrarLivro(out, scanner);
                        break;
                    case "0":
                        System.out.println("Encerrando cliente.");
                        out.writeObject("sair");
                        return;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exibirMenu() {
        System.out.println("\nEscolha uma opção:");
        System.out.println("1. Listar livros");
        System.out.println("2. Alugar livro");
        System.out.println("3. Devolver livro");
        System.out.println("4. Cadastrar novo livro");
        System.out.println("0. Sair");
    }

    private static void exibirResposta(ObjectInputStream in) {
        try {
            Object resposta = in.readObject();
            if (resposta instanceof String) {
                System.out.println("\nResposta do servidor:");
                System.out.println((String) resposta);
            } else if (resposta instanceof Livro) {
                System.out.println("\nDetalhes do livro recebido:");
                Livro livro = (Livro) resposta;
                System.out.println("Nome: " + livro.getNome());
                System.out.println("Autor: " + livro.getAutor());
                System.out.println("Gênero: " + livro.getGenero());
                System.out.println("Número de exemplares: " + livro.getNumExemplares());
            } else {
                System.out.println("\nResposta inválida do servidor.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\nErro ao ler resposta do servidor.");
            e.printStackTrace();
        }
    }

    private static void alugarLivro(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) {
        try {
            System.out.println("\nDigite o nome do livro que deseja alugar:");
            String nome = scanner.nextLine().trim();
            out.writeObject("alugar");
            out.writeObject(nome);
            exibirResposta(in);
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }

    private static void devolverLivro(ObjectOutputStream out, Scanner scanner) {
        try {
            System.out.println("\nDigite o nome do livro:");
            String nome = scanner.nextLine().trim();
            System.out.println("Digite o autor do livro:");
            String autor = scanner.nextLine().trim();
            System.out.println("Digite o gênero do livro:");
            String genero = scanner.nextLine().trim();
            System.out.println("Digite o número de exemplares do livro:");
            int numExemplares = Integer.parseInt(scanner.nextLine().trim());

            Livro livroDevolvido = new Livro(autor, nome, genero, numExemplares);
            out.writeObject("devolver");
            out.writeObject(livroDevolvido);
            System.out.println("Livro devolvido com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }

    private static void cadastrarLivro(ObjectOutputStream out, Scanner scanner) {
        try {
            System.out.println("\nDigite o nome do livro:");
            String nome = scanner.nextLine().trim();
            System.out.println("Digite o autor do livro:");
            String autor = scanner.nextLine().trim();
            System.out.println("Digite o gênero do livro:");
            String genero = scanner.nextLine().trim();
            System.out.println("Digite o número de exemplares do livro:");
            int numExemplares = Integer.parseInt(scanner.nextLine().trim());

            Livro novoLivro = new Livro(autor, nome, genero, numExemplares);
            out.writeObject("cadastrar");
            out.writeObject(novoLivro);
            System.out.println("Livro cadastrado com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }
}
