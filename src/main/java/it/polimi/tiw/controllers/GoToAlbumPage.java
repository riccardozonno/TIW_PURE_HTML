package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

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
        // TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		 // Ottieni l'ID dell'album dai parametri della richiesta
		String pageStartStr = request.getParameter("pageStart");
        String albumIdStr = request.getParameter("albumId");
        int userId = ((User) session.getAttribute("currentUser")).getId_user();
        System.out.println(albumIdStr);
        int albumId;

        try {
            albumId = Integer.parseInt(albumIdStr); // Converti l'ID in un intero
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid album ID");
            return;
        }

        // Recupera l'oggetto Album dal database 
        AlbumDAO albumDAO = new AlbumDAO(connection); // Supponendo che AlbumDAO gestisca l'accesso ai dati
        Album album;
		try {
			album = albumDAO.getAlbumById(albumId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

        if (album == null) {
            // Se l'album non esiste, invia un errore 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found");
            return;
        }
        System.out.println(album.getTitolo());

        int pageStart = 0;
        if (pageStartStr != null && !pageStartStr.isEmpty()) {
            try {
                pageStart = Integer.parseInt(pageStartStr);
            } catch (NumberFormatException e) {
                pageStart = 0;  // Se non valido, imposto a 0
            }
        }
        
        // Recupera le foto non in quell'album, sempre dell'utente
        ImmagineDAO immagineDAO = new ImmagineDAO(connection); // Supponendo che AlbumDAO gestisca l'accesso ai dati
        Immagine[] immagine;
        try {
			immagine = immagineDAO.getAllUserImageExceptAlbum(userId, albumId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
        // Salva l'oggetto Album come attributo della richiesta
        request.setAttribute("gallery", immagine);
        request.setAttribute("album", album);
        request.getSession().setAttribute("albumId", (int)album.getId_album());
        request.setAttribute("pageStart", pageStart);
        
        // Reindirizza alla pagina album
        forward(request, response,"/WEB-INF/albumPage.html");
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
