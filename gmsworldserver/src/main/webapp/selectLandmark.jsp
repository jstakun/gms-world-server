<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang.StringUtils,
                 com.jstakun.lm.server.utils.HtmlUtils,
                 net.gmsworld.server.config.Commons" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>                 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
  boolean hotelsMode = StringUtils.equals(request.getParameter("hotels"),"true");
%>
<html>
<head>
  <% if (hotelsMode) { %>
  <%@ include file="/WEB-INF/jspf/hotelsonmap_header.jspf" %>
  <title><bean:message key="hotels.header" /></title>
  <% } else { %>
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
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
  <!--script src="/js/jquery.min.js"></script-->
  <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/sunny/jquery-ui.min.css" />
  <script src="http://code.jquery.com/jquery-1.12.3.min.js"></script>
  <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.min.js"></script>
  <script type="text/javascript">
    jQuery.fn.center = function () {
        this.css("position","absolute");
        this.css("top", ( $(window).height() - this.height() ) / 2+$(window).scrollTop() + "px");
        this.css("left", ( $(window).width() - this.width() ) / 2+$(window).scrollLeft() + "px");
        return this;
    }
  </script>  
  <script type="text/javascript">
   var hotelsMode = <%= hotelsMode %>;

   var map;
   
   function initialize()
   {
<% if (request.getParameter("lat") != null && request.getParameter("lng") != null && request.getParameter("zoom") != null) { %>	   
        var latlng = new google.maps.LatLng(<%= request.getParameter("lat") %>,<%= request.getParameter("lng") %>);
        var zoom = <%= request.getParameter("zoom") %>;
<% } else { %>
		var latlng = new google.maps.LatLng(<%= HtmlUtils.getLocaleCoords(request.getLocale(), pageContext.getServletContext()) %>);
		var zoom = 7;
<% } %>
        
        var myOptions = {
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

       //add other markers here
       var topcities = <%= HtmlUtils.getTopLocations(pageContext.getServletContext()) %>;
       loadMarkers(topcities, '/images/ok.png', desc);
       //
       
       var infoWindow = new google.maps.InfoWindow();
       var latlngbounds = new google.maps.LatLngBounds();

       google.maps.event.addListener(map, 'click', function (e) {
    	   proceedWithSelectedLocation(e.latLng.lat(), e.latLng.lng(), null); 
       });

       //top header
       var centerControlDiv = document.createElement('div');
	   var centerControl;
	   if (hotelsMode == true) {
		   centerControl = new CenterControl(centerControlDiv, '<bean:message key="hotels.header" />', true);
	   } else {
		   centerControl = new CenterControl(centerControlDiv, '<bean:message key="landmarks.header" />', true);  
	   }
	   centerControlDiv.index = 1;
	   map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);

	   //my location box
	   var shareControlDiv = document.createElement('div');
	   var message
	   if (hotelsMode == true) {
			message = "<img src='/images/mypos.png' title='<bean:message key="hotels.your.location" />'/>";
	   } else {
		    message = "<img src='/images/mypos.png' title='<bean:message key="landmarks.your.location" />'/>";
	   }
	   var shareControl = new CenterControl(shareControlDiv, message, true);
	   google.maps.event.addDomListener(shareControlDiv, 'click', function() {
		   showStatus("<bean:message key="landmarks.location.prompt" />");
		   getLocation();
  	   });
	   shareControlDiv.index = 2;
	   map.controls[google.maps.ControlPosition.LEFT_CENTER].push(shareControlDiv);

       //top locations box 
	   var topLocationsDiv = document.createElement('div');
	   var topLocationsControl = new CenterControl(topLocationsDiv, '<img src=\'/images/ok.png\' style=\'width:24px; height:24px; vertical-align: middle;\'><span style=\'line-height:24px;\'>&nbsp;<bean:message key="hotels.top.destinations" /></span>', false);
	   topLocationsDiv.index = 3
	   map.controls[google.maps.ControlPosition.RIGHT_TOP].push(topLocationsDiv);	   

	   //checkin dates box
	   var checkinDiv = document.getElementById('checkin');
	   checkinDiv.index = 4
	   map.controls[google.maps.ControlPosition.RIGHT_CENTER].push(checkinDiv);	   
	   
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
		    } else if (places.length >= 1) {
                if (places.length > 1) {
		    		console.log('Found ' + places.length + ' places. First one will be selected...');
                }
                console.log('Selected place: ' + places[0].name + ' - ' + places[0].geometry.location);
              	//center map in selected location 
    		    //map.setZoom(10);
                //map.setCenter(places[0].geometry.location);
    		    //start hotels search in selected location
    		    proceedWithSelectedLocation(places[0].geometry.location.lat(), places[0].geometry.location.lng(), places[0].name);
                //
    		}

		});	    	   
   }

   function CenterControl(controlDiv, text, addTitle) {

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
	   controlDiv.appendChild(controlUI);	   
	   
	   if (addTitle) {
       		controlUI.title = text;
       }
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
	    if (navigator.geolocation) {
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
            	  var checkin = document.getElementById("checkinDate").value;
            	  if (isEmpty(checkin)) {
					 checkin = "0";
                  }
            	  var checkout = document.getElementById("checkoutDate").value; 
            	  if (isEmpty(checkout)) {
 					 checkout = "0";
                  }          
             	  window.location.replace("/hotelLandmark/" +  encodeDouble(lat) + "/" + encodeDouble(lng) + "/" + checkin + "/" + checkout);   
              } else {
         		 window.location.replace("/newLandmark/" +  encodeDouble(lat) + "/" + encodeDouble(lng));
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

	function loadMarkers(markers, icon, desc) {
		for (var i = 0; i < markers.length; i++) {
	    	   var marker = new google.maps.Marker({
	    		    position: {lat: markers[i].lat, lng: markers[i].lng},
	    		    map: map,
	    		    title: markers[i].name,
	    		    icon: icon,
	    		    desc: desc
	    		});   		
	    	    google.maps.event.addListener(marker, 'click', function() {
	    	    	proceedWithSelectedLocation(this.getPosition().lat(), this.getPosition().lng(), this.getTitle());   
	     		});          
	     		console.log("Added marker " + markers[i].name + " to the map")
	     }
	} 

	function encodeDouble(val) {
 		var v = val * 1E6;
 		var iv = parseInt(v, 10);
 		var n = iv.toString(); 
 		var out = "";
 		for (var i = 0; i < n.length; i++) {
            var k = n.charCodeAt(i) + 64;
			out += String.fromCharCode(k);
 	 	}
 	 	return out;
	}

	function isEmpty(str) {
	    return (!str || 0 === str.length);
	}

  </script>
  <script src="https://maps.googleapis.com/maps/api/js?libraries=places&callback=initialize" async defer> 
    //key=
  </script>
</head>
<body>
    <input id="pac-input" class="controls" type="text" placeholder="<bean:message key="landmarks.search" />">
	<div id="map_canvas"></div>
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:16px;line-height:28px;padding-left:4px;padding-right:4px"></div>
    <div id="checkin" style="background-color:#fff;border:2px solid #fff;border-radius:3px;box-shadow:0 2px 6px rgba(0,0,0,.3);color:black;font-family:Roboto,Arial,sans-serif;font-size:16px;line-height:28px;padding-left:4px;padding-right:4px;margin-right:10px">
    <table><tr><td>Check-in date</td><td><input type="text" id="checkinDate" size="10"></td></tr><tr><td>Check-out date</td><td><input type="text" id="checkoutDate" size="10"></td></tr></table>
    </div>
    <script type="text/javascript">
      $(function() {
	     var daysToAdd = 1;
	     $("#checkinDate").datepicker({
	        onSelect: function (selected) {
	            var dtMax = new Date(selected);
	            dtMax.setDate(dtMax.getDate() + daysToAdd); 
	            var dd = dtMax.getDate();
	            var mm = dtMax.getMonth() + 1;
	            var y = dtMax.getFullYear();
	            var dtFormatted = y + '-'+ mm + '-'+ dd;
	            $("#checkoutDate").datepicker("option", "minDate", dtFormatted);
	        }, minDate: 0, dateFormat: 'yy-mm-dd'
	     });
	    
	     $("#checkoutDate").datepicker({
	        onSelect: function (selected) {
	            var dtMax = new Date(selected);
	            dtMax.setDate(dtMax.getDate() - daysToAdd); 
	            var dd = dtMax.getDate();
	            var mm = dtMax.getMonth() + 1;
	            var y = dtMax.getFullYear();
	            var dtFormatted = y + '-'+ mm + '-'+ dd;
	            $("#checkinDate").datepicker("option", "maxDate", dtFormatted)
	        },  minDate: 1, dateFormat: 'yy-mm-dd'
	     });                 
      })
    </script>
</body>
</html>