<%-- 
    Document   : fullScreenLandmarkMap
    Created on : 2011-11-02, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="net.gmsworld.server.utils.persistence.GeocodeCache,
		com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
        com.jstakun.lm.server.utils.HtmlUtils,
        net.gmsworld.server.utils.DateUtils,
        net.gmsworld.server.utils.UrlUtils,
        net.gmsworld.server.utils.StringUtil,
        org.apache.commons.lang.StringEscapeUtils,
        org.apache.commons.lang.StringUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <%
         GeocodeCache gc = null;
         if (request.getAttribute("geocodeCache") != null) {
             gc = (GeocodeCache) request.getAttribute("geocodeCache");
         }
         boolean isMobile = StringUtils.equals(request.getParameter("mobile"), "true");
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title>GMS World geocode on the map</title>
        <% if (gc != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyD_bSG1hQY2x8dfUTeSegTqfIChsvLzUJI">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                var latlng = new google.maps.LatLng(<%= gc.getLatitude()%>,<%= gc.getLongitude()%>);
                var myOptions =
                    {
                    zoom: 12,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    scaleControl: true
                };

                var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

                var contentString = <%= HtmlUtils.buildGeocodeDescV2(gc, request.getAttribute("address"), request.getLocale(), isMobile) %>

                var infowindow = new google.maps.InfoWindow(
                {
                    content: contentString
                });
                
                var marker = new google.maps.Marker(
                {
                    position: latlng,
                    title:"<%= StringEscapeUtils.escapeJavaScript(gc.getLocation())%>",
                    map: map,
                    icon: image
                });

                google.maps.event.addListener(marker, 'click', function() {
                    infowindow.open(map,marker);
                });
            }
        </script>
        <% }%>
    </head>
    <body onLoad="initialize()">
        <% if (gc != null) {%>
        <div id="map_canvas" style="width:100%; height:100%"></div>
        <% } else {%>
        No landmark selected
        <% }%>
    </body>
</html>
