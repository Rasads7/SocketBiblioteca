package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Servidor {
    private static final int PORTA = 12345;
    private static final String ARQUIVO_JSON = "Server/livros.json";
    private static List<Livro> acervo = new ArrayList<>();

    public static void main(String[] args) {
        carregarAcervoDoArquivo();

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado. Aguardando conexões...");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket);

                // Criar uma thread para tratar cada conexão de cliente
                new Thread(new ClienteHandler(clienteSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarAcervoDoArquivo() {
        try {
            File arquivo = new File(ARQUIVO_JSON);

            if (arquivo.exists()) {
                Scanner scanner = new Scanner(arquivo);

                StringBuilder jsonBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    jsonBuilder.append(scanner.nextLine());
                }
                scanner.close();

                // Processar o conteúdo do arquivo JSON
                String jsonContent = jsonBuilder.toString();
                if (!jsonContent.isEmpty()) {
                    String[] livrosArray = jsonContent.split("\\},\\s*\\{");

                    for (String livroStr : livrosArray) {
                        String cleanStr = livroStr.replaceAll("[{}\"]", "");
                        String[] parts = cleanStr.split(",");
                        String autor = getValue(parts[0]);
                        String nome = getValue(parts[1]);
                        String genero = getValue(parts[2]);
                        int numExemplares = Integer.parseInt(getValue(parts[3]));

                        Livro livro = new Livro(autor, nome, genero, numExemplares);
                        acervo.add(livro);
                    }
                }
            } else {
                System.out.println("Arquivo JSON não encontrado. Criando acervo vazio.");
                salvarAcervoNoArquivo();
            }

            System.out.println("Acervo carregado do arquivo JSON.");
        } catch (IOException e) {
            System.out.println("Erro ao carregar acervo do arquivo JSON. Criando acervo vazio.");
            salvarAcervoNoArquivo();
        }
    }

    private static String getValue(String part) {
        return part.split(":")[1].trim();
    }

    private static void salvarAcervoNoArquivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_JSON))) {
            for (Livro livro : acervo) {
                writer.println(String.format("{\"autor\":\"%s\",\"nome\":\"%s\",\"genero\":\"%s\",\"numExemplares\":%d}",
                        livro.getAutor(), livro.getNome(), livro.getGenero(), livro.getNumExemplares()));
            }
            System.out.println("Acervo salvo no arquivo JSON.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private final Socket clienteSocket;

        public ClienteHandler(Socket socket) {
            this.clienteSocket = socket;
        }

        @Override
        public void run() {
            try (
                ObjectOutputStream out = new ObjectOutputStream(clienteSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clienteSocket.getInputStream())
            ) {
                System.out.println("Thread iniciada para o cliente: " + clienteSocket);

                while (true) {
                    // Receber comando do cliente
                    Object entrada = in.readObject();
                    if (entrada instanceof String) {
                        String comando = (String) entrada;
                        switch (comando) {
                            case "listar":
                                out.writeObject(listarLivros());
                                break;
                            case "alugar":
                                alugarLivro(in, out);
                                break;
                            case "devolver":
                                devolverLivro(in, out);
                                break;
                            case "cadastrar":
                                cadastrarLivro(in);
                                break;
                            case "sair":
                                System.out.println("Cliente desconectado: " + clienteSocket);
                                return;
                            default:
                                out.writeObject("Comando inválido.");
                                break;
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clienteSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String listarLivros() {
            StringBuilder listaLivros = new StringBuilder();
            for (Livro livro : acervo) {
                listaLivros.append(livro.getNome()).append(" - ").append(livro.getAutor()).append(" - ").append(livro.getNumExemplares()).append(" exemplares\n");
            }
            return listaLivros.toString();
        }

        private void alugarLivro(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            String nomeLivro = (String) in.readObject();
            Livro livroParaAlugar = null;

            for (Livro livro : acervo) {
                if (livro.getNome().equalsIgnoreCase(nomeLivro)) {
                    livroParaAlugar = livro;
                    break;
                }
            }

            if (livroParaAlugar != null && livroParaAlugar.getNumExemplares() > 0) {
                livroParaAlugar.setNumExemplares(livroParaAlugar.getNumExemplares() - 1);
                if (livroParaAlugar.getNumExemplares() == 0) {
                    acervo.remove(livroParaAlugar);
                }
                salvarAcervoNoArquivo();
                out.writeObject("Livro alugado com sucesso.");
            } else {
                out.writeObject("Livro não disponível para aluguel.");
            }
        }

        private void devolverLivro(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            Livro livroDevolvido = (Livro) in.readObject();
            boolean livroExiste = false;

            for (Livro livro : acervo) {
                if (livro.getNome().equalsIgnoreCase(livroDevolvido.getNome())) {
                    livro.setNumExemplares(livro.getNumExemplares() + 1);
                    livroExiste = true;
                    break;
                }
            }

            if (!livroExiste) {
                acervo.add(livroDevolvido);
            }

            salvarAcervoNoArquivo();
            out.writeObject("Livro devolvido com sucesso.");
        }

        private void cadastrarLivro(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object entrada = in.readObject();
            if (entrada instanceof Livro) {
                Livro novoLivro = (Livro) entrada;
                acervo.add(novoLivro);
                salvarAcervoNoArquivo();
                System.out.println("Novo livro cadastrado: " + novoLivro.getNome());
            } else {
                System.out.println("Dados inválidos para cadastro de livro.");
            }
        }
    }
}