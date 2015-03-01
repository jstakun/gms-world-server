<%-- 
    Document   : landmark
    Created on : 2010-12-19, 09:34:12
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.Landmark,
        com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
        com.jstakun.lm.server.persistence.Comment,
        com.jstakun.lm.server.utils.UrlUtils,net.gmsworld.server.utils.DateUtils,java.util.List,com.google.appengine.api.datastore.KeyFactory,net.gmsworld.server.utils.StringUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.cw3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        <title>GMS World - Landmark Details</title>
    </head>
    <body>
        <jsp:include page="/WEB-INF/jspf/header.jsp"/>
        <div id="content-wrap" class="clear" >
            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <div class="post">
                        <%
                        Landmark landmark = (Landmark) request.getAttribute("landmark");            
                        String key = (String) request.getAttribute("key");
                        
                        if (landmark == null && key == null) {
                        %>
                        <h3>Landmark not found.</h3>
                        <div class="post">
                    	   <p>
                 			Landmark has been archived our you have not provided landmark key.
                    	   </p>
                        </div>  
                        <% } else if (landmark == null) {
                        %>
                        <h3>Landmark not found.</h3>
                        <div class="post">
                    	   <p>
                 			Landmark has been archived our you have provided wrong landmark key.
                    	   </p>
                        </div>  
                        <% } else {
                        %>
                        <h3><%= landmark.getName()%></h3>
                        <h4><%= landmark.getDescription()%></h4>
    
                        <!--div id="map_canvas" style="width:512px; height:256px"></div-->
                       
                        
                          <p class="image-section">
                              <a href="/showLandmark/<%= key %>/fullScreen">
                                  <img src="http://maps.google.com/maps/api/staticmap?center=<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>&zoom=12&size=640x256&sensor=false&markers=icon:http://gms-world.appspot.com/images/flagblue.png|<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>" alt="Landmark on Google Map" width="640" height="256"/>
                              </a>
                          </p> 
                          <p class="post-details">
                            <a href="/showLandmark/<%= key %>/fullScreen">See full screen map</a>, <a href="/landmarks.jsp?lat=<%= landmark.getLatitude()%>&lng=<%= landmark.getLongitude()%>">Landmarks (Experimental)</a> 
                            <%= request.getAttribute("address") != null ? "<br/>Geocode address: " + request.getAttribute("address") : ""%><br/>
                            Latitude: <%= StringUtil.formatCoordE6(landmark.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(landmark.getLongitude()) %><br/>
                            Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate()) %> by <a href="<%= landmark.getLayer().equals("Social") ? response.encodeURL("/blogeo/" + landmark.getUsername()) : response.encodeURL("/showUser/" + landmark.getUsername())%>"><%= UrlUtils.createUsernameMask(landmark.getUsername()) %></a> | Created in layer <a href="/showLayer/<%= landmark.getLayer() %>"><%= LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) %></a><br/>
                          </p>
                   
                        <%
                              if (!landmark.getLayer().equals("Social"))
                              {
                        %>
                        <h4>Check-ins</h4>
                        <p class="post-info">
                            Number of check-ins: <b><%= request.getAttribute("checkinsCount") == null ? "0" : request.getAttribute("checkinsCount")%></b> <br/>
                            <%
                                                             if (request.getAttribute("checkinsCount") != null) {
                                                                 String lastCheckinUsername = (String)request.getAttribute("lastCheckinUsername");
                            %>
                            Last check-in by user <a href="<%= response.encodeURL("/showUser/" + lastCheckinUsername) %>"><%= UrlUtils.createUsernameMask(lastCheckinUsername) %></a> on  <%= request.getAttribute("lastCheckinDate")%>
                            <%
                               }
                               if (request.getAttribute("comments") != null)
                               {
                                    List<Comment> commentList = (List<Comment>) request.getAttribute("comments");
                            %>
                        </p>
                        <h4><%= commentList.size() %> comments</h4>
                        <div>
                           <ol class="commentlist">
<%
                             for (Comment comment : commentList)
                             {
%>
                                <li class="depth-1">
                                    <div class="comment-info">
                                        <img alt="user photo" src="/images/gravatar.jpg" class="avatar" height="40" width="40" />
                                        <cite>
<%
                             if (StringUtil.isSocialUser(comment.getUsername()))
                             {
%>
                                 <a href="/socialProfile?uid=<%= comment.getUsername() %>"><%= UrlUtils.createUsernameMask(comment.getUsername()) %></a>
<%
                             } else {
%>
                                 <%= comment.getUsername() %>
<%
                             }
%>
                                            Says: <br/>
                                            <span class="comment-data"><%= DateUtils.getFormattedDateTime(request.getLocale(), comment.getCreationDate()) %></span>
                                        </cite>
                                    </div>
                                    <div class="comment-text">
                                        <p><%= comment.getMessage() %></p>
                                    </div>
                                </li>
                                <%
                                  }
                                %>
                            </ol>
                        </div>
                        <%
                               }
                            }
                         }
                        %>
                    </div>

                    <%@ include file="/WEB-INF/jspf/ad_medium_baner.jspf" %>
                    <!-- /main -->
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

        <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>

