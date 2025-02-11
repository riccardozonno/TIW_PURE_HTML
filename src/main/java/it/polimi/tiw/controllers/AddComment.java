package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
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
import it.polimi.tiw.dao.CommentoDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class AddComment
 */
@WebServlet("/AddComment")
public class AddComment extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddComment() {
        super();
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
        
        String username = ((User) session.getAttribute("currentUser")).getUsername();
        
        String commento = request.getParameter("testo");
        if (commento == null || commento.isEmpty()) {
        	forwardToErrorPage(request,response,"il commento inserito è vuoto");
            return;
        }
		
		
		int id_user = ((User) session.getAttribute("currentUser")).getId_user();
		
		if (request.getParameter("imageId") == null) {
        	forwardToErrorPage(request,response,"id immagine non valido");
            return;
        }
		int id_immagine = Integer.parseInt(request.getParameter("imageId"));
		if (id_immagine <= 0) {
        	forwardToErrorPage(request,response,"id immagine non valido");
            return;
        }
		CommentoDAO commentoDAO = new CommentoDAO(connection);
		ImmagineDAO immagineDAO = new ImmagineDAO(connection);
		try {
			commentoDAO.createCommento(commento, id_user, id_immagine, username);
			immagineDAO.updateTotCommentiByImageId(id_immagine);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
		}
		int id_album = Integer.parseInt(request.getParameter("albumId"));
		request.setAttribute("imageId", id_immagine);
		request.setAttribute("albumId",id_album );
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/GoToImagePage");
	    dispatcher.forward(request, response);
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
    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{

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
    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }
    
    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
        this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    public void destroy() {

        try {

            ConnectionHandler.closeConnection(connection);

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }




}
