<%-- 
    Document   : fullScreenLandmarkMap
    Created on : 2011-11-02, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.jstakun.lm.server.persistence.GeocodeCache,
        com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
        net.gmsworld.server.utils.DateUtils,
        net.gmsworld.server.utils.UrlUtils,
        net.gmsworld.server.utils.StringUtil,
        org.apache.commons.lang.StringEscapeUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <%
                GeocodeCache gc = null;
                if (request.getAttribute("geocodeCache") != null) {
                    gc = (GeocodeCache) request.getAttribute("geocodeCache");
                }
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        <title>Geocode Full Screen Map</title>
        <% if (gc != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                var latlng = new google.maps.LatLng(<%= gc.getLatitude()%>,<%= gc.getLongitude()%>);
                var myOptions =
                    {
                    zoom: 12,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

                var contentString = '<span style="font-family:Roboto,Arial,sans-serif;font-size:16px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;">' +
                    '<b><%= StringEscapeUtils.escapeJavaScript(gc.getLocation())%></b><br/>' +
                    '<img src="https://maps.googleapis.com/maps/api/streetview?size=200x150&location=<%=gc.getLatitude()%>,<%=gc.getLongitude()%>" style="margin: 4px 0px" title="Location street view image"/><br/>' +
                    '<%= request.getAttribute("address") != null ? "Geocode address: " + request.getAttribute("address") + ",<br/>" : ""%>' +
                    'Latitude: <%= StringUtil.formatCoordE6(gc.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(gc.getLongitude()) %>,<br/>' +
                    'Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), gc.getCreationDate())%>.</span>';

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
