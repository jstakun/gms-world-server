<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang.StringUtils,
                 com.jstakun.lm.server.utils.HtmlUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
  String token = null;
  if (StringUtils.equals(request.getParameter("generatetoken"),"true")) {
	  token = com.jstakun.lm.server.config.ConfigurationManager.getParam(com.jstakun.lm.server.config.ConfigurationManager.GMS_WORLD_ACCESS_TOKEN, null);  
  } else {	
	  token = request.getHeader("X-GMS-Token") != null ? request.getHeader("X-GMS-Token") : request.getParameter("gmstoken");
  }	
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Select location on the map</title>
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
                var latlng = new google.maps.LatLng(<%= HtmlUtils.getLocaleCoords(request.getLocale()) %>);
                var myOptions =
                    {
                    zoom: 7,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

                var infoWindow = new google.maps.InfoWindow();
                var latlngbounds = new google.maps.LatLngBounds();

                google.maps.event.addListener(map, 'click', function (e) {
                	 var message = "Do you want to discover landmarks around selected location?";

                     var token = "<%= token %>";
                	 
                	 var r = confirm(message);
                	 if (r == true) {
                		 window.location.replace("/newLandmark/" +  e.latLng.lat() + "/" + e.latLng.lng() + "/" + token);
                	 } else {
                	     //                	 
                	 } 
                });

                var centerControlDiv = document.createElement('div');
		        var centerControl = new CenterControl(centerControlDiv, map, latlng);

		        centerControlDiv.index = 1;
		        map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv); 
             }

        	 function CenterControl(controlDiv, map, center) {

           	  	// Set CSS for the control border
           	  	var controlUI = document.createElement('div');
           	  	controlUI.style.backgroundColor = '#fff';
           	  	controlUI.style.border = '2px solid #fff';
           	  	controlUI.style.borderRadius = '3px';
           	  	controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
           	  	controlUI.style.cursor = 'pointer';
           	  	controlUI.style.marginTop = '18px';
           	  	controlUI.style.textAlign = 'center';
           	  	//controlUI.title = 'Click to recenter the map';
           	  	controlDiv.appendChild(controlUI);

           	  	// Set CSS for the control interior
           	  	var controlText = document.createElement('div');
           	  	controlText.style.color = 'rgb(25,25,25)';
           	 	controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
           	  	controlText.style.fontSize = '16px';
           	  	controlText.style.lineHeight = '32px';
           	  	controlText.style.paddingLeft = '4px';
           	  	controlText.style.paddingRight = '4px';
           	  	controlText.innerHTML = 'Click location on the map';
           	  	controlUI.appendChild(controlText);
             }  
        </script>
</head>
<body onLoad="initialize()">
	<div id="map_canvas" style="width:100%; height:100%"></div>
    <script type="text/javascript">
            //<![CDATA[

            var map;
            if (GBrowserIsCompatible()) {

                //Monitor the window resize event and let the map know when it occurs
                if (window.attachEvent) {
                    window.attachEvent("onresize", function() {this.map.onResize()} );
                } else {
                    window.addEventListener("resize", function() {this.map.onResize()} , false);
                }
            }

            //]]>
     </script>
</body>
</html>