package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.polimi.tiw.beans.Commento;
import it.polimi.tiw.beans.Immagine;

public class ImmagineDAO {
	private final Connection connection;

    public ImmagineDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Immagine getImageById(int id_image) throws SQLException {
    	String performedAction = " finding an image by id";
    	String performedAction2 = " finding all comment of a image by id_image ";
    	String query = "SELECT * FROM immagine WHERE id_image = ?";
    	String query2 = "SELECT * FROM commento WHERE id_image = ?";
    	PreparedStatement statement = null;
    	PreparedStatement statement2 = null;
    	ResultSet result = null;
    	ResultSet result2 = null;
    	
    	Immagine immagine = null;
    	CommentoDAO commentoDAO = new CommentoDAO(connection);
    	
        try {

            
            statement = connection.prepareStatement(query);
            statement.setInt(1, id_image);
            result = statement.executeQuery();   
            if(result.next()) {
                String titolo = result.getString("titolo");
                int proprietario = result.getInt("proprietario");
                String descrizione = result.getString("descrizione");
                Timestamp data = result.getTimestamp("data_c");
                String percorso = result.getString("percorso");
                int totaleCommenti = result.getInt("totale_commenti");
                Commento[] commenti = null;
                
	            // Inizializza l'array di ID delle immagini
	            if (totaleCommenti > 0) {
	                int[] id_comm = new int[totaleCommenti];
	                 commenti = new Commento[totaleCommenti];

	                
	                	statement2 = connection.prepareStatement(query2);
	                    statement2.setInt(1, id_image);
	                    result2 = statement2.executeQuery();
	                        int index = 0;
	                        while (result2.next() && index < totaleCommenti) {
	                            id_comm[index] = result2.getInt("id_ima");
	                            index++;
	                    }
	                
	                // Carica le immagini dal database
	                commentoDAO = new CommentoDAO(connection);
	                for (int i = 0; i < totaleCommenti; i++) {
	                    commenti[i] = CommentoDAO.getCommentoById(id_comm[i]);
	                }

	                
	                immagine.setCommenti(commenti);
	            }
            
            immagine = new Immagine(id_image, titolo, descrizione, percorso, proprietario, data, commenti );
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
 	    String performedAction = " finding all image of a user by id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Immagine immagine = null;
 	    Immagine[] allPhotos = new Immagine[5];  // Inizializzo l'array con 5 posizioni
 	    CommentoDAO commentoDAO = new CommentoDAO(connection);

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
 	            Commento[] commenti = commentoDAO.getCommentoById(id_immagine);
 	            

 	            immagine = new Immagine(id_immagine, titolo, descrizione, percorso, id_user_proprietario, data, commenti);
 	            
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

	public void linkImageToAlbum(Connection connection, int imageId, int albumId, int userId) throws SQLException {
		String performedAction = "create record in image_album to link an image to an album";
        String query = "INSERT INTO image_album (id_ima, id_alb, id_use) VALUES (?, ?, ?)";
        PreparedStatement statement = null;
        try  {
        	statement = connection.prepareStatement(query);
            statement.setInt(1, imageId);
            statement.setInt(2, albumId);
            statement.setInt(3, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	    }
    }
	
    public int insertImageData(Connection connection, String title, String description, String filePath, int userId) throws SQLException {
    	String performedAction = "create a new image";
        String query = "INSERT INTO immagine (titolo, percorso, descrizione, proprietario) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = null;
   
        try  {
        	statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, filePath);
            statement.setString(3, description);
            statement.setInt(4, userId);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                	throw new SQLException("Errore nell'inserimento dell'immagine, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	    }
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