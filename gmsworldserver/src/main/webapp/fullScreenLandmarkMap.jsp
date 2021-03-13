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
                net.gmsworld.server.config.Commons,
                org.apache.commons.lang.StringUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <%
                Landmark landmark = null;
                if (request.getAttribute("landmark") != null) {
                    landmark = (Landmark) request.getAttribute("landmark");
                }
                final boolean isDevice = StringUtils.equalsIgnoreCase((String)request.getAttribute("type"),"device");
                final String imei = (String) request.getAttribute("imei");
                final String deviceName = (String) request.getAttribute("deviceName");
                final String status = (String)request.getAttribute("status");
                String image = "flagblue.png";
                String title = "GMS World landmarks on the map";
                if (isDevice) {
                	image = "dl_32.png";
                	title = "Device";
                	if (landmark != null) {
                		title = landmark.getName();
                	}
                	title += " location on the map";
                }
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title><%= title %></title>
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

                var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

                var contentString = <%= HtmlUtils.buildLandmarkDescV2(landmark, request.getAttribute("address"), request.getLocale(), false) %>;

                var infowindow = new google.maps.InfoWindow(
                {
                    content: contentString
                });
                
                var marker = new google.maps.Marker({
                    position: latlng,
                    title:"<%= StringEscapeUtils.escapeJavaScript(landmark.getName()) %>",
                    map: map,
                    icon: {
                        url: '/images/<%= image %>',
                        scaledSize: new google.maps.Size(32, 32),
                        anchor: new google.maps.Point(16, 16),
                    },
                });

         <% if (landmark.getAltitude() > 0d) { %>

				var accuracyOptions = {
        				strokeColor: '#87CEFA',
        				strokeOpacity: 0.7,
        				strokeWeight: 2,
        				fillColor: '#87CEFA',
        				fillOpacity: 0.35,
        				map: map,
        				center: latlng,
        				radius: <%= landmark.getAltitude() %>
    			};
    			circle = new google.maps.Circle(accuracyOptions);
         <% } %>

                google.maps.event.addListener(marker, 'click', function() {
                    infowindow.open(map,marker);
                });
                           
                <% if (isDevice) { %>
                var centerControlDiv = document.getElementById('centerMap');
		        centerControlDiv.index = 1;
	            CenterControl2(centerControlDiv, 'center', 'Locate now');
	            map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(centerControlDiv);
	            google.maps.event.addDomListener(centerControlDiv, 'click', function() {
	    	   	    window.location.reload();
	    	    });
            }    

            function CenterControl2(controlDiv, align, title) {

                    // Set CSS for the control border
                    controlDiv.style.display = "block";
                    controlDiv.style.backgroundColor = '#fff';
                    controlDiv.style.border = '2px solid #fff';
                    controlDiv.style.borderRadius = '3px';
                    controlDiv.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
                    controlDiv.style.cursor = 'pointer';
                    controlDiv.style.marginTop = '10px';
                    controlDiv.style.marginLeft = '10px';
                    controlDiv.style.marginBottom = '16px';
                    controlDiv.style.marginRight = '10px';
                    controlDiv.style.textAlign = align; 
                    controlDiv.title = title;
                    
                    // Set CSS for the control interior
                    controlDiv.style.color = 'rgb(25,25,25)';
                    controlDiv.style.fontFamily = 'Roboto,Arial,sans-serif';
                    controlDiv.style.fontSize = '16px';
                    controlDiv.style.lineHeight = '32px'; //scale
                    controlDiv.style.paddingLeft = '4px';
                    controlDiv.style.paddingRight = '4px';
             }    
         
         <% } %>        
        </script>
    <% }%>
    </head>
    <body onLoad="initialize()">
        <% if (landmark != null) {%>
        <div id="map_canvas" style="width:100%; height:100%"></div>
        <div id="centerMap" style="display:none;">Locate now</div>
        <% } else if (imei != null && isDevice && request.getAttribute("landmarkFound") != null) {%>
        <h3><%= deviceName != null ? "Device " + deviceName : "This device"  %> location is currently unknown. Please click <a href="/showDevice/<%= imei %>">here</a> to discover it now!</h3>
             <% if (StringUtils.isNotEmpty(status)) { %>
             	  Last discovery status: <%= status %>
             <% } %>
        <% } else if (imei != null && isDevice) {%>
        <h3><%= deviceName != null ? "Device " + deviceName : "This device"  %> location is currently unknown. Please open Device Locator mobile application on this device and finish device registration and later click again <a href="/showDevice/<%= imei %>">here</a>!</h3>
        <% } else if (isDevice) {%>
        <h3>This device location is currently unknown. Please open this page again later!</h3>
        <% } else {%>
        <h3>Item not found.</h3>
        <% } %>
    </body>
</html>
