package com.jstakun.lm.server.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.StringTokenizer;
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

import org.apache.commons.lang.StringUtils;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.utils.persistence.TokenPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import net.gmsworld.server.config.Commons;

/**
 *
 * @author jstakun
 */
public class ServicesAuthorizationFilter implements Filter {

    private static final boolean debug = true;
    private static final Logger logger = Logger.getLogger(ServicesAuthorizationFilter.class.getName());
    private FilterConfig filterConfig = null;

    public ServicesAuthorizationFilter() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String authHeader = httpRequest.getHeader("Authorization");
            int authStatus = -10; //1 - ok, 0 - failed, -1 service unavailable, -10 - none
            String token = null;
            
            if (StringUtils.isNotEmpty(authHeader)) {
            	if (StringUtils.startsWith(authHeader, "Basic")) {
            		byte[] username = null;
            		byte[] password = null;

            		byte[][] unPw = userPass(authHeader);
            		if (unPw != null) {
            			username = unPw[0];
            			password = unPw[1];
            		}

            		if (username != null) {
            			String usr = new String(username);
            			request.setAttribute("username", usr);                   
            			if (UserPersistenceUtils.login(usr, password)) {
            				authStatus = 1;
            			} else {
            				authStatus = 0;
            			}
            		}
            	} else if (StringUtils.startsWith(authHeader, "Bearer")) {
            		token = getToken(authHeader);
            	}
            } else {
            	logger.log(Level.INFO, "No Authorization header.");
            }
            
            //>= 1101, 101
            if (authStatus == -10) {
            	if (token != null) {
            		authHeader = token;
            	} else {
            		authHeader = httpRequest.getHeader(Commons.TOKEN_HEADER);
            	}
            	final String scope = httpRequest.getHeader(Commons.SCOPE_HEADER);
            	if (StringUtils.isNotEmpty(authHeader) && StringUtils.isNotEmpty(scope)) {
            		try {
            			authStatus = TokenPersistenceUtils.isTokenValid(authHeader, scope);
            		} catch (Exception e) {
                		logger.log(Level.SEVERE, e.getMessage(), e);
                		authStatus = -1;
                	}
            	} else if (StringUtils.contains(httpRequest.getRequestURI(), "crashReport") ||
            			StringUtils.contains(httpRequest.getRequestURI(), "fbauth") ||
            			StringUtils.contains(httpRequest.getRequestURI(), "twauth") ||
            			StringUtils.contains(httpRequest.getRequestURI(), "glauth") ||
            			StringUtils.contains(httpRequest.getRequestURI(), "fsauth") ||
            			StringUtils.contains(httpRequest.getRequestURI(), "lnauth"))  {  //TODO fix
            		logger.log(Level.INFO, authHeader   + " " + scope);
            		authStatus = 1; 
            	} else {
            		logger.log(Level.INFO, "No Token and Scope headers");
            	}
            }        

            if (authStatus == 1) {
                chain.doFilter(request, response);
            } else if (authStatus == -1) {
            	logger.log(Level.SEVERE, "Service Unavailable!");
                httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("text/html");
			    PrintWriter out = response.getWriter();
			    out.println("<html><head><title>503 Service Unavailable</title></head><body>");
			    out.println("<h3>Service Unavailable.</h3>");
			    out.println("</body></html>");
			    out.close();
            } else if (authStatus == 0 || authStatus == -10) {
            	if (authStatus == 0) {
            		logger.log(Level.SEVERE, "Authorization failed!");
            	} else if (authStatus == -10) {
            		logger.log(Level.WARNING, "Authorization failed!");
            	}
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/html");
			    PrintWriter out = response.getWriter();
			    out.println("<html><head><title>401 Unauthorized</title></head><body>");
			    out.println("<h3>Request Unauthorized.</h3>");
			    out.println("</body></html>");
			    out.close();
            }
        } else {
        	PrintWriter out = response.getWriter();
		    out.println("<html><head><title>401 Unauthorized</title></head><body>");
		    out.println("<h3>Request Unauthorized.</h3>");
		    out.println("</body></html>");
		    out.close();
        }
    }

    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("ServicesAuthorizationFilter:Initializing filter");
            }
        }
    }

    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("ServicesAuthorizationFilter()");
        }
        StringBuilder sb = new StringBuilder("ServicesAuthorizationFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private static String getToken(String authorization) {
    	String token = null;
    	StringTokenizer st = new StringTokenizer(authorization);
        if (st.hasMoreTokens()) {
        	//Bearer: <token>
   		 	 String bearer = st.nextToken();
        	 if (bearer.equalsIgnoreCase("Bearer") && st.hasMoreTokens()) {
                 token = st.nextToken();
        	 }
        }
        return token;
    }
    
    private static byte[][] userPass(String authorization) {
        StringTokenizer st = new StringTokenizer(authorization);
        if (st.hasMoreTokens()) {
            String basic = st.nextToken();

            // We only handle HTTP Basic authentication

            if (basic.equalsIgnoreCase("Basic") && st.hasMoreTokens()) {
                String credentials = st.nextToken();

                String userPass = "";
                byte[] authzBytes = null;

                try {
                    authzBytes = Base64.decode(credentials);
                    userPass = new String(authzBytes);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }

                // The decoded string is in the form
                // "userID:password".

                int p = userPass.indexOf(":");
                if (p != -1) {
                    byte[] userId = new byte[p];
                    byte[] password = new byte[userPass.length() - p];
                    userId = Arrays.copyOfRange(authzBytes, 0, p);
                    password = Arrays.copyOfRange(authzBytes, p + 1, authzBytes.length);
                    return new byte[][]{userId, password};
                }
            }
        }
        return null;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
}
