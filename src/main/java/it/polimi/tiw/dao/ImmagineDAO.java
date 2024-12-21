package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.polimi.tiw.beans.Immagine;

public class ImmagineDAO {
	private final Connection connection;

    public ImmagineDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Immagine getImageById(int id_image) throws SQLException {
    	String performedAction = " finding an image by id";
    	PreparedStatement statement = null;
    	ResultSet result = null;
    	Immagine immagine = null;
    	
        try {

            String query = "SELECT * FROM immagine WHERE id_image = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id_image);
            result = statement.executeQuery();   
            if(result.next()) {
                String titolo = result.getString("titolo");
                int proprietario = result.getInt("proprietario");
                String descrizione = result.getString("descrizione");
                Timestamp data = result.getTimestamp("data_c");
                String percorso = result.getString("percorso");
            
            immagine = new Immagine(id_image, titolo, descrizione, percorso, proprietario, data);
            }
    	
        }catch (SQLException e) {
            throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
        } finally {

            try {
                if (result != null) {
                    result.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
            }

            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
            }
        }
    	return immagine;
    }
}