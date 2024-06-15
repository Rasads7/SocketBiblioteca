package Server;

import java.io.Serializable;

public class Livro implements Serializable {
    private String autor;
    private String nome;
    private String genero;
    private int numExemplares;

    public Livro(String autor, String nome, String genero, int numExemplares) {
        this.autor = autor;
        this.nome = nome;
        this.genero = genero;
        this.numExemplares = numExemplares;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getNumExemplares() {
        return numExemplares;
    }

    public void setNumExemplares(int numExemplares) {
        this.numExemplares = numExemplares;
    }

    @Override
    public String toString() {
        return "Livro{" +
                "autor='" + autor + '\'' +
                ", nome='" + nome + '\'' +
                ", genero='" + genero + '\'' +
                ", numExemplares=" + numExemplares +
                '}';
    }
}