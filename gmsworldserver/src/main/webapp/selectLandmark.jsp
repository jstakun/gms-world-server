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
  boolean hotelsMode = StringUtils.equals(request.getParameter("hotels"),"true");
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <% if (hotelsMode) { %>
  <meta name="keywords" content="hotels, accommodation, hotel deals, compare hotels, hotel reviews, hotel photos" />
  <title>Select location on the map and discover hotels around</title>
  <% } else { %>
  <title>Select location on the map and discover landmarks around</title>
  <% } %>
  <style type="text/css">
    html, body, #map_canvas { margin: 0; padding: 0; height: 100%; }
  </style>
  <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js">
  </script>
  <script src="/js/jquery.min.js"></script>
  <script type="text/javascript">
    jQuery.fn.center = function () {
        this.css("position","absolute");
        this.css("top", ( $(window).height() - this.height() ) / 2+$(window).scrollTop() + "px");
        this.css("left", ( $(window).width() - this.width() ) / 2+$(window).scrollLeft() + "px");
        return this;
    }
  </script>
  <script type="text/javascript">
   var token = "<%= token %>";

   var hotelsMode = <%= hotelsMode %>;

   function initialize()
   {
        var latlng = new google.maps.LatLng(<%= HtmlUtils.getLocaleCoords(request.getLocale()) %>);
        var myOptions =
        	{
                    zoom: 7,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    mapTypeControl: true,
                    mapTypeControlOptions: {
                      	style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,     
                      	position: google.maps.ControlPosition.LEFT_BOTTOM,  
                      	//style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
                      	//mapTypeIds: [
                      	//  google.maps.MapTypeId.ROADMAP,
                      	//  google.maps.MapTypeId.TERRAIN
                      	//]
                    },   
                    zoomControl: true,
                	zoomControlOptions: {
                    	position: google.maps.ControlPosition.RIGHT_BOTTOM
                	},       
                	streetViewControl: false,                                  
       };

       var map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

       var infoWindow = new google.maps.InfoWindow();
       var latlngbounds = new google.maps.LatLngBounds();

       google.maps.event.addListener(map, 'click', function (e) {
    	   proceedWithSelectedLocation(e.latLng.lat(), e.latLng.lng()); 
       });

       var centerControlDiv = document.createElement('div');
	   var centerControl;

	   if (hotelsMode == true) {
		   centerControl = new CenterControl(centerControlDiv, map, latlng, 'Select location on the map and discover hotels');
	   } else {
		   centerControl = new CenterControl(centerControlDiv, map, latlng, 'Select location on the map and discover landmarks');   
	   }

	   centerControlDiv.index = 1;
	   map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);

	   var shareControlDiv = document.createElement('div');
	   var shareControl = new CenterControl(shareControlDiv, map, latlng, 'Discover hotels around your location');

	   google.maps.event.addDomListener(shareControlDiv, 'click', function() {
		   showStatus("Please share your location");
		   getLocation();
  	   });

	   shareControlDiv.index = 2;
	   map.controls[google.maps.ControlPosition.LEFT_CENTER].push(shareControlDiv);
   }

   function CenterControl(controlDiv, map, center, text) {

       // Set CSS for the control border
       var controlUI = document.createElement('div');
       controlUI.style.backgroundColor = '#fff';
       controlUI.style.border = '2px solid #fff';
       controlUI.style.borderRadius = '3px';
       controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
       controlUI.style.cursor = 'pointer';
       controlUI.style.marginTop = '18px';
       controlUI.style.marginLeft = '18px';
       controlUI.style.textAlign = 'center';
       //controlUI.title = 'title';
       controlDiv.appendChild(controlUI);

       // Set CSS for the control interior
       var controlText = document.createElement('div');
       controlText.style.color = 'rgb(25,25,25)';
       controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
       controlText.style.fontSize = '16px';
       controlText.style.lineHeight = '32px';
       controlText.style.paddingLeft = '4px';
       controlText.style.paddingRight = '4px';
       controlText.innerHTML = text;
       controlUI.appendChild(controlText);
   } 

   function getLocation() {
		//var x = document.getElementById("status");
		var message;
	    if (token == "null") {
	    	message = "Token is required to proceed with your request!";
	    } else if (navigator.geolocation) {
	    	console.log("Please wait. I'm reading now your geolocation...");
	        navigator.geolocation.getCurrentPosition(showPosition, errorCallback, {maximumAge: 60000, timeout: 30000});
	    } else { 
	    	message = "Geolocation is not supported by this browser!";
	    }
	    if (message != null) {
	    	showStatus(message);
		}
	 }

	 function showPosition(position) {
		 proceedWithSelectedLocation(position.coords.latitude, position.coords.longitude);   
     }

	 function errorCallback(error) {
	 	//var x = document.getElementById("status");
		var message;
		console.log("Error: " + error);
		switch(error.code) {
    		case error.PERMISSION_DENIED:
        		message = "User denied the request for geolocation!"
        		break;
    		case error.POSITION_UNAVAILABLE:
    			message = "Location information is unavailable!"
        		break;
    		case error.TIMEOUT:
    			message = "The request to get user location timed out!"
        		break;
    		case error.UNKNOWN_ERROR:
    			message = "An unknown error occurred!"
        		break;
	 	}
	 	if (message != null) {
	 		showStatus(message);
		}
	 } 

	 function proceedWithSelectedLocation(lat, lng) {
		 var message = "";

         //map.panTo(e.latLng);
         //map.setCenter(e.latLng);
         	 
         if (hotelsMode == true) {
         	  message = "Do you want to find hotels around selected location?";
         } else {
         	  message = "Do you want to discover landmarks around selected location?";
         }
         	 
         var r = confirm(message);

         if (r == true) {
              if (hotelsMode == true) {
             	 window.location.replace("/hotelLandmark/" +  lat + "/" + lng + "/" + token);   
              } else {
         		 window.location.replace("/newLandmark/" +  lat + "/" + lng + "/" + token);
              }
         } else {
         	     //                	 
         } 
    }

	function showStatus(message) {
		$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
        $("#status").html(message);
		$("#status").center().show().delay(3000).queue(function(n) {
				  $(this).hide(); n();
		});
	}    

   google.maps.event.addDomListener(window, 'load', initialize);
  </script>
</head>
<body>
	<div id="map_canvas"></div>
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:16px;line-height:32px;padding-left:4px;padding-right:4px"></div>
</body>
</html>