package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;


/**
 * Servlet implementation class DownloadPhoto
 */
@WebServlet("/DownloadPhoto")
public class DownloadPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    public DownloadPhoto() {
		super();
	}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession(false);

        // Verifica se l'utente è loggato
        if (session == null || session.getAttribute("currentUser") == null) {
        	response.sendRedirect(request.getServletContext().getContextPath() + "/GoToLoginPage");
            return;
        }
        try {
            String imageId = request.getParameter("imageId");
            if (imageId == null || imageId.isEmpty()) {
    	       	forwardToErrorPage(request,response,"id immagine non valido");
    	        return;
    	        }
            ImmagineDAO immagineDAO = new ImmagineDAO(connection);
   
            String imagePath = immagineDAO.getImagePathById(Integer.parseInt(imageId));
            File imageFile = new File(imagePath);
            String mimeType = getServletContext().getMimeType(imageFile.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";  
            }
            response.setContentType(mimeType);
            response.setContentLengthLong(imageFile.length());

            try{
            byte[] imageData = Files.readAllBytes( new File ((imagePath)).toPath());
            
            OutputStream out = response.getOutputStream();
            out.write(imageData);
            out.flush();
            out.close();
            }catch (Exception e) {
            	;
            }
        } catch (NumberFormatException | SQLException e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();
        this.connection = ConnectionHandler.getConnection(servletContext);
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }
    
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
