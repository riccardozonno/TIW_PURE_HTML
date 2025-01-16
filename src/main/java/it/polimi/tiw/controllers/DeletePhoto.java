package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Album;
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
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
		String albumIdStr =  request.getParameter("albumId");
		String imageIdStr =  request.getParameter("imageId");
		int id_image;
		int id_album;
		try {
            id_album = Integer.parseInt(albumIdStr); // Converti l'ID in un intero
        } catch (NumberFormatException e) {
        	 id_album = (int)request.getAttribute("albumId");
            
        }
		try {
            id_image = Integer.parseInt(imageIdStr); // Converti l'ID in un intero
        } catch (NumberFormatException e) {
        	id_image = (int)request.getAttribute("imageId");
            
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
            // Recupera il percorso del file dal database
            String filePath = immagineDAO.getImagePathById(id_immagine);
            
            commentoDAO.deleteAllCommentsByImageId(id_immagine);
            // Aggiorna il totale delle immagini nell'album
            albumDAO.decrementAlbumImageCount(id_immagine);
            
            //Aggiorna tabella image_album
            immagineDAO.delinkImageToAlbum(id_immagine);
            
            // Elimina l'immagine dal database
            immagineDAO.deleteImageById(id_immagine);

            // Elimina fisicamente il file dal server
            deletePhysicalFile(getServletContext(), filePath);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'eliminazione dell'immagine.");
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	   private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{

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
	        // Percorso assoluto del file
	        String absolutePath = context.getRealPath("") + File.separator + filePath;
	        File file = new File(absolutePath);

	        if (file.exists()) {
	            if (file.delete()) {
	                System.out.println("File eliminato: " + absolutePath);
	            } else {
	                System.out.println("Errore durante l'eliminazione del file: " + absolutePath);
	            }
	        } else {
	            System.out.println("Il file non esiste: " + absolutePath);
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
