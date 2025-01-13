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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.UserDAO;
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
        // TODO Auto-generated method stub
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Tries to register the user, if the function returns null ends (registerUser already re-directed)
    	Album album = createAlbum(request, response);
        if (album == null) {
            return;
        }
        request.getSession().setAttribute("myAlbums", album);

        // Once the user is registered is redirected to the LoginPage
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

        // Gets the parameters of the request and verifies if they are in the correct format (length and syntax)
        String titolo = request.getParameter("title");
        User user = (User) (request.getSession().getAttribute("currentUser"));
        

        // Verifies if all parameters are not null
        if( titolo == null) {

            forwardToErrorPage(request, response, "Missing album title");
            return null;
        }

        

        // Checks if the inserted strings (PASSWORD and REPEAT_PWD) are of the correct length (1-45) and equal
        if (titolo.length() <= 0 || titolo.length() > 49) {

            request.setAttribute("warning", "Chosen title invalid (a valid title has more than one character and less than 50)!");
            forward(request, response, "/WEB-INF/home.html");
            return null;
        }
        
       

       
        // CHECKS that the submitted user for the registration has not the SAME EMAIL
        // of another user in the DB.
        // If another user with the same email is present redirects to the to the RegisterPage
        // with a warning and error message
        AlbumDAO albumDAO = new AlbumDAO(connection);
        Album album = null;
/*
        try {

            album = albumDAO.findUserByEmail(email);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(album != null) {

            request.setAttribute("warning", "Chosen email already in use!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        // CHECKS that the submitted user for the registration has not the SAME USERNAME
        // of another user in the DB.
        // If another user with the same username is present redirects to the to the RegisterPage
        // with a warning and error message
        try {

            album = albumDAO.findUserByUsername(username);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(album != null) {

            request.setAttribute("warning", "Chosen username already in use!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }
*/
        // If all the parameters are correct creates the user in the DB
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