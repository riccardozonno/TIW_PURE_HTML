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
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class CreateAlbum
 */
@WebServlet("/CreateAlbum")
public class CreateAlbum extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateAlbum() {
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
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

        // Verifica se l'utente è loggato
        if (session == null || session.getAttribute("currentUser") == null) {
        	response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }

    	Album album = createAlbum(request, response);
        if (album == null) {
            return;
        }
        request.getSession().setAttribute("myAlbums", album);

        response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");
    }


    /**
     * Verifies the input and if it is correct creates the user in the DB and an account of the user.
     * If the operation is successful returns 0, else returns null
     *
     * @param request
     * @param response
     * @return User
     * @throws ServletException
     * @throws IOException
     */
    private Album createAlbum(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String titolo = request.getParameter("title");
        User user = (User) (request.getSession().getAttribute("currentUser"));
        

        if( titolo == null || titolo.isEmpty()) {
            forwardToErrorPage(request, response, "Missing album title");
            return null;
        }

        

        if (titolo.length() <= 0 || titolo.length() > 49) {
            request.setAttribute("warning", "Chosen title invalid (a valid title has more than one character and less than 50)!");
            forward(request, response, "/WEB-INF/home.html");
            return null;
        }
        
        AlbumDAO albumDAO = new AlbumDAO(connection);
        Album album = null;
        try {


            albumDAO.createAlbum( titolo, user.getId_user());

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        // Gets the created user in the DB to verify it has been correctly created in the DB,
        // else if an Exception is raised forwards to the ErrorPage
        try {

        	album = albumDAO.getAlbumByTitolo(titolo);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(album == null) {

            forwardToErrorPage(request, response, "Error: album non correctly created - HomePage!");
            return null;
        }

        return album;
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


}