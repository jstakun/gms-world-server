package com.jstakun.lm.server.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet Filter implementation class GeocodeFilter
 */
public class GeocodeFilter implements Filter {

    /**
     * Default constructor. 
     */
    public GeocodeFilter() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		 if (request instanceof HttpServletRequest) {
	            //HttpServletRequest httpRequest = (HttpServletRequest) request;
	            //HttpServletResponse httpResponse = (HttpServletResponse) response;
	            
	            String address = request.getParameter("address");
	            String query = request.getParameter("query");
	            if (query != null) {
	            	query = URLDecoder.decode(query, "utf-8");
	            }
	            
	            if ((StringUtils.isNotEmpty(address) && StringUtils.containsIgnoreCase(address, "test input data")) ||
	            	(StringUtils.isNotEmpty(query) && StringUtils.containsIgnoreCase(query, "test"))) {
	            	PrintWriter out = response.getWriter();
				    out.println("<html><head><title>400 Bad Request</title></head><body>");
				    out.println("<h3>Bad Request.</h3>");
				    out.println("</body></html>");
				    out.close();
	            } else {
	            	chain.doFilter(request, response);
	            }
		 } else {
	        	PrintWriter out = response.getWriter();
			    out.println("<html><head><title>400 Bad Request</title></head><body>");
			    out.println("<h3>Bad Request.</h3>");
			    out.println("</body></html>");
			    out.close();
	     }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
