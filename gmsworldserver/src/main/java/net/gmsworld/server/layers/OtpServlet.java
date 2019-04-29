package net.gmsworld.server.layers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

/**
 * Servlet implementation class OtpServlet
 */
public final class OtpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PREFIX = "otp:";
       
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
				int count = 8;
				final String countStr = request.getParameter("count");
				if (StringUtils.isNumeric(countStr)) {
					count = Integer.valueOf(countStr).intValue();
				}
				final String token = RandomStringUtils.random(count, false, true);
				CacheUtil.put(PREFIX + key, token, CacheType.FAST);
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
			final Object token = CacheUtil.remove(PREFIX + key);
			if (token != null) {
				if (StringUtils.equals(token.toString(), value)) {
					response.getWriter().append("ok");
				} else {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Invalid or missing parameters");
		}
	}

}
