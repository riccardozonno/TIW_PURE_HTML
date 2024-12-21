package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Immagine;

public class AlbumDAO {
	private final Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	public Album getAllUserAlbum(int id_user) throws SQLException {
		String performedAction = " finding all album of a user by id_user ";
		PreparedStatement statement = null;
		ResultSet result = null;
		Album album = null;
		Album[] allAlbum = null;

		PreparedStatement statement2 = null;
		ResultSet result2 = null;
		Immagine[] immagini = null;

		try {

			String query1 = "SELECT * FROM album WHERE creatore = ?";
			statement = connection.prepareStatement(query1);
			statement.setInt(1, id_user);
			result = statement.executeQuery();
			int i = 0;
			while (result.next()) {
				String titolo = result.getString("titolo");
				int id_user_proprietario = result.getInt("id_user_proprietario");
				Timestamp data = result.getTimestamp("data_creazione");
				int totale_immagini = result.getInt("totale_immagini");

				album = new Album(titolo, id_user_proprietario, totale_immagini, data);
				album.setId_album(result.getInt("id_album"));

				// TODO verificarne correttezza
				String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
				statement2 = connection.prepareStatement(query2);
				statement2.setInt(1, album.getId_album());
				result2 = statement2.executeQuery();
				int j = 0;
				while (result.next()) {
					int id_image_in_album = result2.getInt("id_ima");
					album.setId_immagini(j, id_image_in_album);
					j++;
				}
				for (int k = 0; k < album.getTotale_immagini(); k++) {
					ImmagineDAO imageDao = new ImmagineDAO(connection);
					immagini[k] = imageDao.getImageById(album.getId_immagini()[k]);
				}
				album.setImmagini(immagini);
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
		return album;
	}
}
