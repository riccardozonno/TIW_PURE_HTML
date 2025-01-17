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
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class GoToImagePage
 */
@WebServlet("/GoToImagePage")
public class GoToImagePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToImagePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {

		ServletContext servletContext = getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
		this.connection = ConnectionHandler.getConnection(servletContext);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

        // Verifica se l'utente è loggato
        if (session == null || session.getAttribute("currentUser") == null) {
        	response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }
		
       String imageIdStr = request.getParameter("imageId");
       String albumIdStr = request.getParameter("albumId");
       
       
       int imageId;
       int albumId;
       

       try {
           imageId = Integer.parseInt(imageIdStr);// Converti l'ID in un intero
       } catch (NumberFormatException e) {
    	   imageId = (int)request.getAttribute("imageId");
    	   
       }
       try {
           albumId = Integer.parseInt(albumIdStr);// Converti l'ID in un intero
       } catch (NumberFormatException e) {
    	   albumId = (int)request.getAttribute("albumId");
       }
       

       // Recupera l'oggetto Album dal database 
       ImmagineDAO immagineDAO = new ImmagineDAO(connection); // Supponendo che AlbumDAO gestisca l'accesso ai dati
       Immagine immagine;
		try {
			immagine = immagineDAO.getImageById(imageId);
			System.out.println("GoToImagePage getImageById idImage: " + immagine.getId_immagine());
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

       if (immagine == null) {
           // Se l'album non esiste, invia un errore 404
           response.sendError(HttpServletResponse.SC_NOT_FOUND, "Immagine not found");
           return;
       }
       System.out.println(immagine.getTitolo());

       
       // Salva l'oggetto Album come attributo della richiesta
       request.setAttribute("image", immagine);
       request.setAttribute("albumId", albumId);
       
       // Reindirizza alla pagina albumPage.html (con Thymeleaf per il rendering dinamico)
       forward(request, response,"/WEB-INF/imagePage.html");
   }

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
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