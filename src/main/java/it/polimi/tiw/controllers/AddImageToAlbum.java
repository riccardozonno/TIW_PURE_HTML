package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class AddImageToAlbum
 */
@WebServlet("/AddImageToAlbum")
public class AddImageToAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddImageToAlbum() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
        	response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }
		
        int userId = ((User) session.getAttribute("currentUser")).getId_user();
        int albumId = (int) session.getAttribute("albumId");
        int imageId = Integer.parseInt(request.getParameter("imageId"));
		ImmagineDAO immagineDAO = new ImmagineDAO(connection);
		AlbumDAO albumDAO = new AlbumDAO(connection);
        
        try {
			immagineDAO.linkImageToAlbum(imageId, albumId, userId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Aggiornamento del totale immagini nell'album
        try {
			albumDAO.updateAlbumImageCount(connection, albumId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        response.sendRedirect("GoToAlbumPage?albumId=" + albumId);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
        this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    @Override
    public void destroy() {
    	
		try {
			
			ConnectionHandler.closeConnection(connection);
		
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
	}

}
