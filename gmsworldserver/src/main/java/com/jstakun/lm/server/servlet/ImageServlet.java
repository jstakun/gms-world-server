package com.jstakun.lm.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.FileUtils;

/**
 * Servlet implementation class ImageServlet
 */
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	
	/**
	 * @see HttpServlet#process(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String key = (String) request.getParameter("key");
		Screenshot s = FileUtils.getScreenshot(key, true);
		String imageUrl = null;
		if (s != null) {
			//HttpUtils.processImageFileRequest(response, s.getUrl());
			response.sendRedirect(s.getUrl());
		} else {
			response.setContentType("image/png");
			imageUrl = "/images/location.png";
			request.getRequestDispatcher(imageUrl).include(request, response);
		}
	}

}
