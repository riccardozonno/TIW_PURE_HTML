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

import it.polimi.tiw.utils.*;
import it.polimi.tiw.dao.*;
import it.polimi.tiw.beans.*;


@WebServlet("/Register")
public class Register extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
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
        // TODO Auto-generated method stub
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (registerUser(request, response) == null) {
            return;
        }
        response.sendRedirect(getServletContext().getContextPath() + "/GoToLoginPage");
    }



    private Integer registerUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String repeat_pwd = request.getParameter("repeat_pwd");

        if( email == null || email.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty() || repeat_pwd == null || repeat_pwd.isEmpty()) {
            forwardToErrorPage(request, response, "Register module missing some data");
            return null;
        }

        String emailRegEx = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$";

        if (!email.matches(emailRegEx)) {

            request.setAttribute("warning", "Chosen email invalid!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        if (username.length() <= 0 || username.length() > 40) {

            request.setAttribute("warning", "Chosen username invalid (a valid username has more than one character and less than 45)!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        if (password.length() <= 0 || password.length() > 40) {
            request.setAttribute("warning", "Chosen password invalid (a valid password has more than one character and less than 45)!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        if (!password.equals(repeat_pwd)) {
            request.setAttribute("warning", "Password and repeat password field not equal!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user = null;

        try {

            user = userDAO.findUserByEmail(email);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user != null) {

            request.setAttribute("warning", "Chosen email already in use!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        try {

            user = userDAO.findUserByUsername(username);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user != null) {

            request.setAttribute("warning", "Chosen username already in use!");
            forward(request, response, "/WEB-INF/register.html");
            return null;
        }

        try {


            userDAO.createUser( username, email, password);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }
        try {

            user = userDAO.findUserByEmail(email);

        } catch (SQLException e) {

            forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user == null) {

            forwardToErrorPage(request, response, "Error: user non correctly created - registerPage!");
            return null;
        }

        return 0;
    }



    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{

        request.setAttribute("error", error);
        forward(request, response, "/WEB-INF/error.html");
        return;
    }


    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }


}
