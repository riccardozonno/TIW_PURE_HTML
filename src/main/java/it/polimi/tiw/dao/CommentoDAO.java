package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.polimi.tiw.beans.Commento;
import it.polimi.tiw.beans.Immagine;

public class CommentoDAO {

	private final Connection connection;

    public CommentoDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Commento getCommentoById(int id) throws SQLException {
		String performedAction = " finding a comment by id_comm ";
		String query = "SELECT * FROM commento WHERE id_comm = ?";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    Commento commento = null;
	    
	    try {
 	        statement = connection.prepareStatement(query);
 	        statement.setInt(1, id);
 	        result = statement.executeQuery();

 	        if (result.next()) 
 	        	commento = new Commento(id, result.getString("testo"), result.getInt("proprietario"), result.getInt("id_im"), result.getTimestamp("data_c"));
 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when " + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        // Chiude il ResultSet
 	        if (result != null) {
 	            try {
 	                result.close();
 	            } catch (SQLException e) {
 	                throw new SQLException("Error closing the result set when " + performedAction + "[ " + e.getMessage() + " ]");
 	            }
 	        }

 	        // Chiude il PreparedStatement
 	        if (statement != null) {
 	            try {
 	                statement.close();
 	            } catch (SQLException e) {
 	                throw new SQLException("Error closing the statement when " + performedAction + "[ " + e.getMessage() + " ]");
 	            }
 	        }
 	    }
	    return commento;
    }

    
    // Metodo per raddoppiare la dimensione dell'array
 	private Commento[] resizeArray(Commento[] original) {
 	    int newSize = original.length * 2;
 	    Commento[] newArray = new Commento[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	// Metodo per tagliare l'array alla dimensione esatta
 	private Commento[] trimArray(Commento[] original, int size) {
 	    Commento[] trimmedArray = new Commento[size];
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
