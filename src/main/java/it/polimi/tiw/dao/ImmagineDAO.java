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
    

	public Immagine[] getAllUserPhoto(int id_user) throws SQLException {
 	    String performedAction = " finding all album of a user by id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Immagine immagine = null;
 	    Immagine[] allPhotos = new Immagine[5];  // Inizializzo l'array con 5 posizioni

 	    try {
 	        String query1 = "SELECT * FROM immagine WHERE proprietario = ? ORDER BY immagine.data_c DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();

 	        int i = 0;  // Indice per scorrere l'array

 	        while (result.next()) {
 	        	
 	            // Se l'array è pieno, lo raddoppio
 	            if (i == allPhotos.length) {
 	                allPhotos = resizeArray(allPhotos);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("proprietario");
 	            Timestamp data = result.getTimestamp("data_c");
 	            String descrizione= result.getString("descrizione");
 	            String percorso = result.getString("percorso");
 	            int id_immagine = result.getInt("id_image");

 	            immagine = new Immagine(id_immagine, titolo, descrizione, percorso, id_user_proprietario, data);
 	            
 	            allPhotos[i] = immagine;
 	            i++;
 	        }

 	        // Ridimensiono l'array alla dimensione esatta
 	        allPhotos = trimArray(allPhotos, i);

 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	    }

 	    return allPhotos;
 	}

 // Metodo per raddoppiare la dimensione dell'array
 	private Immagine[] resizeArray(Immagine[] original) {
 	    int newSize = original.length * 2;
 	    Immagine[] newArray = new Immagine[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	// Metodo per tagliare l'array alla dimensione esatta
 	private Immagine[] trimArray(Immagine[] original, int size) {
 	    Immagine[] trimmedArray = new Immagine[size];
 	    System.arraycopy(original, 0, trimmedArray, 0, size);
 	    return trimmedArray;
 	}
 	
 	private void closeResources(ResultSet resultSet, PreparedStatement statement) throws SQLException {
 	    if (resultSet != null) {
 	        try {
 	            resultSet.close();
 	        } catch (SQLException e) {
 	            throw new SQLException("Error closing ResultSet: " + e.getMessage());
 	        }
 	    }
 	    if (statement != null) {
 	        try {
 	            statement.close();
 	        } catch (SQLException e) {
 	            throw new SQLException("Error closing PreparedStatement: " + e.getMessage());
 	        }
 	    }
 	}


	
}