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
      var map;

      //1 provider & image
      var fsprovider = '<%= ConfigurationManager.SERVER_URL %>/geoJsonProvider?lat=<%= latitude %>&lng=<%= longitude %>&layer=<%= Commons.FOURSQUARE_LAYER %>&callback=fs_callback';
      var fsimage = '/images/layers/foursquare.png';

      var fbprovider = '<%= ConfigurationManager.SERVER_URL %>/geoJsonProvider?lat=<%= latitude %>&lng=<%= longitude %>&layer=<%= Commons.FACEBOOK_LAYER %>&callback=fb_callback';
      var fbimage = '/images/layers/facebook.png'

      function initialize() {
        map = new google.maps.Map(document.getElementById('map-canvas'), {
          zoom: 14,
          center: new google.maps.LatLng(<%= latitude %>, <%= longitude %>),
          mapTypeId: google.maps.MapTypeId.TERRAIN
        });

        //2 script
        var script1 = document.createElement('script');
        script1.src = fsprovider; 
        document.getElementsByTagName('head')[0].appendChild(script1);

        var script2 = document.createElement('script');
        script2.src = fbprovider; 
        document.getElementsByTagName('head')[0].appendChild(script2);
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

      //3 callback function
      window.fs_callback = function(results) {
          loadMarkers(results, fsimage); 
      }

      window.fb_callback = function(results) {
          loadMarkers(results, fbimage); 
      }
      
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body>
    <div id="map-canvas"></div>
  </body>
</html>