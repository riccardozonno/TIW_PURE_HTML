package it.polimi.tiw.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Immagine;

public class AlbumDAO {
	private final Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Album getAlbumById(int id_album) throws SQLException {
	    String performedAction = " finding an album by id_album ";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    Album album = null;

	    String queryAlbum = "SELECT * FROM album WHERE id_album = ?";
	    String queryImages = "SELECT id_ima FROM image_album WHERE id_alb = ?";

	    try {
	        statement = connection.prepareStatement(queryAlbum);
	        statement.setInt(1, id_album);
	        result = statement.executeQuery();

	        if (result.next()) {
	            album = new Album(
	                result.getInt("id_album"),
	                result.getString("titolo"),
	                result.getInt("creatore"),
	                result.getInt("totale_immagini"),
	                result.getTimestamp("data_creazione")
	            );

	            int totaleImmagini = album.getTotale_immagini();

	            if (totaleImmagini > 0) {
	                int[] id_immagini = new int[totaleImmagini];
	                Immagine[] immagini = new Immagine[totaleImmagini];

	                try (PreparedStatement statement2 = connection.prepareStatement(queryImages)) {
	                    statement2.setInt(1, id_album);
	                    try (ResultSet result2 = statement2.executeQuery()) {
	                        int index = 0;
	                        while (result2.next() && index < totaleImmagini) {
	                            id_immagini[index] = result2.getInt("id_ima");
	                            index++;
	                        }
	                    }
	                }

	                ImmagineDAO imageDao = new ImmagineDAO(connection);
	                for (int i = 0; i < totaleImmagini; i++) {
	                    immagini[i] = imageDao.getImageById(id_immagini[i]);
	                }

	                album.setId_immagini(id_immagini);
	                album.setImmagini(orderImage(immagini));
	                album.setImmagini(immagini);
	            }
	        }
	    } catch (SQLException e) {
	        throw new SQLException("Error accessing the DB when " + performedAction + "[ " + e.getMessage() + " ]");
	    } finally {
	        if (result != null) {
	            try {
	                result.close();
	            } catch (SQLException e) {
	                throw new SQLException("Error closing the result set when " + performedAction + "[ " + e.getMessage() + " ]");
	            }
	        }
	        if (statement != null) {
	            try {
	                statement.close();
	            } catch (SQLException e) {
	                throw new SQLException("Error closing the statement when " + performedAction + "[ " + e.getMessage() + " ]");
	            }
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
		String query = "INSERT INTO album (titolo, creatore) VALUES (?, ?)";
		String query2 = "SELECT id_album FROM album WHERE titolo = ? ORDER BY id_album DESC LIMIT 1";
		ResultSet result;
		PreparedStatement statement2;
		long generatedId = 0;

		try {
			connection.setAutoCommit(false);
			statement = connection.prepareStatement(query);
			statement.setString(1, titolo);
			statement.setInt(2, creatore);
            statement.executeUpdate();
            
            
            statement2 = connection.prepareStatement(query2);
			statement2.setString(1, titolo);
			result = statement2.executeQuery();
			connection.commit();
			if (result.next()) {
			    generatedId = result.getLong("id_album");
			}
			return generatedId;
		}
		 catch (SQLException e) {
			 connection.rollback();
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
			connection.setAutoCommit(true);

		}
	}
 	
    public void updateAlbumImageCount(Connection connection, int albumId) throws SQLException {
    	String performedAction = " updating totale_immagini in album " + Integer.toString(albumId);
        String query = "UPDATE album SET totale_immagini = totale_immagini + 1 WHERE id_album = ?";
        PreparedStatement statement = null;
        try  {
        	connection.setAutoCommit(false);
        	statement = connection.prepareStatement(query);
            statement.setInt(1, albumId);
            statement.executeUpdate();
            connection.commit();
        }catch (SQLException e) {
        	connection.rollback();
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
        } finally {
        	try {
			statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
        	connection.setAutoCommit(true);

 	    }
    }
 	
 	public Album[] getAllOtherUserAlbums(int id_user) throws SQLException {
 	    String performedAction = " finding all albums of users different from id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Album album = null;
 	    Album[] allAlbums = new Album[5];  
 	    PreparedStatement statement2 = null;
 	    ResultSet result2 = null;
 	    try {
 	        String query1 = "SELECT a.*, u.username FROM album a JOIN user u ON a.creatore = u.id_user WHERE a.creatore <> ? ORDER BY a.data_creazione DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();
 	        
 	        int i = 0; 
 	        while (result.next()) {
 	            if (i == allAlbums.length) {
 	                allAlbums = resizeArray(allAlbums);
 	            }
 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("creatore");
 	            Timestamp data = result.getTimestamp("data_creazione");
 	            int totale_immagini = result.getInt("totale_immagini");
 	            int id_album = result.getInt("id_album");
 	            String username_proprietario = result.getString("username"); 
 	            album = new Album(id_album, titolo, id_user_proprietario, totale_immagini, data);
 	            album.setUsernameCreatore(username_proprietario);
 	            if (album.getTotale_immagini() > 0) {
 	                String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
 	                statement2 = connection.prepareStatement(query2);
 	                statement2.setInt(1, album.getId_album());
 	                result2 = statement2.executeQuery();
 	                
 	                int j = 0;
 	                int[] id_immagini = new int[album.getTotale_immagini()];
 	                while (result2.next()) {
 	                    id_immagini[j] = result2.getInt("id_ima");
 	                    j++;
 	                }

 	                Immagine[] immagini = new Immagine[album.getTotale_immagini()];
 	                for (int k = 0; k < album.getTotale_immagini(); k++) {
 	                    ImmagineDAO imageDao = new ImmagineDAO(connection);
 	                    immagini[k] = imageDao.getImageById(id_immagini[k]);
 	                }
 	                album.setImmagini(immagini);
 	            }
 	            allAlbums[i] = album;
 	            i++;
 	        }
 	        if (i == 0) {
 	            return null;
 	        }
 	        allAlbums = trimArray(allAlbums, i);
 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	        closeResources(result2, statement2);
 	    }
 	    return allAlbums;
 	}

 	public Album[] getAllUserAlbum2(int id_user) throws SQLException {
 	    String performedAction = " finding all album of a user by id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Album album = null;
 	    Album[] allAlbum = null;  
 	    PreparedStatement statement2 = null;
 	    ResultSet result2 = null;
 	    boolean alb = false;

 	    try {
 	        String query1 = "SELECT * FROM album WHERE creatore = ? ORDER BY album.data_creazione DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();
 	        int i = 0;  
 	        while (result.next()) {
 	        	if(i==0) {
 	        		alb = true;
 	        		allAlbum = new Album[5];
 	        	}
 	            if (i == allAlbum.length) {
 	                allAlbum = resizeArray(allAlbum);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("creatore");
 	            Timestamp data = result.getTimestamp("data_creazione");
 	            int totale_immagini = result.getInt("totale_immagini");
 	            int id_album = result.getInt("id_album");

 	            album = new Album(id_album, titolo, id_user_proprietario, totale_immagini, data);

 	            if (album.getTotale_immagini() > 0) {
 	                String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
 	                statement2 = connection.prepareStatement(query2);
 	                statement2.setInt(1, album.getId_album());
 	                result2 = statement2.executeQuery();

 	                int j = 0;
 	                int[] id_immagini = new int[album.getTotale_immagini()];
 	                while (result2.next()) {
 	                    id_immagini[j] = result2.getInt("id_ima");
 	                    j++;
 	                }

 	                Immagine[] immagini = new Immagine[album.getTotale_immagini()];
 	                for (int k = 0; k < album.getTotale_immagini(); k++) {
 	                    ImmagineDAO imageDao = new ImmagineDAO(connection);
 	                    immagini[k] = imageDao.getImageById(id_immagini[k]);
 	                }
 	                
 	                for(int a = 0; a < album.getTotale_immagini(); a++) {
 	                }
 	                album.setImmagini(orderImage(immagini));
 	            }
 	            allAlbum[i] = album;
 	            i++;
 	        }
 	        if(alb) {
 	        allAlbum = trimArray(allAlbum, i);
 	        }
 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	        closeResources(result2, statement2);
 	    }

 	    return allAlbum;
 	} 	
 	private Album[] resizeArray(Album[] original) {
 	    int newSize = original.length * 2;
 	    Album[] newArray = new Album[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	private Album[] trimArray(Album[] original, int size) {
 	    Album[] trimmedArray = new Album[size];
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
 	
 	public void decrementAlbumImageCount(int imageId) throws SQLException {
		String performedAction = "updating totale_immagini when deleting a image";
	    String query = "UPDATE album SET totale_immagini = totale_immagini - 1 WHERE id_album = (SELECT id_alb FROM image_album WHERE id_ima = ?)";
	    PreparedStatement statement = null;
	    try {
	    	connection.setAutoCommit(false);
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, imageId);
	        statement.executeUpdate();
	        connection.commit();
	        }catch (SQLException e) {
	        	connection.rollback();
	        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	        } finally {
	        	
	     		statement.close();
	     		connection.setAutoCommit(true);

            }
	}
 	
	private static Immagine[] orderImage(Immagine[] immagini) {
        if (immagini == null || immagini.length == 0) {
            return null;  
        }
        Arrays.sort(immagini, new Comparator<Immagine>() {
            @Override
            public int compare(Immagine img1, Immagine img2) {
                return img2.getData_creazione().compareTo(img1.getData_creazione());
            }
        });
        return immagini;
    }
	
}
