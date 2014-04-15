package com.jstakun.lm.server.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 * Servlet Filter implementation class IPFilter
 */
public class IPFilter implements Filter {

	private static final Logger logger = Logger.getLogger(IPFilter.class.getName());
    /**
     * Default constructor. 
     */
    public IPFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final String ip = request.getRemoteAddr();
		final String ip_key = getClass().getName() + "_" + ip;

		Integer total_count = (Integer)CacheUtil.getObject(ip_key);

		if (total_count == null) {
				total_count = 1;
				CacheUtil.put(ip_key, 0);
		} else {
				total_count += 1;
		}
		
		CacheUtil.increment(ip_key);
		
		logger.log(Level.INFO, "Added address to cache " + ip_key + ": " + total_count);
		
		if (total_count > NumberUtils.getInt(ConfigurationManager.getParam(ConfigurationManager.IP_TOTAL_LIMIT, "90"), 90)) {
				logger.log(Level.SEVERE, "IP: " + ip + " is blocked after " + total_count + " requests");
				if (response instanceof HttpServletResponse) {
					logger.log(Level.INFO, "User-Agent: " + ((HttpServletRequest) request).getHeader("User-Agent"));
					((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
					PrintWriter out = response.getWriter();
				    out.println("<html><head><title>403 Request rate too high</title></head><body>");
				    out.println("<h3>Request rate too high.</h3>");
				    out.println("</body></html>");
				    out.close();
					//((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Request rate too high");
				} else {
					response.getWriter().println("Request rate too high");
				}
		} else if (request instanceof HttpServletRequest) {
			    final HttpServletRequest httpRequest = (HttpServletRequest) request;
				final String uri = httpRequest.getRequestURI();
				final String uri_key = getClass().getName() + "_" + ip + "_" + uri;
            			
				Integer uri_count = (Integer)CacheUtil.getObject(uri_key);

				if (uri_count == null) {
					uri_count = 1;
					CacheUtil.put(uri_key, 0);
				} else {
					uri_count += 1;
				}
			
				CacheUtil.increment(uri_key);
			
				logger.log(Level.INFO, "Added uri to cache " + uri_key + ": " + uri_count);
            
				if (uri_count > NumberUtils.getInt(ConfigurationManager.getParam(ConfigurationManager.IP_URI_LIMIT, "3"), 3)) {
					logger.log(Level.INFO, "User-Agent: " + httpRequest.getHeader("User-Agent"));
					logger.log(Level.SEVERE, "IP: " + ip + " is blocked after " + uri_count + " uri requests");
					((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.setContentType("text/html");
				    PrintWriter out = response.getWriter();
				    out.println("<html><head><title>403 Request rate too high</title></head><body>");
				    out.println("<h3>Request rate too high.</h3>");
				    out.println("</body></html>");
				    out.close();
					//((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN, "Request rate too high");				     
				} else {
					chain.doFilter(request, response);
				}
		} else {	
				chain.doFilter(request, response);
		}	
	}
	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
