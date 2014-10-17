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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;

/**
 * Servlet Filter implementation class BlockerFilter
 */
public class BlockerFilter implements Filter {

	 private static final Logger logger = Logger.getLogger(BlockerFilter.class.getName());
	/**
	 * Default constructor.
	 */
	public BlockerFilter() {
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
		boolean block = false;
    	final String ip = request.getRemoteAddr();
    	
        if (request instanceof HttpServletRequest) {
        	HttpServletRequest httpRequest = (HttpServletRequest) request;
            
        	String username = StringUtil.getUsername(request.getAttribute("username"), httpRequest.getHeader("username"));
            String appId = httpRequest.getHeader(Commons.APP_HEADER);
            int appIdVal = -1;
            if (StringUtils.isNumeric(appId)) {
                try {
                   appIdVal = Integer.parseInt(appId);
                } catch (Exception e) {
                   appIdVal = -1;
                }
            }

            //blocked user agents
            String userAgent = httpRequest.getHeader("User-Agent");    
            //Browser browser = Browser.parseUserAgentString(userAgent);
        
            if (appIdVal == -1) {
                String blockedAgents = com.jstakun.lm.server.config.ConfigurationManager.getParam("blockedAgents", "");
                String[] blockedAgentsList = StringUtils.split(blockedAgents, "|");
            
                if (blockedAgentsList != null && blockedAgentsList.length > 0) {
                	for (int i=0;i<blockedAgentsList.length;i++) {
                		//System.out.println("Checking if " + userAgent + "=" + blockedAgentsList[i]);
                		if (StringUtils.containsIgnoreCase(userAgent,blockedAgentsList[i])) {
                			logger.log(Level.SEVERE, "Remote Addr: " + ip + ", User agent: " + userAgent + ", username: " + username + ", blocked AppId = -1");
                			block = true;
                			break;
                		}
                	}
                }
            }
            
            //blocked urls
            if (!block) {
            	logger.log(Level.WARNING, "User agent: " + userAgent + ", appId: " + appIdVal);    
            	String closed = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.CLOSED_URLS, "");
            	String[] closedUrlsList = StringUtils.split(closed, ",");
                if (closedUrlsList != null && closedUrlsList.length > 0) {
                	String uri = httpRequest.getRequestURI();
                	for (int i=0;i<closedUrlsList.length;i++) {
                		if (StringUtils.equals(uri, closedUrlsList[i])) {
                			logger.log(Level.SEVERE, "Remote Addr: " + ip + ", User agent: " + userAgent + ", username: " + username + ", AppId = " + appIdVal);
                        	block = true;
                        	break;
                		}
                	}
                }
            }          
        } 

        if (block) {
        	if (response instanceof HttpServletResponse) {
        		((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("text/html");
			    PrintWriter out = response.getWriter();
			    out.println("<html><head><title>403 Request rate too high</title></head><body>");
			    out.println("<h3>Request rate too high.</h3>");
			    out.println("</body></html>");
			    out.close();
        		//((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Request rate too high");
            } else {
            	response.getWriter().println("Request rate too high");
            }
        	return;
        } else {
        	chain.doFilter(request, response); 
        }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
