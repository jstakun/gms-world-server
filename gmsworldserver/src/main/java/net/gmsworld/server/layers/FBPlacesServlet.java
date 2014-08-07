/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import org.json.JSONObject;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstakun
 */
public class FBPlacesServlet extends HttpServlet {
     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            //THIS is for NOKIA application
        	Logger.getLogger(FBPlacesServlet.class.getName()).log(Level.SEVERE, "Oops !!! Somebody called " + FBPlacesServlet.class.getName());

            if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "distance")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String latitude = request.getParameter("latitude");
                String longitude = request.getParameter("longitude");
                String distance = request.getParameter("distance");
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);

                FacebookClient facebookClient = new DefaultFacebookClient(Commons.getProperty(Property.fb_app_token));

                String query = request.getParameter("q");
                JsonObject placesSearch = null;

                if (query != null && query.length() > 0) {
                    placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", distance), Parameter.with("q", query), Parameter.with("limit", limit));
                } else {
                    placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", distance), Parameter.with("limit", limit));
                }

                JsonArray data = placesSearch.getJsonArray("data");

                ArrayList<Object> jsonArray = new ArrayList<Object>();
                String output = "";

                if (request.getParameter("version") != null && request.getParameter("version").equals("3")) {

                    for (int i = 0; i < data.length(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        jsonObject.put("name", place.getString("name"));
                        jsonObject.put("url", place.getString("id"));

                        Map<String, String> desc = new HashMap<String, String>();
                        if (place.has("category")) {
                            desc.put("category", place.getString("category"));
                        }
                        JsonObject location = place.getJsonObject("location");
                        Iterator<?> iter = location.sortedKeys();
                        while (iter.hasNext()) {
                            String next = (String)iter.next();
                            if (!(next.equals("latitude") || next.equals("longitude"))) {
                                desc.put(next, location.getString(next));
                            }
                        }
                        jsonObject.put("desc", desc);
                        jsonObject.put("lat", MathUtils.normalizeE6(location.getDouble("latitude")));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.getDouble("longitude")));
                        jsonArray.add(jsonObject);
                    }

                    JSONObject json = new JSONObject().put("ResultSet", jsonArray);
                    output = json.toString();

                } else if (request.getParameter("version") != null && request.getParameter("version").equals("2")) {

                    for (int i = 0; i < data.length(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        jsonObject.put("name", place.getString("name"));
                        jsonObject.put("desc", place.getString("id"));
                        JsonObject location = place.getJsonObject("location");
                        jsonObject.put("lat", MathUtils.normalizeE6(location.getDouble("latitude")));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.getDouble("longitude")));
                        jsonArray.add(jsonObject);
                    }

                    JSONObject json = new JSONObject().put("ResultSet", jsonArray);
                    output = json.toString();

                } else {
                    //data               
                    for (int i = 0; i < data.length(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        if (place.has("name")) {
                            jsonObject.put("name", place.getString("name"));
                        } else {
                            jsonObject.put("name", place.getString("id"));
                        }
                        jsonObject.put("id", place.getString("id"));
                        JsonObject location = place.getJsonObject("location");
                        jsonObject.put("lat", MathUtils.normalizeE6(location.getDouble("latitude")));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.getDouble("longitude")));
                        jsonArray.add(jsonObject);
                    }

                    JSONObject json = new JSONObject().put("data", jsonArray);
                    output = json.toString();
                }

                out.println(output);

            }
        } catch (Exception ex) {
            Logger.getLogger(FBPlacesServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            if (ex instanceof FacebookOAuthException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "FB Places servlet";
    }// </editor-fold>
}
