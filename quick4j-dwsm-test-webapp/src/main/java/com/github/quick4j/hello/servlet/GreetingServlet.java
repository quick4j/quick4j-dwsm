package com.github.quick4j.hello.servlet;

import com.github.quick4j.hello.entity.Person;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhaojh.
 */
public class GreetingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        if(null != name){
            Person person = new Person(name);
            request.getSession().setAttribute("person", person);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/views/greeting.jsp");
        dispatcher.forward(request, response);
    }
}
