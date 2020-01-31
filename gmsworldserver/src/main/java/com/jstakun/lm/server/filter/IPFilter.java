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
import com.jstakun.lm.server.utils.memcache.CacheUtil;

import net.gmsworld.server.utils.NumberUtils;

/**
 * Servlet Filter implementation class IPFilter
 */
public class IPFilter implements Filter {

	private static final Logger logger = Logger.getLogger(IPFilter.class.getName());

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		logger.log(Level.INFO, "Destroying filter " + getClass().getName());
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final String ip = request.getRemoteAddr();
		final String ip_key = getClass().getName() + "_" + ip;
		final Long total_count = CacheUtil.increment(ip_key);
		logger.log(Level.INFO, "Added address to cache " + ip_key + ": " + total_count);	
		final int ipLimit = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.IP_TOTAL_LIMIT, "90"), 90);
		if (total_count > ipLimit) {
				logger.log(Level.WARNING, "IP: " + ip + " is blocked after " + total_count + " requests against limit " + net.gmsworld.server.config.ConfigurationManager.IP_TOTAL_LIMIT + "=" + ipLimit);
				if (response instanceof HttpServletResponse) {
					logger.log(Level.INFO, "User-Agent: " + ((HttpServletRequest) request).getHeader("User-Agent"));
					((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
					PrintWriter out = response.getWriter();
				    out.println("<html><head><title>403 Request rate too high</title></head><body>");
				    out.println("<h3>Request rate too high.</h3>");
				    out.println("</body></html>");
				    out.close();
				} else {
					response.getWriter().println("Request rate too high");
				}
		} else if (request instanceof HttpServletRequest) {
			    final HttpServletRequest httpRequest = (HttpServletRequest) request;
				final String uri = httpRequest.getRequestURI();
				final String uri_key = getClass().getName() + "_" + ip + "_" + uri;
				Long uri_count = CacheUtil.increment(uri_key);
				logger.log(Level.INFO, "Added uri to cache " + uri_key + ": " + uri_count);
				final int uriLimit = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.IP_URI_LIMIT, "3"), 3);
				if (uri_count > uriLimit) {
					logger.log(Level.INFO, "User-Agent: " + httpRequest.getHeader("User-Agent"));
					logger.log(Level.WARNING, "IP: " + ip + " is blocked after " + uri_count + " uri requests against limit " + net.gmsworld.server.config.ConfigurationManager.IP_URI_LIMIT + "=" + uriLimit);
					((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.setContentType("text/html");
				    PrintWriter out = response.getWriter();
				    out.println("<html><head><title>403 Request rate too high</title></head><body>");
				    out.println("<h3>Request rate too high.</h3>");
				    out.println("</body></html>");
				    out.close();
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
		logger.log(Level.INFO, "Initializing filter " + getClass().getName());
	}

}
