package com.jstakun.lm.server.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

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

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String authHeader = httpRequest.getHeader("Authorization");
            boolean auth = false;
            boolean noservice = false;
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
            			auth = UserPersistenceUtils.login(usr, password);
            		}
            	} else if (StringUtils.startsWith(authHeader, "Bearer")) {
            		token = getToken(authHeader);
            	}
            }
            
            //>= 1101, 101
            if (!auth) {
            	if (token != null) {
            		authHeader = token;
            	} else {
            		authHeader = httpRequest.getHeader(Commons.TOKEN_HEADER);
            	}
            	String scope = httpRequest.getHeader(Commons.SCOPE_HEADER);
            	if (authHeader != null && scope != null) {
            		try {
            			String tokenUrl = net.gmsworld.server.config.ConfigurationManager.RHCLOUD_SERVER_URL + "isValidToken?scope=" + scope + "&key=" + authHeader;
            			String tokenJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(tokenUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
        				if (StringUtils.startsWith(tokenJson, "{")) {
        					JSONObject root = new JSONObject(tokenJson);
        					auth = root.getBoolean("output");
        				} else if (tokenJson == null || StringUtils.contains(tokenJson, "503 Service Temporarily Unavailable")) {
        				    noservice = true;
        				} else {
        					logger.log(Level.SEVERE, "Received following server response {0}", tokenJson);
        				}
            		} catch (JSONException e) {
                		logger.log(Level.SEVERE, e.getMessage(), e);
                	}
            	} //else if (StringUtils.contains(httpRequest.getRequestURI(), "crashReport")) {
            		//auth = true; 
            	//}
            }        

            if (auth) {
                chain.doFilter(request, response);
            } else if (noservice) {
            	logger.log(Level.SEVERE, "Service Unavailable!");
                httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("text/html");
			    PrintWriter out = response.getWriter();
			    out.println("<html><head><title>503 Service Unavailable</title></head><body>");
			    out.println("<h3>Service Unavailable.</h3>");
			    out.println("</body></html>");
			    out.close();
            } else {
            	logger.log(Level.SEVERE, "Authorization failed!");
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
    @Override
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("ServicesAuthorizationFilter:Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
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
