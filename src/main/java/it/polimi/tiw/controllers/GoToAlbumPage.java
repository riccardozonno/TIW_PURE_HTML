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

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Immagine;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class GoToAlbumPage
 */
@WebServlet("/GoToAlbumPage")
public class GoToAlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToAlbumPage() {
        super();
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

        // Verifica se l'utente è loggato
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }

		String pageStartStr = request.getParameter("pageStart");
        String albumIdStr = request.getParameter("albumId");
        
        
        if (albumIdStr == null || albumIdStr.isEmpty()) {
        	forwardToErrorPage(request,response,"id album non valido");
            return;
        }
        int userId = ((User) session.getAttribute("currentUser")).getId_user();
        int albumId;

        try {
            albumId = Integer.parseInt(albumIdStr); 
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid album ID");
            return;
        }

         
        AlbumDAO albumDAO = new AlbumDAO(connection); 
        Album album;
		try {
			album = albumDAO.getAlbumById(albumId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

        if (album == null) {

        	forwardToErrorPage(request, response, "Album not found.");
            return;
        }

        int pageStart = 0;
        if (pageStartStr != null && !pageStartStr.isEmpty()) {
            try {
                pageStart = Integer.parseInt(pageStartStr);
            } catch (NumberFormatException e) {
                pageStart = 0;  
            }
        }
        
        
        ImmagineDAO immagineDAO = new ImmagineDAO(connection); 
        Immagine[] immagine;
        try {
			immagine = immagineDAO.getAllUserImageExceptAlbum(userId, albumId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
        
        request.setAttribute("gallery", immagine);
        request.setAttribute("album", album);
        request.getSession().setAttribute("albumId", (int)album.getId_album());
        request.setAttribute("pageStart", pageStart);
        

        forward(request, response,"/WEB-INF/albumPage.html");
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * Forwards to the ErrorPage
	 * 
	 * @param request
	 * @param response
	 * @param error
	 * @throws ServletException
	 * @throws IOException
	 */
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error)
			throws ServletException, IOException {

		request.setAttribute("error", error);
		forward(request, response, "/WEB-INF/error.html");
		return;
	}
	
	/**
	 * Forwards to the specified path
	 * 
	 * @param request
	 * @param response
	 * @param path
	 * @throws ServletException
	 * @throws IOException
	 */
	private void forward(HttpServletRequest request, HttpServletResponse response, String path)
			throws ServletException, IOException {

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());

	}
	


}
