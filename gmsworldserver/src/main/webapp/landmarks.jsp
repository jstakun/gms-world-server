<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="org.apache.commons.lang.StringUtils,
                net.gmsworld.server.utils.NumberUtils,
                net.gmsworld.server.config.Commons,
                net.gmsworld.server.utils.StringUtil,
                net.gmsworld.server.config.ConfigurationManager"%>
<%
    double latitude;
    if (request.getAttribute("lat") != null) {
		latitude = NumberUtils.getDouble(request.getAttribute("lat").toString(), 52.23);
    } else {
    	latitude = NumberUtils.getDouble(request.getParameter("lat"), 52.23);
    }
    double longitude;
    if (request.getAttribute("lng") != null) {
		longitude = NumberUtils.getDouble(request.getAttribute("lng").toString(), 21.02);
    } else {
    	longitude = NumberUtils.getDouble(request.getParameter("lng"), 21.02);
    }
	boolean isMobile = StringUtils.equals(request.getParameter("mobile"), "true");
	String landmarkDesc = null;
	if (request.getAttribute("landmarkDesc") != null) {
		landmarkDesc = request.getAttribute("landmarkDesc").toString();
	} else {
		landmarkDesc = "'<span style=\"font-family:Cursive;font-size:14px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">'+\n" +
                       "'<img src=\"/images/flagblue.png\"/><br/>' +\n" +
                       "'This is map center location: " + StringUtil.formatCoordE6(latitude) + "," + StringUtil.formatCoordE6(longitude) + "'";
 
	}
	String landmarkName = null;
	if (request.getAttribute("landmarkName") != null) {
		landmarkName = request.getAttribute("landmarkName").toString();
	} else {	
		landmarkName = "'Map center location: " + StringUtil.formatCoordE6(latitude) + "," + StringUtil.formatCoordE6(longitude) + "'";
	}
%>
<!DOCTYPE html>
<html>
  <head>
    <style>
      html, body, #map-canvas { margin: 0; padding: 0; height: 100%; }
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
    <script src="https://maps.googleapis.com/maps/api/js?libraries=visualization"></script>
    <script src="/js/markerclusterer.js"></script>
    <script>
      var mapcenter = new google.maps.LatLng(<%= latitude %>, <%= longitude %>);

      var map;

      var mc;

      var layer_counter = 0;

      var marker_counter = 1;

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

          var infowindow = new google.maps.InfoWindow(
          {
                content: <%= landmarkDesc %>
          });
          
          var flagmarker = new google.maps.Marker({
  				position: mapcenter,
  				map: map,
  				icon: '/images/flagblue.png',
  				title: <%= landmarkName %>,
  	  	  });

          google.maps.event.addListener(flagmarker, 'click', function() {
              infowindow.open(map, flagmarker);
          });
          
          var mcOptions = {gridSize: 50, maxZoom: 18};
          var markers = [flagmarker]; 
          mc = new MarkerClusterer(map, markers, mcOptions);                
      }

      function loadMarkers(results, image, ismobile) {
    	  	var markers = []; 
          	for (var i = 0; i < results.features.length; i++) {
          			var coords = results.features[i].geometry.coordinates;
          			var latLng = new google.maps.LatLng(coords[1],coords[0]);
          			var url = results.features[i].properties.url;
          			if (url == null || ismobile) {
						url = results.features[i].properties.mobile_url
                  	}
          			var marker = new google.maps.Marker({
           				position: latLng,
            			map: map,
            			title: results.features[i].properties.name,
            			icon: image,
            			url: url 
          			});
          			google.maps.event.addListener(marker, 'click', function() {
              			if (this.url != null) {
    						window.open(this.url);	
          				}
	  	  			});
          			markers.push(marker);	
        	}
          	if (markers.length > 0) {  
                    marker_counter += markers.length;
  				    mc.addMarkers(markers, true);
	  		}
      }

      window.layers_callback = function(results) {
           if (results.properties != null) {
          		var layer = results.properties.layer;
    	  		for (var i = 0; i < layers.length; i++) {
          			 if (layer == layers[i].name) {
                    		var image = '/images/layers/' + layers[i].icon; 
          	  				loadMarkers(results, image, <%= isMobile %>);
          	  				break;
          			 }	   
           		} 
      	   }
           layer_counter++;
		   console.log("Loaded " + mc.getTotalMarkers() + " markers from (" + layer_counter + "/" + layers.length + ") layers!");
		   if (layer_counter == layers.length && marker_counter > 1) {
				mc.repaint();
				//window.alert("Loaded " + marker_counter + " landmarks!");
				$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
                $("#status").html(marker_counter + " landmarks were loaded on the map!");
				$("#status").center().show().delay(3000).queue(function(n) {
					  $(this).hide(); n();
				});

				var centerControlDiv = document.createElement('div');
		        var centerControl = new CenterControl(centerControlDiv, map, mapcenter);

		        centerControlDiv.index = 1;
		        map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(centerControlDiv); 
		   }
      }

      function CenterControl(controlDiv, map, center) {

    	  // Set CSS for the control border
    	  var controlUI = document.createElement('div');
    	  controlUI.style.backgroundColor = '#fff';
    	  controlUI.style.border = '2px solid #fff';
    	  controlUI.style.borderRadius = '3px';
    	  controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
    	  controlUI.style.cursor = 'pointer';
    	  controlUI.style.marginBottom = '18px';
    	  controlUI.style.textAlign = 'center';
    	  controlUI.title = 'Click to recenter the map';
    	  controlDiv.appendChild(controlUI);

    	  // Set CSS for the control interior
    	  var controlText = document.createElement('div');
    	  controlText.style.color = 'rgb(25,25,25)';
    	  controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
    	  controlText.style.fontSize = '16px';
    	  controlText.style.lineHeight = '32px';
    	  controlText.style.paddingLeft = '4px';
    	  controlText.style.paddingRight = '4px';
    	  controlText.innerHTML = 'Center Map';
    	  controlUI.appendChild(controlText);

    	  // Setup the click event listeners: simply set the map to
    	  // Chicago
    	  google.maps.event.addDomListener(controlUI, 'click', function() {
    	    map.setCenter(center)
    	  });
      }    	      

      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body>
    <div id="map-canvas"></div>
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:16px;line-height:32px;padding-left:4px;padding-right:4px"></div>
  </body>
</html>