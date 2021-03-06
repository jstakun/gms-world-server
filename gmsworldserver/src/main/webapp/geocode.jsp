<%-- 
    Document   : template
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.persistence.GeocodeCache,
                com.jstakun.lm.server.utils.HtmlUtils,
				net.gmsworld.server.utils.StringUtil,
				net.gmsworld.server.utils.ImageUtils,
				org.ocpsoft.prettytime.PrettyTime,
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
                          <a href="/showGeocode/<%= key %>/fullScreen">
                            <img src="<%= ImageUtils.getImageUrl(gc.getLatitude(), gc.getLongitude(), "640x256", 12, true, ConfigurationManager.MAP_PROVIDER.OSM_MAPS, request.isSecure()) %>" alt="Geocode on the map" title="Discover interesting places around" width="640" height="256"/>
                          </a>
                        </p>
                        <p class="post-details">
                          <a href="/showGeocode/<%= key %>/fullScreen">See full screen map</a><br/>
                          Latitude: <%= StringUtil.formatCoordE6(gc.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(gc.getLongitude()) %><br/>
                          Posted <%= new PrettyTime(request.getLocale()).format(gc.getCreationDate()) %><br/>
                          <b><a href="<%= HtmlUtils.getHotelLandmarkUrl(gc.getLatitude(), gc.getLongitude()) %>" target="_blank">Discover hotels around!</a></b>
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