<%-- 
    Document   : fullScreenLandmarkMap
    Created on : 2011-11-02, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="net.gmsworld.server.utils.persistence.Landmark,
				com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
                net.gmsworld.server.utils.DateUtils,
                net.gmsworld.server.utils.UrlUtils,
                net.gmsworld.server.utils.StringUtil,
                com.jstakun.lm.server.utils.HtmlUtils,
                org.apache.commons.lang.StringEscapeUtils,
                net.gmsworld.server.config.Commons" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <%
                Landmark landmark = null;
                if (request.getAttribute("landmark") != null) {
                    landmark = (Landmark) request.getAttribute("landmark");
                }
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title>GMS World landmarks on the map</title>
        <% if (landmark != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=<%= Commons.getProperty(Commons.Property.GOOGLE_API_WEB_KEY) %>">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                var latlng = new google.maps.LatLng(<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>);
                var myOptions =
                    {
                    zoom: 12,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    scaleControl: true
                };

                var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

                var contentString = <%= HtmlUtils.buildLandmarkDescV2(landmark, request.getAttribute("address"), request.getLocale(), false) %>;

                var infowindow = new google.maps.InfoWindow(
                {
                    content: contentString
                });
                
                var marker = new google.maps.Marker(
                {
                    position: latlng,
                    title:"<%= StringEscapeUtils.escapeJavaScript(landmark.getName()) %>",
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
        <% if (landmark != null) {%>
        <div id="map_canvas" style="width:100%; height:100%"></div>
        <% } else {%>
        No landmark selected
        <% }%>
    </body>
</html>
