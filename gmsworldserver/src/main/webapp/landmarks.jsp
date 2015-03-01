<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.layers.GeocodeUtils,
                net.gmsworld.server.config.Commons,
                net.gmsworld.server.config.ConfigurationManager"%>
<%
double latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
double longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
%>
<!DOCTYPE html>
<html>
  <head>
    <style>
      html, body, #map-canvas { margin: 0; padding: 0; height: 100%; }
    </style>
    <script
      src="https://maps.googleapis.com/maps/api/js?libraries=visualization">
    </script>
    <script>
      var mapcenter = new google.maps.LatLng(<%= latitude %>, <%= longitude %>);
    
      var layers = [
          {"name": "<%= Commons.FOURSQUARE_LAYER %>", "icon" : "foursquare.png"},
          {"name": "<%= Commons.FACEBOOK_LAYER %>", "icon" : "facebook.png"}
      ];

      function initialize() {
    	  var map = new google.maps.Map(document.getElementById('map-canvas'), {
      			zoom: 14,
        		center: mapcenter,
        		mapTypeId: google.maps.MapTypeId.TERRAIN
          });

          for (var i = 0; i < layers.length; i++) {
        		var script = document.createElement('script');
        		script.src = '<%= ConfigurationManager.SERVER_URL %>/geoJsonProvider?lat=<%= latitude %>&lng=<%= longitude %>&layer=' + layers[i].name + '&callback=layers_callback'; 
        		document.getElementsByTagName('head')[0].appendChild(script);
          }

          var flagmarker = new google.maps.Marker({
  				position: mapcenter,
  				map: map,
  				icon: '/images/flagblue.png',
  	  	  });
      }

      function loadMarkers(results, image) {
          for (var i = 0; i < results.features.length; i++) {
          		var coords = results.features[i].geometry.coordinates;
          		var latLng = new google.maps.LatLng(coords[1],coords[0]);
          		var marker = new google.maps.Marker({
           			position: latLng,
            		map: map,
            		title: results.features[i].properties.name,
            		icon: image,
            		url: results.features[i].properties.url
          		});
          		google.maps.event.addListener(marker, 'click', function() {
    				window.location.href = this.url;
	  	  });
        }
      }

      window.layers_callback = function(results) {
    	  for (var i = 0; i < layers.length; i++) {
          		if (results.properties.layer == layers[i].name) {
                    var image = '/images/layers/' + layers[i].icon; 
          	  		loadMarkers(results, image);
          	  		break;
          		}   
           } 
      }
      
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body>
    <div id="map-canvas"></div>
  </body>
</html>