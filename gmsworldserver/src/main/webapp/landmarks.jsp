<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.NumberUtils,
                net.gmsworld.server.config.Commons,
                net.gmsworld.server.config.ConfigurationManager"%>
<%
double latitude = NumberUtils.getDouble(request.getParameter("lat"), 52.23);
double longitude = NumberUtils.getDouble(request.getParameter("lng"), 21.02);
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

      var map;
      
      var layers = [
          {"name": "<%= Commons.FOURSQUARE_LAYER %>", "icon" : "foursquare.png"},
          {"name": "<%= Commons.FACEBOOK_LAYER %>", "icon" : "facebook.png"},
          {"name": "<%= Commons.YELP_LAYER %>", "icon" : "yelp.png"},
          {"name": "<%= Commons.GOOGLE_PLACES_LAYER %>", "icon" : "google_plus.png"},
          {"name": "<%= Commons.COUPONS_LAYER %>", "icon" : "dollar.png"},         
          {"name": "<%= Commons.GROUPON_LAYER %>", "icon" : "marker.png"},  
          {"name": "<%= Commons.MC_ATM_LAYER %>", "icon" : "mastercard.png"}, 
          {"name": "<%= Commons.FLICKR_LAYER %>", "icon" : "flickr.png"},     
          {"name": "<%= Commons.LM_SERVER_LAYER %>", "icon" : "gmsworld.png"},     
          {"name": "<%= Commons.PICASA_LAYER %>", "icon" : "picasa.png"},     
          {"name": "<%= Commons.MEETUP_LAYER %>", "icon" : "meetup.png"},     
          {"name": "<%= Commons.YOUTUBE_LAYER %>", "icon" : "youtube.png"},     
          {"name": "<%= Commons.EVENTFUL_LAYER %>", "icon" : "event.png"},     
          {"name": "<%= Commons.OSM_ATM_LAYER %>", "icon" : "credit_cards.png"},     
          {"name": "<%= Commons.OSM_PARKING_LAYER %>", "icon" : "parking.png"},      
          {"name": "<%= Commons.GEOCODES_LAYER %>", "icon" : "wikipedia.png"},  
          {"name": "<%= Commons.LASTFM_LAYER %>", "icon" : "lastfm.png"},  
          {"name": "<%= Commons.WEBCAM_LAYER %>", "icon" : "webcam.png"},
          {"name": "<%= Commons.PANORAMIO_LAYER %>", "icon" : "panoramio.png"},
          {"name": "<%= Commons.FOURSQUARE_MERCHANT_LAYER %>", "icon" : "gift.png"},   
          {"name": "<%= Commons.EXPEDIA_LAYER %>", "icon" : "expedia.png"},         
          {"name": "<%= Commons.HOTELS_LAYER %>", "icon" : "hotel.png"},         
          {"name": "<%= Commons.TWITTER_LAYER %>", "icon" : "twitter.png"},
          {"name": "<%= Commons.INSTAGRAM_LAYER %>", "icon" : "instagram.png"},
          {"name": "<%= Commons.FREEBASE_LAYER %>", "icon" : "freebase.png"},                
      ];

      function initialize() {
    	  map = new google.maps.Map(document.getElementById('map-canvas'), {
      			zoom: 15,
        		center: mapcenter,
        		mapTypeId: google.maps.MapTypeId.ROADMAP //TERRAIN, SATELLITE, HYBRID
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
    				window.open(this.url);
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