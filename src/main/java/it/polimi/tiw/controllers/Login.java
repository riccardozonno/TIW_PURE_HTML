package it.polimi.tiw.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.*;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String email = request.getParameter("email");
		String password = request.getParameter("password");
		if (email == null || password == null || email.isEmpty() || password.isEmpty()) {

			forwardToErrorPage(request, response, "Null email or password");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			user = userDAO.getUserLog(email, password);
		} catch (SQLException e) {

			request.setAttribute("warning", "Email or password incorrect!");
			forward(request, response, "/WEB-INF/login.html");
			return;
		}
		if (user == null) {

			request.setAttribute("warning", "Email or password incorrect!");
			forward(request, response, "/WEB-INF/login.html");
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("currentUser", user);
		session.setAttribute("currentUserId", user.getId_user());
		response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");
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
}