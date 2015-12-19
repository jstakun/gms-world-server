<%-- 
    Document   : template
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.GeocodeCache,
				net.gmsworld.server.utils.StringUtil,
				net.gmsworld.server.utils.ImageUtils,
				net.gmsworld.server.utils.DateUtils,
				net.gmsworld.server.config.ConfigurationManager" %>
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
                            <img src="<%= ImageUtils.getImageUrl(gc.getLatitude(), gc.getLongitude(), "640x256", 12, true, ConfigurationManager.MAP_PROVIDER.OSM_MAPS) %>" alt="Geocode on the map" title="See geocode on the map" width="640" height="256"/>
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