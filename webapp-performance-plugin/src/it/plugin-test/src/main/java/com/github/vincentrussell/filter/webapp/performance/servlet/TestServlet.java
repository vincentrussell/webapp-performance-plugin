package com.github.vincentrussell.filter.webapp.performance.servlet;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class TestServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        // Set response content type
        response.setContentType("text/plain");

        try(InputStream inputStream = TestServlet.class.getResourceAsStream("/billOfRights.txt")) {
            IOUtils.copy(inputStream,response.getWriter());
        }
    }

}