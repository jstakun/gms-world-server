<%-- 
    Document   : fullScreenLandmarkMap
    Created on : 2011-11-02, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.jstakun.lm.server.persistence.Landmark,
                com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,net.gmsworld.server.utils.DateUtils,
                com.jstakun.lm.server.utils.UrlUtils,net.gmsworld.server.utils.StringUtil,
                org.apache.commons.lang.StringEscapeUtils" %>
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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Landmark Full Screen Map</title>
        <% if (landmark != null) {%>
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
                var latlng = new google.maps.LatLng(<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>);
                var myOptions =
                    {
                    zoom: 12,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

                var contentString = '<span style="font-family:Cursive;font-size:14px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;">' +
                                    '<img src="/images/flagblue.png"/><br/>' +
                                    'Name: <%= StringEscapeUtils.escapeJavaScript(landmark.getName()) %>,<br/>' +
                                    'Description: <%= StringEscapeUtils.escapeJavaScript(landmark.getDescription()) %>,<br/>' +
                                    '<%= request.getAttribute("address") != null ? "Geocode address: " + request.getAttribute("address") + ",<br/>" : ""%>' +
                                    'Latitude:<%= StringUtil.formatCoordE6(landmark.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(landmark.getLongitude()) %>,<br/>' +
                                    'Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate()) %> by <%= UrlUtils.createUsernameMask(landmark.getUsername()) %>,<br/>' +
                                    'Created in layer <%= LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) %>.</span>';

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
        <script type="text/javascript">
            //<![CDATA[

            var map;
            if (GBrowserIsCompatible()) {

                // Monitor the window resize event and let the map know when it occurs
                if (window.attachEvent) {
                    window.attachEvent("onresize", function() {this.map.onResize()} );
                } else {
                    window.addEventListener("resize", function() {this.map.onResize()} , false);
                }
            }

            //]]>
        </script>
        <% } else {%>
        No landmark selected
        <% }%>
    </body>
</html>
