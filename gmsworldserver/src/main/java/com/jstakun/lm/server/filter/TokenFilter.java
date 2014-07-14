package com.jstakun.lm.server.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 * Servlet Filter implementation class TokenFilter
 */
public class TokenFilter implements Filter {

	private static final Logger logger = Logger.getLogger(TokenFilter.class.getName());
    /**
     * Default constructor. 
     */
    public TokenFilter() {
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
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			String authHeader = httpRequest.getHeader(Commons.TOKEN_HEADER);
			if (authHeader != null) {
				Integer token_count = (Integer)CacheUtil.getObject(authHeader);
				
				if (token_count == null) {
					token_count = 1;
					CacheUtil.put(authHeader, 0);
				} else {
					token_count += 1;
				}
			
				CacheUtil.increment(authHeader);
			
				logger.log(Level.INFO, "Added token to cache " + authHeader + ": " + token_count);
				
				if (token_count > 100) {
					logger.log(Level.WARNING, "User with token {0} sent {1} requests.", new Object[]{authHeader, token_count});
				}
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
