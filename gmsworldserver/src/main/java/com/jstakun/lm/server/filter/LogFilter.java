/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import com.jstakun.lm.server.utils.persistence.ServiceLogPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class LogFilter implements Filter {

    private static final boolean debug = true;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    
    
    public LogFilter() {
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	boolean block = false;
    	final String ip = request.getRemoteAddr();
    	
        if (request instanceof HttpServletRequest) {
        	HttpServletRequest httpRequest = (HttpServletRequest) request;
            
        	String username = StringUtil.getUsername(request.getAttribute("username"), httpRequest.getHeader("username"));
            int appIdVal = NumberUtils.getInt(httpRequest.getHeader(Commons.APP_HEADER), -1);
    		
            if (StringUtils.isNotEmpty(username)){
                ServiceLogPersistenceUtils.persist(username, httpRequest.getRequestURI(), true, appIdVal);
            } else {
                ServiceLogPersistenceUtils.persist(null, httpRequest.getRequestURI(), false, appIdVal);
            }
        } else {
            String url = request.getScheme() + "://" + request.getLocalName();
            ServiceLogPersistenceUtils.persist(null, url, false, -1);
        }

        if (block) {
        	if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, ip + " has too many requests");
            } else {
            	response.getWriter().println("Request rate too high");
            }
        	return;
        } else {
        	chain.doFilter(request, response); 
        }
    }
    

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("LogFilter:Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return "LogFilter()";
        } else {
            StringBuilder sb = new StringBuilder("LogFilter(");
            sb.append(filterConfig);
            sb.append(")");
            return (sb.toString());
        }
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
}
