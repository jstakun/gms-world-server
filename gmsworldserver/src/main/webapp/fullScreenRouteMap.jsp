<%-- 
    Document   : fullScreenRouteMap
    Created on : 2017-04-30, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.lang.String" %>
<%
       String route = (String) request.getAttribute("route");
       int interval = 10000;
       if (request.getParameter("interval") != null) {
    	    try {
    	    	 int tmp = Integer.parseInt(request.getParameter("interval"));
    	    	 if (tmp > interval) {
    	    		  interval = tmp;
    	    	 }
    	    } catch (Exception e) {
    	    	
    	    }
       }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title>See your recorded route on the map</title>
        <% if (route != null) {%>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyD_bSG1hQY2x8dfUTeSegTqfIChsvLzUJI">
        </script>
        <script type="text/javascript">
        	var routePath, endMarker, map, bounds, currentPath;
            
            function initialize() {
            	var myOptions =
                {
                	zoom: 12,
                	mapTypeId: google.maps.MapTypeId.ROADMAP,
                	scaleControl: true
            	};
            	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions); 
            	bounds = new google.maps.LatLngBounds();
				loadRouteFromServer();
				<% if (request.getParameter("now") != null) { %>				
				window.setInterval(loadRouteFromServer, <%= interval %>); //reload every 10 sec
				<% } %>
            }
        
            function loadRouteFromServer() {
                var script = document.getElementById("loadedRoute");
                if (script != null) {
                    document.head.removeChild(script);
                } 
                console.info("Loading route points...");  
                
                script = document.createElement('script');
                script.src = '/routeProvider?route=<%= route %>&callback=loadRoute';
                script.id = 'loadedRoute'
                document.head.appendChild(script);

                window.loadRoute = function(results) {
                	var pathCoords = [];
                	var description = '';   
                	 
                    if (results != null && results.features != null && results.features.length > 0) {
                         //geojson       
                 	 	var geometry = results.features[0].geometry;
                 	 	if (results.features[0].properties.time != null && results.features[0].properties.distance != null) {
                 	 		var time = new Date(results.features[0].properties.time).toISOString().substr(11, 8); //ms
                        	var length = results.features[0].properties.distance / 1000; //km
                        	var avg = results.features[0].properties.distance / (results.features[0].properties.time / 1000) * 3.6; //km/h
                        	//console.log("time: " + results.features[0].properties.time + " lenght: " + results.features[0].properties.distance + " avg: " + avg);
                        	description = "Route length: " + length.toFixed(2) + " km, Average speed: " +  avg.toFixed(2) + " km/h, Estimated time: " + time;
                        } else {
                        	description = results.features[0].properties.description;
                        }        
                        for (var i = 0; i < geometry.coordinates.length; i++) {
                      		var coords = geometry.coordinates[i];
                      		//TODO fix that after new version release
                      		<% if (request.getParameter("lnglat") != null) { %>
                      		var lat = coords[1];
                      		var lng = coords[0];                 
                      		<% } else { %>
                      		var lat = coords[0];
                      		var lng = coords[1];
                      		<% } %>
                      		var latlng = new google.maps.LatLng(lat, lng);
                     		pathCoords.push(latlng);
                     		bounds.extend(latlng);
                      		//console.log("Loading coordinate latitude: " + lat + ", longitude: " + lng);
                        } 
                    } else if (results != null && results.route_geometry != null && results.route_geometry.length > 0) {
                        //mapquest  
                    	var seconds= results.route_summary.total_time
                        var time = new Date(seconds * 1000).toISOString().substr(11, 8); 
                    	var length = results.route_summary.total_distance / 1000; //km
                    	var avg = length / results.route_summary.total_time / 3600;
                    	description = "Route length: " + length.toFixed(2) + " km, Average speed: " +  avg.toFixed(2) + " km/h, Estimated time: " + time;        
                    	//console.log("time: " + results.features[0].properties.time + " lenght: " + results.features[0].properties.distance + " avg: " + avg);    
                        for (var i = 0; i < results.route_geometry.length; i++) {
                      		var coords = results.route_geometry[i];
                      		var latlng = new google.maps.LatLng(coords[0], coords[1]);
                     		pathCoords.push(latlng);
                     		bounds.extend(latlng);
                      		//console.log('Loading point ' + coords[0] + "," + coords[1]);
                    	}    
                    } else {
                    	console.log('No routes found in results: ' + JSON.stringify(results));
                    }

                    console.log("Loaded " + pathCoords.length + " route points");

                    if (pathCoords.length > 0) { 
                         if (currentPath == null) {
                             var startMarker = new google.maps.Marker({
                   	 	        position: pathCoords[0],
                  	     	    map: map,
                  	 	        title: description,
                  	 	        icon: '/images/route-start.png'
                	        }); 
                         }
                     
                         var currentPathLength = 0;
                         if (currentPath != null) {
                        	 currentPathLength = currentPath.length;
                         }
                	      
                         if (routePath == null) {
                    	     routePath = new google.maps.Polyline({
                                  path: pathCoords,
                                  geodesic: true,
                                  strokeColor: '#FF0000',
                                  strokeOpacity: 1.0,
                                  strokeWeight: 4
                            });
                    	    routePath.setMap(map);	 
                            currentPath = pathCoords;
                         } else {
                             var lastRoutePointIndex = -1; 
                             for (var i = pathCoords.length-1; i >= 0; i--) {
							    if (pathCoords[i].lat() == currentPath[currentPath.length-1].lat() && pathCoords[i].lng() ==currentPath[currentPath.length-1].lng()) {
							    	lastRoutePointIndex = i;
							    	console.log("Found current route last point at " + i + "/" + (pathCoords.length-1));
							    	break;				
							    }					
                             }
                             for (var i = lastRoutePointIndex+1; i < pathCoords.length; i++) {
                            	 currentPath.push(pathCoords[i]);
                             }
                             if (currentPath.length > currentPathLength) {
                             	  routePath.setPath(currentPath)
                             }
                         }

                         if (currentPath.length > currentPathLength) {
                         		if (endMarker != null) {
                             		endMarker.setMap(null);
                          		}
                     	
                          		endMarker = new google.maps.Marker({
                       	     		position: pathCoords[pathCoords.length-1],
                       	     		map: map,
                      	     		title: description,
                      	     		<% if (request.getParameter("now") != null) { %>				
             			     		icon: '/images/dl_32.png'
                              		<% } else { %>
                      	     		icon: '/images/route-end.png'
                              		<% } %> 	
                    	     	});
                          }

                         console.log("Current route length: " + currentPath.length + " compared to previous " + currentPathLength);
                          
                         if (currentPath.length > 1 && currentPath.length > currentPathLength) {
                             map.fitBounds(bounds);
                             map.panToBounds(bounds);
                  	     } else if (currentPath.length > currentPathLength) {
                  		     map.setCenter(bounds.getCenter());	
                         }
                        			
                  } else if (routePath == null) {
                        console.log('Route path is empty!');
                        window.alert('Route has not been found. You\'ll be redirected to GMS World main page!'); 
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
