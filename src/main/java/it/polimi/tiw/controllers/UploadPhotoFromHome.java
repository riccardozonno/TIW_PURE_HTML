package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class UploadPhotoFromHome
 */
@WebServlet("/UploadPhotoFromHome")
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
	    maxFileSize = 1024 * 1024 * 10,      // 10MB
	    maxRequestSize = 1024 * 1024 * 50    // 50MB
	)
public class UploadPhotoFromHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;

    public UploadPhotoFromHome() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
        this.connection = ConnectionHandler.getConnection(servletContext);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("/login.html");
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        //int albumId = Integer.parseInt(request.getParameter("album_id"));
        int userId = (int) session.getAttribute("currentUserId");

     // Percorso corretto per salvare le immagini
        String uploadPath = getServletContext().getRealPath("/static/res");
        Random random = new Random();

     // Obtain a number between [0 - 49].
        int n = random.nextInt(1000000);
        String rand = Integer.toString(n);
        ImmagineDAO immagineDAO = new ImmagineDAO(connection);

        try {
        	
            Part filePart = request.getPart("file");
            String fileName = rand + Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String filePath = uploadPath + File.separator + fileName;
            System.out.println("Percorso di upload: " + uploadPath);

            filePart.write(filePath);
            System.out.println("ho scritto il file");

            String dbPath =  "static/res/" + fileName;

            immagineDAO.insertImageData(connection, title, description, dbPath, userId);

            response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");

        } catch (Exception e) {
            forwardToErrorPage(request, response, e.getMessage());
        }
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
		forward(request, response, "/error.html");
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
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
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
	