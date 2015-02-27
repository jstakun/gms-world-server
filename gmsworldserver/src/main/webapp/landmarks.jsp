<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.layers.GeocodeUtils"%>
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

      function initialize() {
        map = new google.maps.Map(document.getElementById('map-canvas'), {
          zoom: 14,
          center: new google.maps.LatLng(<%= latitude %>, <%= longitude %>),
          mapTypeId: google.maps.MapTypeId.TERRAIN
        });

        // Create a <script> tag and set the USGS URL as the source.
        var script = document.createElement('script');
        script.src = 'http://localhost:8080/geoJsonProvider?lat=<%= latitude %>&lng=<%= longitude %>&layer=Foursquare&callback=fs_callback';
        document.getElementsByTagName('head')[0].appendChild(script);
      }

      // Loop through the results array and place a marker for each
      // set of coordinates.
      window.fs_callback = function(results) {
        var image = 'https://playfoursquare.s3.amazonaws.com/press/2014/foursquare-icon-36x36.png';
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
      google.maps.event.addDomListener(window, 'load', initialize)
    </script>
  </head>
  <body>
    <div id="map-canvas"></div>
  </body>
</html>