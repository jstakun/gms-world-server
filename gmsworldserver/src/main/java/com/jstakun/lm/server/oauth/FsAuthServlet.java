package com.jstakun.lm.server.oauth;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.FoursquareUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.TokenUtil;

/**
 * Servlet implementation class FsAuthServlet
 */
public class FsAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FsAuthServlet.class.getName());

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String code = request.getParameter("code");

			URL tokenUrl = new URL(FSCommons.getAccessTokenUrl(code));

			String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
			String accessToken = null;

			if (StringUtils.startsWith(result, "{")) {
				JSONObject resp = new JSONObject(result);
				accessToken = resp.getString("access_token");
			}

			if (accessToken != null) {

				Map<String, String> userData = FoursquareUtils.getUserData(accessToken);
				
				if (!userData.isEmpty()) {
					userData.put("token", accessToken);
					
					String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.FS_USERNAME) + "@" + Commons.FOURSQUARE);
                    userData.put("gmsToken", key); 

					Queue queue = QueueFactory.getQueue("notifications");
					queue.add(withUrl("/tasks/notificationTask").
                		param("service", Commons.FOURSQUARE).
                		param("accessToken", accessToken).
                		param("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                		param("username", userData.get(ConfigurationManager.FS_USERNAME)).
                		param("name", userData.containsKey(ConfigurationManager.FS_NAME) ? userData.get(ConfigurationManager.FS_NAME) : "noname"));             
				
					out.print(OAuthCommons.getOAuthSuccessHTML(new JSONObject(userData).toString()));
				} else {
					logger.log(Level.SEVERE, "User data is empty!");
					response.sendRedirect("/m/oauth_logon_error.jsp");
				}
			} else {
				logger.log(Level.SEVERE, "User access token is null!");
				response.sendRedirect("/m/oauth_logon_error.jsp");
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			response.sendRedirect("/m/oauth_logon_error.jsp");
		} finally {
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
