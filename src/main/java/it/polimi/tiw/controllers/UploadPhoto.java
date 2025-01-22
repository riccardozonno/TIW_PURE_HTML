package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

@WebServlet("/UploadPhoto")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1,  // 1KB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class UploadPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;

    
    public UploadPhoto() {
		super();
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
        	response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }
        
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        
        String inAlbumStr = request.getParameter("inAlbum");
        System.out.println(inAlbumStr);
        boolean inAlbum = Boolean.parseBoolean(inAlbumStr);

        int userId = ((User) session.getAttribute("currentUser")).getId_user();

        String uploadFilePath = "/Users/riccardozonno/res_html";

        File uploadFolder = new File(uploadFilePath);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        Random random = new Random();
        int n = random.nextInt(1000000);
        String rand = Integer.toString(n);
        AlbumDAO albumDAO = new AlbumDAO(connection);
        ImmagineDAO immagineDAO = new ImmagineDAO(connection);

        try {
            Part filePart = request.getPart("file");
            String fileName = rand + Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String filePath = uploadFolder + File.separator + fileName;
            System.out.println("Percorso di upload: " + filePath);
            System.out.println("ciao");

            filePart.write(filePath);
            File uploadedFile = new File(filePath);
            if (uploadedFile.exists()) {
                System.out.println("File scritto correttamente: " + filePath);
            } else {
                System.err.println("Errore: il file non è stato scritto.");
            }
            
            String dbPath =  "/Users/riccardozonno/res_html" + "/" + fileName;

            int imageId = immagineDAO.insertImageData(title, description, dbPath, userId);

            if(!inAlbum) {
            	response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");
            	return;
            }
            int albumId = (int)(request.getSession().getAttribute("albumId"));
            immagineDAO.linkImageToAlbum(imageId, albumId, userId);

            albumDAO.updateAlbumImageCount(connection, albumId);

            response.sendRedirect("GoToAlbumPage?albumId=" + albumId);

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
	
    @Override
    public void destroy() {
    	
		try {
			
			ConnectionHandler.closeConnection(connection);
		
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
	}

}
