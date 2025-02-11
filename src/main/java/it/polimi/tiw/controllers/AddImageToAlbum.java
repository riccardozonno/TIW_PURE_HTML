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
import org.thymeleaf.context.WebContext;

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
    private Connection connection;
    private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddImageToAlbum() {
        super();
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
        
        if (request.getParameter("imageId") == null || request.getParameter("imageId").isEmpty()) {
        	forwardToErrorPage(request,response,"id immagine non valido");
            return;
        }
        int imageId = Integer.parseInt(request.getParameter("imageId"));
		ImmagineDAO immagineDAO = new ImmagineDAO(connection);
		AlbumDAO albumDAO = new AlbumDAO(connection);
        
        try {
			immagineDAO.linkImageToAlbum(imageId, albumId, userId);
		} catch (SQLException e) {
			e.printStackTrace();
		}

        try {
			albumDAO.updateAlbumImageCount(connection, albumId);
		} catch (SQLException e) {
			e.printStackTrace();
		}

        response.sendRedirect("GoToAlbumPage?albumId=" + albumId);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.connection = ConnectionHandler.getConnection(servletContext);
        this.templateEngine = TemplateHandler.getEngine(servletContext, getServletInfo());
    }
    
    @Override
    public void destroy() {
    	
		try {
			
			ConnectionHandler.closeConnection(connection);
		
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
	}
    
    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{

        request.setAttribute("error", error);
        forward(request, response, "/WEB-INF/error.html");
        return;
    }


    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }
    

}
