package it.polimi.tiw.controllers;

import java.io.File;
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
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.CommentoDAO;
import it.polimi.tiw.dao.ImmagineDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TemplateHandler;

/**
 * Servlet implementation class DeletePhoto
 */
@WebServlet("/DeletePhoto")
public class DeletePhoto extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeletePhoto() {
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
       
		String albumIdStr =  request.getParameter("albumId");
		String imageIdStr =  request.getParameter("imageId");
		 if (albumIdStr == null || albumIdStr.isEmpty()) {
	        	forwardToErrorPage(request,response,"id album non valido");
	            return;
	        }
	     if (imageIdStr == null || imageIdStr.isEmpty()) {
	       	forwardToErrorPage(request,response,"id immagine non valido");
	        return;
	        }
		int id_image;
		int id_album;
		try {
            id_album = Integer.parseInt(albumIdStr);
        } catch (NumberFormatException e) {
        	 id_album = (int)request.getAttribute("albumId");
            
        }
		try {
            id_image = Integer.parseInt(imageIdStr); 
        } catch (NumberFormatException e) {
        	id_image = (int)request.getAttribute("imageId");
            
        }
		ImmagineDAO imageDAO = new ImmagineDAO(connection);
		
		try {
			if((int)((User)session.getAttribute("currentUser")).getId_user() != imageDAO.getImageById(id_image).getId_user_proprietario() )
			forwardToErrorPage(request, response,"cancellazione non autorizzata");
			return;
		} catch (SQLException | ServletException | IOException e) {
			forwardToErrorPage(request, response,"immagine non trovata");
		}
		
		deleteImage(request, response, id_image);
    	request.setAttribute("albumId",id_album );
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/GoToAlbumPage");
	    dispatcher.forward(request, response);
    }
	
	private void deleteImage(HttpServletRequest request, HttpServletResponse response, int id_immagine) throws ServletException, IOException {
        CommentoDAO commentoDAO = new CommentoDAO(connection);
		ImmagineDAO immagineDAO = new ImmagineDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection);

        try {
            
            String filePath = immagineDAO.getImagePathById(id_immagine);
            
            commentoDAO.deleteAllCommentsByImageId(id_immagine);
            
            albumDAO.decrementAlbumImageCount(id_immagine);
            
            
            immagineDAO.delinkImageToAlbum(id_immagine);
            
            
            immagineDAO.deleteImageById(id_immagine);

            
            deletePhysicalFile(getServletContext(), filePath);

        } catch (SQLException e) {
        	forwardToErrorPage(request, response, e.getMessage());
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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
	    
	    @Override
	    public void init() throws ServletException {

	        ServletContext servletContext = getServletContext();
	        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
	        this.connection = ConnectionHandler.getConnection(servletContext);
	    }

	    
	    private void deletePhysicalFile(ServletContext context, String filePath) {	        
	        File file = new File(filePath);

	        if (file.exists()) {
	            file.delete();
	        }
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
