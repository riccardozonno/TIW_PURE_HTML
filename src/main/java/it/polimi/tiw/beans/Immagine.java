package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Immagine {
    private Timestamp data_creazione;
    private int id_immagine;
    private String titolo;
    private String descrizione;
    private String percorso;
    private int id_user_proprietario;

    public Immagine(int id,String nome, String descrizione, String path, int id_user, Timestamp data_creazione) {
        this.id_immagine = id;
    	this.titolo = nome;
        this.descrizione = descrizione;
        this.percorso = path;
        this.id_user_proprietario = id_user;
        this.data_creazione = data_creazione;
    }

    public Timestamp getData_creazione() {
        return data_creazione;
    }

    public void setData_creazione(Timestamp data_creazione) {
        this.data_creazione = data_creazione;
    }

    public int getId_immagine() {
        return id_immagine;
    }

    public void setId_immagine(int id_immagine) {
        this.id_immagine = id_immagine;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getPercorso() {
        return percorso;
    }

    public void setPercorso(String percorso) {
        this.percorso = percorso;
    }

    public int getId_user_proprietario() {
        return id_user_proprietario;
    }

    public void setId_user_proprietario(int id_user_proprietario) {
        this.id_user_proprietario = id_user_proprietario;
    }
}