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


import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class DownloadPhoto
 */
@WebServlet("/DownloadPhoto")
public class DownloadPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    
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
    }
    
    public void destroy() {

        try {

            ConnectionHandler.closeConnection(connection);

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}
