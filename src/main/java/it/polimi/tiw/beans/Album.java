package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Album {

	private int id_album;
	private String titolo;
	private Timestamp data_creazione;
	private int id_user_proprietario;
	private int totale_immagini;
	private int[] id_immagini;
	private Immagine[] immagini;
 
	public Album(String nome, int id_user, int totale_immagini, Timestamp data_creazione) {
		this.titolo = nome;
		this.id_user_proprietario = id_user;
		this.totale_immagini = totale_immagini;
		this.data_creazione = data_creazione;
	}

	public Timestamp getData_creazione() {
		return data_creazione;
	}

	public void setData_creazione(Timestamp data_creazione) {
		this.data_creazione = data_creazione;
	}
	
	public Immagine[] getImage() {
		return immagini;
	}
	
	public void setImmagini(Immagine[] immagini) {
		this.immagini = immagini;
	}
	public int getTotale_immagini() {
		return totale_immagini;
	}
	
	public void setTotale_immagini(int totale_immagini) {
		this.totale_immagini = totale_immagini;
	}
	public int getId_album() {
		return id_album;
	}

	public void setId_album(int id_album) {
		this.id_album = id_album;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public int getId_user_proprietario() {
		return id_user_proprietario;
	}

	public void setId_user_proprietario(int id_user_proprietario) {
		this.id_user_proprietario = id_user_proprietario;
	}

	public int[] getId_immagini() {
		return id_immagini;
	}

	public void setId_immagini(int index, int id_immagine) {
		this.id_immagini[index] = id_immagine;
	}
}