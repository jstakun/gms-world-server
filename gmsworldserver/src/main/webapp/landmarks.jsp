<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="org.apache.commons.lang.StringUtils,
                net.gmsworld.server.utils.NumberUtils,
                net.gmsworld.server.config.Commons,
                net.gmsworld.server.utils.StringUtil,
                net.gmsworld.server.config.ConfigurationManager"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>                                
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
                       "'" + StringUtil.formatCoordE6(latitude) + "," + StringUtil.formatCoordE6(longitude) + "'"; 
	}
	
	String landmarkName = null;
	if (request.getAttribute("landmarkName") != null) {
		landmarkName = request.getAttribute("landmarkName").toString();
	} else {	
		landmarkName = "'" + StringUtil.formatCoordE6(latitude) + "," + StringUtil.formatCoordE6(longitude) + "'";
	}
	
	String enabled = request.getParameter("enabled");
    
	String disabled = request.getParameter("disabled");
    
	boolean hotelsMode = StringUtils.equals(enabled, "Hotels");

	String fontSize = "16px";
	if (isMobile) {
    	fontSize = "24px";
	}
%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <% if (hotelsMode) { %>
    <title><bean:message key="hotels.discover" /></title>
  	<% } else { %>
  	<title><bean:message key="landmarks.discover" /></title>
  	<% } %>
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
    <script src="/js/marker.js"></script>
    <script src="/js/markerclusterer.js"></script>
    <script>
      var mapcenter = new google.maps.LatLng(<%= latitude %>, <%= longitude %>);

      var hotelsOnly = true; 

      var map;

      var mc;

      var layer_counter = 0;

      var marker_counter = 1;

      var excluded_layers = 0;

      var infowindow = new google.maps.InfoWindow();   

      var layers = [
          
<%
      for (String layer : Commons.getLayers()) {
%>
{"name": "<%= layer %>", "icon" : "<%= com.jstakun.lm.server.config.ConfigurationManager.getLayerIcon(layer) %>", "enabled" : "<%= (StringUtils.containsIgnoreCase(enabled, layer) || (disabled != null && !StringUtils.containsIgnoreCase(disabled, layer)) || (disabled == null && enabled == null)) %>"},
<%     
      }
%>          
      ];

      function initialize() {
    	  map = new google.maps.Map(document.getElementById('map-canvas'), {
      			zoom: 12,
        		center: mapcenter,
        		mapTypeId: google.maps.MapTypeId.ROADMAP,  //TERRAIN, SATELLITE, HYBRID
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
          });

          for (var i = 0; i < layers.length; i++) {
              if (layers[i].enabled == "true") {
                	var script = document.createElement('script');
        			script.src = '<%= ConfigurationManager.SERVER_URL %>geoJsonProvider?lat=<%= latitude %>&lng=<%= longitude %>&layer=' + layers[i].name + '&callback=layers_callback'; 
        			document.getElementsByTagName('head')[0].appendChild(script);
              }	else {
                    excluded_layers++; 
              }
          }

          var contentString = <%= landmarkDesc %>;
          
          var flagmarker = new google.maps.Marker({
  				position: mapcenter,
  				map: map,
  				icon: '/images/flagblue.png',
  				title: <%= landmarkName %>,
  	  	  });

          google.maps.event.addListener(flagmarker, 'click', function() {
        	    infowindow.setContent(contentString);
              	infowindow.open(map, flagmarker);
          });

          var mcOptions = {gridSize: 50, maxZoom: 18};
          var markers = [flagmarker]; 
          mc = new MarkerClusterer(map, markers, mcOptions);       

          <% if (hotelsMode) { %>
          var message = '<bean:message key="hotels.wait" />';
          <% } else { %>
          var message = '<bean:message key="landmarks.wait" />';
          <% } %>

          //scale
          $("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
          $("#status").html("<img src=\'/images/progress.gif\' style=\'width:16px; height:16px; vertical-align: middle;'><span style='line-height:<%=fontSize%>;'>&nbsp;" + message + "</span>");
		  $("#status").center().show();
      }

      function loadMarkers(results, image, ismobile) {
    	  	var markers = []; 
          	for (var i = 0; i < results.features.length; i++) {
          			var coords = results.features[i].geometry.coordinates;
          			var latLng = new google.maps.LatLng(coords[1],coords[0]);
          			var url = results.features[i].properties.url;
          			var desc = results.features[i].properties.desc;
                    var name = results.features[i].properties.name;
                    var price = results.features[i].properties.price;

          			if (url == null || ismobile) {
						url = results.features[i].properties.mobile_url
                  	}

                  	var icon = image;
                  	if (results.features[i].properties.icon != null) {
						icon = '/images/layers/' + results.features[i].properties.icon;
                    }

          			if (desc != null) { 
                        desc = '<span style=\"font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">' + 
                               '<strong>' + name + '</strong><br/>' + desc + '</span>';
                  	}
                  	 
                  	//my Marker
                  	var marker = new Marker({
           				position: latLng,
            			title: name,
            			map: map,
            			icon: icon,
            			text: price,
            			url: url, 
            			desc: desc,
            			mobile: <%= isMobile %>,
          			}); 

                  	//google.maps.event.addListener(marker, 'mouseover', function() {
                  	//	if (this.desc != null) {
              		//		infowindow.setContent(this.desc);
                    //    	infowindow.open(map, this);
                    //    } 
          			//});	

          			//google.maps.event.addListener(marker, 'mouseout', function() {
          		    //		infowindow.close(); 
              		//});
              		
          			google.maps.event.addListener(marker, 'click', function() {
                        //map.setCenter(marker.getPosition());
          				//map.panTo(marker.getPosition());
          				if (this.desc != null) {
          					infowindow.setContent(this.desc);
                        	infowindow.open(map, this);
          				} else if (this.url != null) {
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
          			 if (layer == layers[i].name && layers[i].enabled == "true") {
                    		var image = '/images/layers/' + layers[i].icon; 
                    		console.log("Received " + results.features.length + " landmarks from layer " + layer);
          	  				loadMarkers(results, image, <%= isMobile %>);
                            if (layer != "Hotels") {
								hotelsOnly = false;
                            }
                            //$("#status").hide();
                            mc.repaint();
          			 }	   
           		} 
      	   }
           layer_counter++;
		   console.log("Loaded " + mc.getTotalMarkers() + " markers from (" + layer_counter + "/" + layers.length + ") layers!");
		   if ((layer_counter + excluded_layers) == layers.length && marker_counter > 1) {
				mc.repaint();

				<% if (hotelsMode) { %>
		        var message = '<bean:message key="hotels.loaded" />';
		        <% } else { %>
		        var message = '<bean:message key="landmarks.loaded" />';
		        <% } %>
				
				$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
                $("#status").html(marker_counter + " " + message);
				$("#status").center().show().delay(3000).queue(function(n) {
					  $(this).hide(); n();
				});

				var centerControlDiv = document.createElement('div');
				centerControlDiv.index = 1;
		        var centerControl = new CenterControl(centerControlDiv, 'center', '<bean:message key="landmarks.center.map" />');
		        map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(centerControlDiv);
		        google.maps.event.addDomListener(centerControlDiv, 'click', function() {
		    	   	 map.setCenter(mapcenter)
		    	});

		        if (window.location.href.indexOf("?enabled=Hotels") == -1 && hotelsOnly == false) {
					var hotelControlDiv = document.createElement('div');
		        	hotelControlDiv.index = 2;
		        	var centerControl = new CenterControl(hotelControlDiv, 'center', '<img src=\'/images/hotel_search.png\' title=\'<bean:message key="hotels.discover.nearby" />\'/>'); 
		        	map.controls[google.maps.ControlPosition.TOP_CENTER].push(hotelControlDiv);
		        	google.maps.event.addDomListener(hotelControlDiv, 'click', function() { 
		                window.location.href = window.location.href + '?enabled=Hotels';
		       	    });
		        } else {
			        //new search button
		        	var hotelControlDiv = document.createElement('div');
		        	hotelControlDiv.index = 2;
		        	var centerControl = new CenterControl(hotelControlDiv, 'center', '<bean:message key="landmarks.new.search" />'); 
		        	map.controls[google.maps.ControlPosition.TOP_CENTER].push(hotelControlDiv);
		        	google.maps.event.addDomListener(hotelControlDiv, 'click', function() { 
		                window.location.href = '/hotels/' + map.getCenter().lat() + '/' + map.getCenter().lng() + '/' + map.getZoom();
		       	    }); 

		       	    //legend
		        	var topLocationsDiv = document.createElement('div'); //scale
		        	var text = '<img src=\'/images/layers/0stars_blue_32.png\' style=\'width:32px; height:32px; vertical-align: middle;\' title=\'Single room or apartment venue\'><span style=\'line-height:32px;\'>&nbsp;<bean:message key="hotels.single.venue" /></span><br/>' +
			        		   '<img src=\'/images/layers/star_0_32.png\' style=\'width:32px; height:32px; vertical-align: middle;\'><span style=\'line-height:32px;\' title=\'Multiple rooms or apartments venue\'>&nbsp;<bean:message key="hotels.multiple.venue" /></span>'; 
		        	var topLocationsControl = new CenterControl(topLocationsDiv, 'left', text);
		     	    topLocationsDiv.index = 3
		     	    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(topLocationsDiv);	
			    }	 
			} else if ((layer_counter + excluded_layers) == layers.length && marker_counter == 1) {
				<% if (hotelsMode) { %>
		        var message = '<bean:message key="hotels.none" />';
		        <% } else { %>
		        var message = '<bean:message key="landmarks.none" />';
		        <% } %>
				$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
                $("#status").html("Oops. No landmarks available!");
				$("#status").center().show().delay(3000).queue(function(n) {
					  $(this).hide(); n();
				});
			}
      }

      function CenterControl(controlDiv, align, text) {

          // Set CSS for the control border
          var controlUI = document.createElement('div');
          controlUI.style.backgroundColor = '#fff';
          controlUI.style.border = '2px solid #fff';
          controlUI.style.borderRadius = '3px';
          controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
          controlUI.style.cursor = 'pointer';
          controlUI.style.marginTop = '10px';
          controlUI.style.marginLeft = '10px';
          controlUI.style.marginBottom = '10px';
          controlUI.style.marginRight = '10px';
          controlUI.style.textAlign = align; 
          controlUI.title = text;
          controlDiv.appendChild(controlUI);

          // Set CSS for the control interior
          var controlText = document.createElement('div');
          controlText.style.color = 'rgb(25,25,25)';
          controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
          controlText.style.fontSize = '<%=fontSize%>';
          controlText.style.lineHeight = '32px'; //scale
          controlText.style.paddingLeft = '4px';
          controlText.style.paddingRight = '4px';
          controlText.innerHTML = text;
          controlUI.appendChild(controlText);
      }  
      
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body>
    <div id="map-canvas"></div>                                             <!--         scale           -->
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;line-height:32px;padding-left:4px;padding-right:4px"></div>
  </body>
</html>