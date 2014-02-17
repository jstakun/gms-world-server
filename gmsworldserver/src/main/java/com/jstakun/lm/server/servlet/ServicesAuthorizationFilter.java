/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import java.io.IOException;
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
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.BCTools;
import com.jstakun.lm.server.utils.CryptoTools;
import com.jstakun.lm.server.utils.Sha1;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class ServicesAuthorizationFilter implements Filter {

    private static final boolean debug = true;
    private static final String BASIC_REALM = "Basic realm=\"Login Users\"";
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
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String authHeader = httpRequest.getHeader("Authorization");

            byte[] username = null;
            byte[] password = null;

            boolean auth = false;

            if (authHeader != null) {
                byte[][] unPw = userPass(authHeader);
                if (unPw != null) {
                    username = unPw[0];
                    password = unPw[1];
                }

                if (username != null) {
                    String usr = new String(username);
                    String pwdStr = null; 
                    if (password != null) {
                    	pwdStr = new String(password);
                    }
                    request.setAttribute("username", usr);
                    //logger.log(Level.INFO, "User {0} requested for authn", usr);

                    if (StringUtils.equals(usr, Commons.DEFAULT_USERNAME)) {      
                    	try {                  		
                    		pwdStr = Base64.encode(password);
                    		if (StringUtils.equals(pwdStr, Commons.DEFAULT_PASSWORD)) {
                    			//logger.log(Level.INFO, "User default authn succeded!");
                    			auth = true;
                    		} else {
                    			logger.log(Level.SEVERE, "User default authn failed!");
                    		}
                		} catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                		//logger.log(Level.SEVERE, "User {0} authn success with {1}", new String[] {usr, pwdStr});
                	} 
                    
                    User user = UserPersistenceUtils.selectUserByLogin(usr);
                    if (user != null && password != null) {      	
                    	if (user.getPassword().equals(pwdStr)) {
                    		//logger.log(Level.INFO, "User {0} authn success with plain password", usr);
                    		try {
                    				request.setAttribute("password", Base64.encode(BCTools.encrypt(password)));	
                    				request.setAttribute("name", StringUtil.getFormattedUsername(user.getFirstname(), user.getLastname(), user.getLogin()));
                    				request.setAttribute("email", user.getEmail());
                    		} catch (Exception e) {
                    				logger.log(Level.SEVERE, e.getMessage(), e);
                    		}
                    		auth = true;
                    	} else if (password.length % 8 == 0) {
                        	try {
                                   byte[] pwd = CryptoTools.decrypt(password);
                                   if (new String(pwd).equals(user.getPassword())) {
                                      //logger.log(Level.INFO, "User {0} authn success with encrypted password", usr);
                                      auth = true;
                                   }
                             } catch (Exception e) {
                                   logger.log(Level.SEVERE, e.getMessage(), e);
                             }
                        } else if (Sha1.encode(user.getPassword()).equals(pwdStr)) {
                    		 //logger.log(Level.INFO, "User {0} authn success with SHA", usr);
                    	     auth = true;
                    	} 
                    	
                        if (auth) {
                           UserPersistenceUtils.setLastLogonDate(user);
                        } //else {
                            //logger.log(Level.INFO, "User {0} failed to authn!", usr);
                        //}

                        //String token = httpRequest.getHeader("OAuthtoken");
                        //String secret = httpRequest.getHeader("OAuthsecret");
                        //try {
                        //    if (secret != null) {
                        //        secret = new String(Base64.decode(secret.getBytes()));
                        //    }
                        //} catch (Exception ex) {
                        //    logger.log(Level.SEVERE, null, ex);
                        //}

                        //if (auth && token != null && secret != null)
                        //{
                            //logger.log(Level.INFO, "User {0} provided oauth token", usr);
                            //httpRequest.getSession().setAttribute("token", token);
                            //httpRequest.getSession().setAttribute("password",  secret);
                        //}
                    }
                    else 
                    {
                    	logger.log(Level.SEVERE, "Need to check if user {0} has registered with token !!!", usr);
                    	/*String svc = request.getParameter("service");
                        if (OAuthTokenPersistenceUtils.countOAuthTokenByUser(usr, pwdStr) > 0)
                        {
                            httpRequest.setAttribute("username", usr);
                            auth = true;
                        } else if (svc != null) {
                            OAuthToken token = OAuthTokenPersistenceUtils.selectOAuthTokenByService(usr, pwdStr, svc);
                            if (token != null)
                            {
                                httpRequest.setAttribute("username", token.getUserId() + "@" + svc);
                                auth = true;
                            }
                        }

                        httpRequest.getSession().setAttribute("token", usr);
                        httpRequest.getSession().setAttribute("password", pwdStr);*/
                    }
                }
            }

            if (auth) {
                chain.doFilter(request, response);
            } else {
            	String user = null;
            	if (username != null) {
            		user = new String(username);
            	}
            	logger.log(Level.SEVERE, "User {0} authn failed!", user);
                httpResponse.setHeader("WWW-Authenticate", BASIC_REALM);
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
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
