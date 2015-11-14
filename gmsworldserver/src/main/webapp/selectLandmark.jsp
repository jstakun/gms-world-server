<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang.StringUtils,
                 com.jstakun.lm.server.utils.HtmlUtils,
                 net.gmsworld.server.config.Commons" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>                 
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
  <title><bean:message key="hotels.header" /></title>
  <% } else { %>
  <title><bean:message key="landmarks.header" /></title>
  <% } %>
  <style type="text/css">
    html, body, #map_canvas { 
    	margin: 0; 
    	padding: 0; 
    	height: 100%; 
    }
    
    .controls {
  		margin-top: 10px;
  		border: 1px solid transparent;
  		border-radius: 2px 0 0 2px;
  		box-sizing: border-box;
  		-moz-box-sizing: border-box;
  		height: 28px;
  		outline: none;
  		box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
	}

	#pac-input {
  		background-color: #fff;
  		font-family: Roboto,Arial,sans-serif;
  		font-size: 16px;
  		font-weight: 300;
  		margin-left: 12px;
  		padding: 0 11px 0 13px;
  		text-overflow: ellipsis;
  		width: 300px;
	}

	#pac-input:focus {
  		border-color: #4d90fe;
	}

	.pac-container {
  		font-family: Roboto,Arial,sans-serif;
	}

	#type-selector {
  		color: #fff;
  		background-color: #4d90fe;
  		padding: 5px 11px 0px 11px;
	}

	#type-selector label {
  		font-family: Roboto,Arial,sans-serif;
  		font-size: 16px;
  		font-weight: 300;	
	}  
  </style>
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

   var map;
   
   var topcities = [
		<%= HtmlUtils.getTopLocations() %>
   ];
   
   function initialize()
   {
<% if (request.getParameter("lat") != null && request.getParameter("lng") != null && request.getParameter("zoom") != null) { %>	   
        var latlng = new google.maps.LatLng(<%= request.getParameter("lat") %>,<%= request.getParameter("lng") %>);
        var zoom = <%= request.getParameter("zoom") %>;
<% } else { %>
		var latlng = new google.maps.LatLng(<%= HtmlUtils.getLocaleCoords(request.getLocale()) %>);
		var zoom = 7;
<% } %>
        
        var myOptions =
        	{
                    zoom: zoom,
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
                	//streetViewControl: false,                                  
       };

       map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

       var desc;

       if (hotelsMode == true) {
           desc = '<bean:message key="landmarks.marker.desc" />'; 
       } else {
           desc = '<bean:message key="hotels.marker.desc" />'; 
       } 
       
       for (var i = 0; i < topcities.length; i++) {
    	   var marker = new google.maps.Marker({
    		    position: {lat: topcities[i].lat, lng: topcities[i].lng},
    		    map: map,
    		    title: topcities[i].name,
    		    icon: '/images/ok.png',
    		    desc: desc
    		});   		
    	    google.maps.event.addListener(marker, 'click', function() {
    	    	proceedWithSelectedLocation(this.getPosition().lat(), this.getPosition().lng(), this.getTitle());   
     		});          
     		console.log("Added marker " + topcities[i].name + " to the map")
       }
       
       var infoWindow = new google.maps.InfoWindow();
       var latlngbounds = new google.maps.LatLngBounds();

       google.maps.event.addListener(map, 'click', function (e) {
    	   proceedWithSelectedLocation(e.latLng.lat(), e.latLng.lng(), null); 
       });

       var centerControlDiv = document.createElement('div');
	   var centerControl;

	   if (hotelsMode == true) {
		   centerControl = new CenterControl(centerControlDiv, map, latlng, '<bean:message key="hotels.header" />');
	   } else {
		   centerControl = new CenterControl(centerControlDiv, map, latlng, '<bean:message key="landmarks.header" />');  
	   }

	   centerControlDiv.index = 1;
	   map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);

	   var shareControlDiv = document.createElement('div');

	   var message
	   if (hotelsMode == true) {
			message = "<img src='/images/mypos.png' title='<bean:message key="hotels.your.location" />'/>";
	   } else {
		    message = "<img src='/images/mypos.png' title='<bean:message key="landmarks.your.location" />'/>";
	   }
	   
	   var shareControl = new CenterControl(shareControlDiv, map, latlng, message);

	   google.maps.event.addDomListener(shareControlDiv, 'click', function() {
		   showStatus("<bean:message key="landmarks.location.prompt" />");
		   getLocation();
  	   });

	   shareControlDiv.index = 2;
	   map.controls[google.maps.ControlPosition.LEFT_CENTER].push(shareControlDiv);

	   var topLocationsDiv = document.createElement('div');
	   var topLocationsControl = new CenterControl(topLocationsDiv, map, latlng, '<img src=\'/images/ok.png\' style=\'width:24px; height:24px; vertical-align: middle;\'><span style=\'line-height:24px;\'>&nbsp;<bean:message key="hotels.top.destinations" /></span>');

	   topLocationsDiv.index = 3
	   map.controls[google.maps.ControlPosition.RIGHT_TOP].push(topLocationsDiv);	   

	   //search box
	   var input = document.getElementById('pac-input');
	   var searchBox = new google.maps.places.SearchBox(input);
	   map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

	   map.addListener('bounds_changed', function() {
	     	searchBox.setBounds(map.getBounds());
	   });

	   searchBox.addListener('places_changed', function() {
		    var places = searchBox.getPlaces();

		    if (places.length == 0) {
			    showStatus('<bean:message key="landmarks.notfound" />');
		    	console.log('No matching place found!');
		     	return;
		    } else if (places.length > 1) {
		    	console.log('Found ' + places.length + ' places. First one will be selected...');
			}

		    console.log('Selected place: ' + places[0].name + ' - ' + places[0].geometry.location);

		    map.setZoom(10);
            map.setCenter(places[0].geometry.location);
		    //proceedWithSelectedLocation(places[0].geometry.location.lat(), places[0].geometry.location.lng(), places[0].name);
	   });	    	   
   }

   function CenterControl(controlDiv, map, center, text) {

       // Set CSS for the control border
       var controlUI = document.createElement('div');
       controlUI.style.backgroundColor = '#fff';
       controlUI.style.border = '2px solid #fff';
       controlUI.style.borderRadius = '2px';
       controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
       controlUI.style.cursor = 'pointer';
       controlUI.style.marginTop = '10px';
       controlUI.style.marginLeft = '10px';
       controlUI.style.marginRight = '10px';
       controlUI.style.marginBottom = '10px';
 	   controlUI.style.textAlign = 'center';
       controlUI.title = text;
       controlDiv.appendChild(controlUI);

       // Set CSS for the control interior
       var controlText = document.createElement('div');
       controlText.style.color = 'rgb(25,25,25)';
       controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
       controlText.style.fontSize = '16px';
       controlText.style.lineHeight = '26px';
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
		    console.log(message);
	    	showStatus('<bean:message key="landmarks.error" />');
		}
	 }

	 function showPosition(position) {
		 console.log("Geolocation found!");
		 var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
		 map.panTo(latlng);
		 proceedWithSelectedLocation(position.coords.latitude, position.coords.longitude, null);   
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
		 	log.console(message);
	 		showStatus('<bean:message key="landmarks.error" />');
		}
	 } 

	 function proceedWithSelectedLocation(lat, lng, name) {
		 var message = "";

         //map.panTo(e.latLng);
         //map.setCenter(e.latLng);
         if (name == null) {
			  name = "<bean:message key="landmarks.location" />";
         }
         	  	 
         if (hotelsMode == true) {
              message = "<bean:message key="hotels.confirmation" /> " + name + "?"; 
         } else {
         	  message = "<bean:message key="landmarks.confirmation" /> " + name + "?";
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

    //google.maps.event.addDomListener(window, 'load', initialize);
  </script>
  <script src="https://maps.googleapis.com/maps/api/js?libraries=places&callback=initialize" async defer> 
    //key=
  </script>
</head>
<body>
    <input id="pac-input" class="controls" type="text" placeholder="<bean:message key="landmarks.search" />">
	<div id="map_canvas"></div>
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:16px;line-height:28px;padding-left:4px;padding-right:4px"></div>
</body>
</html>