<%-- 
    Document   : fullScreenHeatMap
    Created on : 2012-10-18, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.jstakun.lm.server.persistence.Landmark,
        com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
        java.util.Map,
        java.util.Iterator,
        net.gmsworld.server.utils.DateUtils,
        net.gmsworld.server.utils.UrlUtils,
        org.apache.commons.lang.StringEscapeUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <%
                Map<String, Integer> heatMapData = null;
                Double centerLat = 0.0;
                Double centerLon = 0.0;
                if (request.getAttribute("heatMapData") != null) {
                    heatMapData = (Map<String, Integer>) request.getAttribute("heatMapData");
                }
                if (request.getAttribute("centerLat") != null) {
                    centerLat = (Double) request.getAttribute("centerLat");
                }
                if (request.getAttribute("centerLon") != null) {
                    centerLon = (Double) request.getAttribute("centerLon");
                }
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        <title>GMS World Heat Map</title>
        <% if (heatMapData != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                var latlng = new google.maps.LatLng(<%= centerLat%>,<%= centerLon%>);
                var myOptions =
                    {
                    zoom: 3,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

                var pointArray = new google.maps.MVCArray(landmarks);

                var heatmap = new google.maps.visualization.HeatmapLayer({
                    data: pointArray,
                    maxIntensity: 8,
                    opacity: 0.6
                });

                heatmap.setMap(map);
            }

            var landmarks = [
      <%
         for (Iterator<Map.Entry<String,Integer>> iter = heatMapData.entrySet().iterator(); iter.hasNext();) {
             Map.Entry<String,Integer> entry = iter.next();
             String[] key = entry.getKey().split("_");
      %>
                   {location: new google.maps.LatLng(<%= key[0] %>, <%= key[1] %>), weight: <%= entry.getValue() %>},
      <%
         }
      %>
             ];

            google.maps.event.addDomListener(window, 'load', initialize);   
        </script>
        <% }%>
    </head>
    <body>
        <% if (heatMapData != null) {%>
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
        No landmark list selected
        <% }%>
    </body>
</html>
