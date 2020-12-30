package net.gmsworld.server.layers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.OtpUtils;

/**
 * Servlet implementation class OtpServlet
 */
public final class OtpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OtpServlet() {
        super();
    }

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//generate
		final String uri = request.getRequestURI();
		if (uri.startsWith("/admin")) {
			final String key = request.getParameter("key");
			if (StringUtils.isNotEmpty(key)) {
				int count = OtpUtils.DEFAULT_TOKEN_LENGTH;
				final String countStr = request.getParameter("count");
				if (StringUtils.isNumeric(countStr)) {
					count = Integer.parseInt(countStr);
				}
				final String token = OtpUtils.generateOtpToken(key, count);
				response.getWriter().append(token);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().append("Invalid or missing parameters");
			}
		} else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//validate and delete 
		final String key = request.getParameter("key");
		final String value = request.getParameter("value");
		if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
			if (StringUtils.equals(OtpUtils.getToken(key), value)) {
				response.getWriter().append("ok");
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Invalid or missing parameters");
		}
	}

}
