package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;

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

                FacebookClient facebookClient = FacebookUtils.getFacebookClient(Commons.getProperty(Property.fb_app_token));

                String query = request.getParameter("q");
                JsonObject placesSearch = null;

                if (query != null && query.length() > 0) {
                    placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", distance), Parameter.with("q", query), Parameter.with("limit", limit));
                } else {
                    placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", distance), Parameter.with("limit", limit));
                }

                JsonArray data = placesSearch.get("data").asArray();

                ArrayList<Object> jsonArray = new ArrayList<Object>();
                String output = "";

                if (request.getParameter("version") != null && request.getParameter("version").equals("3")) {

                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        jsonObject.put("name", place.get("name").asString());
                        jsonObject.put("url", place.get("id").asString());

                        Map<String, String> desc = new HashMap<String, String>();
                        if (place.names().contains("category")) {
                            desc.put("category", place.get("category").asString());
                        }
                        JsonObject location = place.get("location").asObject();
                        for (String name : location.names()) {
                            if (!(name.equals("latitude") || name.equals("longitude"))) {
                                desc.put(name, location.get(name).asString());
                            }
                        }
                        jsonObject.put("desc", desc);
                        jsonObject.put("lat", MathUtils.normalizeE6(location.get("latitude").asDouble()));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.get("longitude").asDouble()));
                        jsonArray.add(jsonObject);
                    }

                    JSONObject json = new JSONObject().put("ResultSet", jsonArray);
                    output = json.toString();

                } else if (request.getParameter("version") != null && request.getParameter("version").equals("2")) {

                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        jsonObject.put("name", place.get("name").asString());
                        jsonObject.put("desc", place.get("id").asString());
                        JsonObject location = place.get("location").asObject();
                        jsonObject.put("lat", MathUtils.normalizeE6(location.get("latitude").asDouble()));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.get("longitude").asDouble()));
                        jsonArray.add(jsonObject);
                    }

                    JSONObject json = new JSONObject().put("ResultSet", jsonArray);
                    output = json.toString();

                } else {
                    //data               
                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        JsonObject place = (JsonObject) data.get(i);
                        if (place.names().contains("name")) {
                            jsonObject.put("name", place.get("name").asString());
                        } else {
                            jsonObject.put("name", place.get("id").asString());
                        }
                        jsonObject.put("id", place.get("id").asString());
                        JsonObject location = place.get("location").asObject();
                        jsonObject.put("lat", MathUtils.normalizeE6(location.get("latitude").asDouble()));
                        jsonObject.put("lng", MathUtils.normalizeE6(location.get("longitude").asDouble()));
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
