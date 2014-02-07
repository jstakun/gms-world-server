<%-- 
    Document   : template
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.GeocodeCache,
                com.jstakun.lm.server.utils.StringUtil,
                com.jstakun.lm.server.utils.DateUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<%
   GeocodeCache gc = null;
   if (request.getAttribute("geocodeCache") != null) {
      gc = (GeocodeCache) request.getAttribute("geocodeCache");
   }
   String key = request.getParameter("key");
%>
    <head>
        <title>Geocode location on the map</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>
    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <div class="post">
                        <% if (gc == null) {
                        %>
                        <h3>No geocode selected</h3>
                        <%
                           } else {
                        %>
                        <h3>Geocode location for: <%= gc.getLocation() %></h3>

                        <p class="image-section">
                          <a href="/showGeocode.do?key=<%= key %>&fullScreenGeocodeMap=1">
                            <img src="http://maps.google.com/maps/api/staticmap?center=<%= gc.getLatitude() %>,<%= gc.getLongitude() %>&zoom=12&size=640x256&sensor=false&markers=icon:http://gms-world.appspot.com/images/flagblue.png|<%= gc.getLatitude() %>,<%= gc.getLongitude() %>" alt="Landmark on Google Map" height="256" width="640"/>
                          </a>
                        </p>
                        <p class="post-details">
                          <a href="/showGeocode.do?key=<%= key %>&fullScreenGeocodeMap=1">See full screen map</a><br/>
                          Latitude: <%= StringUtil.formatCoordE6(gc.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(gc.getLongitude()) %><br/>
                          Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), gc.getCreationDate()) %>
                        </p>

                        <%
                           }
                        %>
                    </div>
                    <!-- /main -->
                     <%@ include file="/WEB-INF/jspf/ad_medium_baner.jspf" %>
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->

        </div>

       <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>