<%-- 
    Document   : fullScreenRouteMap
    Created on : 2017-04-30, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.lang.String" %>
<%
       String route = (String) request.getAttribute("route");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title>Route Full Screen Map</title>
        <% if (route != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?libraries=visualization">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                //var latlng = new google.maps.LatLng(52.25, 20.95);
                var bounds = new google.maps.LatLngBounds();
                var myOptions =
                    {
                    zoom: 12,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    scaleControl: true
                };

                var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

                var script = document.createElement('script');

                script.src = '/routeProvider?route=<%= route %>&callback=loadRoute';
                document.getElementsByTagName('head')[0].appendChild(script);

                window.loadRoute = function(results) {
                	 var pathCoords = [];   

                     if (results != null && results.features != null && results.features.length > 0) {
                	 	var geometry = results.features[0].geometry;

                        for (var i = 0; i < geometry.coordinates.length; i++) {
                      		var coords = geometry.coordinates[i];
                      		var latlng = new google.maps.LatLng(coords[0], coords[1]);
                     		pathCoords.push(latlng);
                     		bounds.extend(latlng);
                      		if (i==0 || i == geometry.coordinates.length-1) {
                    	  		var marker = new google.maps.Marker({
                              		position: latlng,
                             	 	map: map
                           		});		
                      		}
                      		console.log('Loading point ' + coords[0] + "," + coords[1]);
                    	}
                    } else {
                    	console.log('No routes found in results: ' + JSON.stringify(results));
                    }
                     
                    if (pathCoords.length > 0) {
                    	var routePath = new google.maps.Polyline({
                            path: pathCoords,
                            geodesic: true,
                            strokeColor: '#FF0000',
                            strokeOpacity: 1.0,
                            strokeWeight: 4
                         });
                        map.fitBounds(bounds);
                        map.setCenter(bounds.getCenter()); 
                    	routePath.setMap(map);						
                    } else {
                        console.log('Route path is empty!');
                        window.alert('No route found. You\'ll be redirected to main page!'); 
                        window.location='https://www.gms-world.net/';
                    }  
                 }
            }
        </script>
        <% }%>
    </head>
    <body onLoad="initialize()">
        <% if (route != null) {%>
        <div id="map_canvas" style="width:100%; height:100%"></div>
        <% } else {%>
        No route selected
        <% }%>
    </body>
</html>
