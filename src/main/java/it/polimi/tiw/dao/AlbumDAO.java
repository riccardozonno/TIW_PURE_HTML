package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Immagine;
import it.polimi.tiw.beans.User;

public class AlbumDAO {
	private final Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	public Album[] getAllUserAlbum(int id_user) throws SQLException {
		String performedAction = " finding all album of a user by id_user ";
		PreparedStatement statement = null;
		ResultSet result = null;
		Album album = null;
		Album[] allAlbum = new Album[3];
		
		
		//TODO URGENTE capire come cambaire in modo dinamico creazione array album
		

		PreparedStatement statement2 = null;
		ResultSet result2 = null;
		Immagine[] immagini = null;

		try {

			String query1 = "SELECT * FROM album WHERE creatore = ?";
			statement = connection.prepareStatement(query1);
			statement.setInt(1, id_user);
			result = statement.executeQuery();
			int i = 0;
			while (result.next() && result != null) {						
				String titolo = result.getString("titolo");
				int id_user_proprietario = result.getInt("creatore");
				Timestamp data = result.getTimestamp("data_creazione");
				int totale_immagini = result.getInt("totale_immagini");
				int id_album = result.getInt("id_album");

				album = new Album(id_album, titolo, id_user_proprietario, totale_immagini, data);
				album.setId_album(result.getInt("id_album"));

				// TODO verificarne correttezza
				if(album.getTotale_immagini() != 0) {
				String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
				statement2 = connection.prepareStatement(query2);
				statement2.setInt(1, album.getId_album());
				result2 = statement2.executeQuery();
				int j = 0;
				while (result.next()) {
					int id_image_in_album = result2.getInt("id_ima");
					album.setId_immagini(j, id_image_in_album);
					j++;
					
					//TODO capire se bisogna prendere anche info user che ha aggiunto foto all'album
				}
				for (int k = 0; k < album.getTotale_immagini(); k++) {
					ImmagineDAO imageDao = new ImmagineDAO(connection);
					immagini[k] = imageDao.getImageById(album.getId_immagini()[k]);
					
				}
				}
				album.setImmagini(immagini);
				System.out.println(album.getId_album());
				allAlbum[i] = album;
				i++;
				}
			

		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {

			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return allAlbum;
	}
	
	public Album getAlbumById(int id_album) throws SQLException{
		
		String performedAction = " finding an album by id_album ";
		PreparedStatement statement = null;
		ResultSet result = null;
		Album album = null;
		String query = "SELECT * FROM album WHERE id_album = ?";
		String id_alb = String.valueOf(id_album);

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, id_alb);
			result = statement.executeQuery();
			if (result.next()) {
				album = new Album(result.getInt("id_album"),result.getString("titolo"), result.getInt("creatore"), result.getInt("totale_immagini"), result.getTimestamp("data_creazione"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return album;
		
	}
	
	public Album getAlbumByTitolo(String titolo) throws SQLException{
		
		String performedAction = " finding an album by titolo ";
		PreparedStatement statement = null;
		ResultSet result = null;
		Album album = null;
		String query = "SELECT * FROM album WHERE titolo = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, titolo);
			result = statement.executeQuery();
			if (result.next()) {
				album = new Album(result.getInt("id_album"), result.getString("titolo"), result.getInt("creatore"), result.getInt("totale_immagini"), result.getTimestamp("data_creazione"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return album;
		
	}
	
 	public long createAlbum(String titolo, int creatore) throws SQLException{
		String performedAction = " creating an album ";
		PreparedStatement statement = null;
		String query = "INSERT INTO album (titolo, creatore, data_creazione) VALUES (?, ?, ?)";

		try {

			statement = connection.prepareStatement(query);
			statement.setString(1, titolo);
			statement.setInt(2, creatore);
			Instant instant = Instant.now();
			Timestamp date =  Timestamp.from(instant);
			statement.setTimestamp(3, date);
            int affectedRows = statement.executeUpdate();
			
			
			if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long generatedId = generatedKeys.getLong(1); 
                        
                        return generatedId;
                    }
                }
            }
            
        

		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {

			try {

				statement.close();

			} catch (Exception e) {

				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return 0;
	}

 	public Album[] getAllOtherUserAlbum(int id_user) throws SQLException {
 		Album[] otherAlbums = null;
 		
 		
 		//TODO copiare da getAllUserAlbum invertendo la query con un except sull'id del current_user
 		
 		
 		return null;
 		
}
}
