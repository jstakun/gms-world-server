<%@ page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="org.apache.commons.lang.StringUtils,
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
    <% if (hotelsMode) { %>
    <title><bean:message key="hotels.discover" /></title>
  	<%@ include file="/WEB-INF/jspf/hotelsonmap_header.jspf" %>
    <% } else { %>
  	<%@ include file="/WEB-INF/jspf/head_small.jspf" %>
  	<title><bean:message key="landmarks.discover" /></title>
  	<% } %>
	<style>
      html, body, #map-canvas { margin: 0; padding: 0; height: 100%; }
    </style>
    <link rel="stylesheet" href="/css/jquery-ui.min.css" />
  	<script src="/js/jquery.min.js"></script>
  	<script src="/js/jquery-ui.min.js"></script>
  	<script src="/js/datepicker-<%= request.getLocale().getLanguage() %>.js"></script>
    <script type="text/javascript">
    jQuery.fn.center = function () {
        this.css("position","absolute");
        this.css("top", ( $(window).height() - this.height() ) / 2+$(window).scrollTop() + "px");
        this.css("left", ( $(window).width() - this.width() ) / 2+$(window).scrollLeft() + "px");
        return this;
    }

    function isEmpty(str) {
	      return (!str || 0 == str.length);
	}

	function callHotelUrl(url) {
		//checkin_monthday=21&checkin_year_month=2016-4&checkout_monthday=23&checkout_year_month=2016-4
		var hotelUrlSuffix = "";
		if (document.getElementById("checkinNodate").checked == false && document.getElementById("checkinDate") != null && document.getElementById("checkoutDate") != null) {
        	var checkinDate = document.getElementById("checkinDate").value;
        	if (!isEmpty(checkinDate) && checkinDate.length == 10) {
       	 		hotelUrlSuffix = "&checkin_year_month=" + checkinDate.substring(0, 7) + "&checkin_monthday=" + checkinDate.substring(8);  
		     	Cookies.set('checkinDate', checkinDate, '{ expires: 2, path: '/'}');	
     		}
	  		var checkoutDate = document.getElementById("checkoutDate").value; 
        	if (!isEmpty(checkoutDate) && checkoutDate.length == 10) {
       	 		hotelUrlSuffix += "&checkout_year_month=" + checkoutDate.substring(0, 7) + "&checkout_monthday=" + checkoutDate.substring(8);  
	  		 	Cookies.set('checkoutDate', checkoutDate, '{ expires: 2, path: '/'}');	
        	} 	
		}

		//no_rooms=2&group_adults=2&group_children=2&age=5&age=9
		hotelUrlSuffix += "&no_rooms=" + document.getElementById("checkinRooms").value;
		hotelUrlSuffix += "&group_adults=" + document.getElementById("checkinAdults").value;
        var childrenCount = document.getElementById("checkinChildren").value;
        hotelUrlSuffix += "&group_children=" + childrenCount;  
		for (var i = 0; i < childrenCount; i++) {
			hotelUrlSuffix += "&age=" + document.getElementById("checkinChildren" + i + "Age").value;
        }
        
        console.log('Opening ' + url + hotelUrlSuffix + '...')
        window.open(url + hotelUrlSuffix, '_blank');
	}
    </script>
    <script src="https://maps.googleapis.com/maps/api/js?libraries=visualization"></script>
    <script src="/js/marker.js"></script>
    <script src="/js/markerclusterer.js"></script>
    <script src="/js/js.cookie.js"></script>
    <script>
      var map,
          mc,
          currencycode,
          eurexchangerate,
          flagmarker,
          sortType = '<%= request.getParameter("sortType") %>', 
          mapcenter = new google.maps.LatLng(<%= latitude %>, <%= longitude %>),
          hotelsOnly = true,
          layer_counter = 0,
 		  marker_counter = 1,
		  excluded_layers = 0,
		  infowindow = new google.maps.InfoWindow(),   
		  markers = [],
	      layers = [
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
                    script.src = '<%= ConfigurationManager.SERVER_URL %>geoJsonProvider?layer=' + layers[i].name + '&lat=<%= latitude %>&lng=<%= longitude %>&callback=layers_callback'; 
        			//script.src = 'http://localhost:8080/geoJsonProvider?layer=' + layers[i].name + '&lat=<%= latitude %>&lng=<%= longitude %>&callback=layers_callback'; 
        			if (layers[i].name == "Hotels") {
        				script.src += '&sortType=' + sortType;
            		}
        			document.getElementsByTagName('head')[0].appendChild(script);
              }	else {
                    excluded_layers++; 
              }
          }

          var contentString = <%= landmarkDesc %>;
          
          flagmarker = new google.maps.Marker({
  				position: mapcenter,
  				map: map,
  				desc: contentString,
  				icon: '/images/flagblue.png',
  				title: <%= landmarkName %>,
  	  	  });

          google.maps.event.addListener(flagmarker, 'click', function() {
        	    infowindow.setContent(this.desc);
              	infowindow.open(map, this);
          });

          var styleOpts = [
          	{textColor: 'white', url: '/images/markerclusterer/m1.png', textSize: 15, width: 53, height: 52},
          	{textColor: 'white', url: '/images/markerclusterer/m2.png', textSize: 15, width: 56, height: 55},
          	{textColor: 'white', url: '/images/markerclusterer/m3.png', textSize: 16, width: 66, height: 65},
          	{textColor: 'white', url: '/images/markerclusterer/m4.png', textSize: 17, width: 78, height: 77},
          	{textColor: 'white', url: '/images/markerclusterer/m5.png', textSize: 18, width: 90, height: 89} 
          ];
          var mcOptions = {gridSize: 50, maxZoom: 18, imagePath: '/images/markerclusterer', styles: styleOpts};
          //
          
          //markers.push(flagmarker); //keep always on top
          mc = new MarkerClusterer(map, markers, mcOptions);       

          <% if (hotelsMode) { %>
          var message = '<bean:message key="hotels.wait" />';
          <% } else { %>
          var message = '<bean:message key="landmarks.wait" />';
          document.getElementById('checkin').remove();
          <% } %>

          //scale
          $("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
          $("#status").html("<img src=\'/images/progress.gif\' style=\'width:16px; height:16px; vertical-align: middle;'><span style='line-height:<%=fontSize%>;'>&nbsp;" + message + "</span>");
		  $("#status").center().show();
      }

      function loadMarkers(results, image, ismobile) {
          currencycode = results.properties.currencycode;
          eurexchangerate = results.properties.eurexchangerate;
          
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
                var stars = 0;

                //only for hotels layer
                if (results.features[i].properties.icon != null) {
                    icon = results.features[i].properties.icon;
                    if (icon.indexOf('5') > -1) {
         				stars = 5;
         			} else if (icon.indexOf('4') > -1) {
         				stars = 4;
         			} else if (icon.indexOf('3') > -1) {
         				stars = 3;
         			} else if (icon.indexOf('2') > -1) {
         				stars = 2;
         			} else if (icon.indexOf('1') > -1) {
         				stars = 1;
         			}     
					icon = '/images/layers/' + icon;
                }

                var thumbnail = results.features[i].properties.thumbnail;
                  	
                var descr;

                if (desc != null) {                              
                     descr = '<span style=\"font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">' + 
                                '<strong>' + name + '</strong>';

                      if (thumbnail != null) {
							descr += '<br/><a href=\"javascript:callHotelUrl(\'' + url + '\')\">' +
							         '<img src=\"' + thumbnail +  '\" style=\"margin: 4px 0px\" title=\"<bean:message key="hotels.booking"/>\"/>' + 
							         '</a>';
                     }       

                     descr += '<br/>' + desc +
                              '<br/><a href=\"javascript:callHotelUrl(\'' + url + '\')\"><bean:message key="hotels.booking"/></a>' +   
                              '</span>'; 
                }
                  	 
                //my Marker
                var marker = new Marker({
           			 position: latLng,
            		 title: name,
            		 map: map,
            		 icon: icon,
            		 price: price,
            		 cc: currencycode,
            		 url: url, 
            		 desc: descr,
            		 stars: stars,
            		 mobile: <%= isMobile %>,
          		}); 

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
      	
          	if (results.features.length > 0) {  
                marker_counter += results.features.length;
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
                            mc.repaint();
          			 }	   
           		} 
      	   }
           layer_counter++;
		   console.log("Loaded markers from (" + layer_counter + "/" + layers.length + ") layers!");
		   if ((layer_counter + excluded_layers) == layers.length && marker_counter > 1) {
				mc.repaint();

				<% if (hotelsMode) { %>
		        var message = '<bean:message key="hotels.loaded" />';
		        <% } else { %>
		        var message = '<bean:message key="landmarks.loaded" />';
		        <% } %>
				
				$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
                $("#status").html((marker_counter-1) + " " + message);
				$("#status").center().show().delay(3000).queue(function(n) {
					  $(this).hide(); n();
				});

				var centerControlDiv = document.createElement('div');
				centerControlDiv.index = 1;
		        var centerControl = new CenterControl(centerControlDiv, 'center', '<bean:message key="landmarks.center.map" />', '<bean:message key="landmarks.center.map" />');
		        map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(centerControlDiv);
		        google.maps.event.addDomListener(centerControlDiv, 'click', function() {
		    	   	 map.setCenter(mapcenter)
		    	});

		        if (window.location.href.indexOf("?enabled=Hotels") == -1 && hotelsOnly == false) {
					var hotelControlDiv = document.createElement('div');
		        	hotelControlDiv.index = 2;
		        	var centerControl = new CenterControl(hotelControlDiv, 'center', '<img src=\'/images/hotel_search_64.png\' title=\'<bean:message key="hotels.discover.nearby" />\'/>', '<bean:message key="hotels.discover.nearby" />'); 
		        	map.controls[google.maps.ControlPosition.TOP_CENTER].push(hotelControlDiv);
		        	google.maps.event.addDomListener(hotelControlDiv, 'click', function() { 
		                window.location.href = window.location.pathname + '?enabled=Hotels';
		       	    });
		       } else {
			        //new search button
		        	var hotelControlDiv = document.createElement('div');
		        	hotelControlDiv.index = 2;
		        	var centerControl = new CenterControl(hotelControlDiv, 'center', '<b><bean:message key="landmarks.new.search" /></b>', '<bean:message key="landmarks.new.search" />'); 
		        	map.controls[google.maps.ControlPosition.TOP_LEFT].push(hotelControlDiv);
		        	google.maps.event.addDomListener(hotelControlDiv, 'click', function() { 
		                window.location.href = '/hotels/' + map.getCenter().lat() + '/' + map.getCenter().lng() + '/' + map.getZoom();
		       	    }); 

		       	    //legend
		        	var topLocationsDiv = document.createElement('div'); //scale
		        	var text = '<img src=\'/images/layers/0stars_blue_32.png\' style=\'width:32px; height:32px; vertical-align: middle;\' title=\'Single room or apartment venue\'><span style=\'line-height:32px;\'>&nbsp;<bean:message key="hotels.single.venue" /></span><br/>' +
			        		   '<img src=\'/images/layers/star_0_32.png\' style=\'width:32px; height:32px; vertical-align: middle;\'><span style=\'line-height:32px;\' title=\'Multiple rooms or apartments venue\'>&nbsp;<bean:message key="hotels.multiple.venue" /></span>'; 
		        	var topLocationsControl = new CenterControl(topLocationsDiv, 'left', text, '');
		     	    topLocationsDiv.index = 3
		     	    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(topLocationsDiv);	

		     	    //dates
		     	    var checkinDiv = document.getElementById('checkin');
		     	    checkinDiv.index = 4
		     	    map.controls[google.maps.ControlPosition.RIGHT_CENTER].push(checkinDiv);	
		     	     
		     	    //filters
		     	    var filtersDiv = document.createElement('div');
		     	    
		     	    var text = '<table style=\"width:100%;border-spacing: 0px;padding: 0px;font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;\">' + 
                               '<tr><td colspan=\"2\"><b><bean:message key="hotels.starrating" /></b></td></tr>' + 
			     	           '<tr><td><input type=\"checkbox\" id=\"5s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['5'] ? results.properties['stats_stars']['5'] : '0') + '</td></tr>' +
		     	               '<tr><td><input type=\"checkbox\" id=\"4s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['4'] ? results.properties['stats_stars']['4'] : '0') + '</td></tr>' +
		     	               '<tr><td><input type=\"checkbox\" id=\"3s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['3'] ? results.properties['stats_stars']['3'] : '0') + '</td></tr>' +
		     	               '<tr><td><input type=\"checkbox\" id=\"2s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['2'] ? results.properties['stats_stars']['2'] : '0') + '</td></tr>' +
		     	               '<tr><td><input type=\"checkbox\" id=\"1s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_blue.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['1'] ? results.properties['stats_stars']['1'] : '0') + '</td></tr>' +
		     	               '<tr><td><input type=\"checkbox\" id=\"0s\" checked=\"checked\" onclick=\"filter()\"/></td><td><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/><img src=\"/images/star_grey.png\" style=\"margin: 0px 2px\"/></td><td align=\'right\'>' + (results.properties['stats_stars']['0'] ? results.properties['stats_stars']['0'] : '0') + '</td></tr>';
                    if (eurexchangerate) {
                        var value = parseInt(eurexchangerate*50, 10);
                    	text += '<tr><td colspan=\"2\"><b><bean:message key="hotels.price" /></b</td></tr>' + 
                                '<tr><td><input type=\"checkbox\" id=\"1p\" checked=\"checked\" onclick=\"filter()\"/></td><td>0 ' + currencycode + ' - ' + value + ' ' + currencycode + '</td><td align=\'right\'>&nbsp;' + (results.properties['stats_price']['1'] ? results.properties['stats_price']['1'] : '0') + '</td></tr>' + 
                                '<tr><td><input type=\"checkbox\" id=\"2p\" checked=\"checked\" onclick=\"filter()\"/></td><td>' + value + ' ' + currencycode + ' - ' + (value*2) + ' ' + currencycode + '</td><td align=\'right\'>&nbsp;' + (results.properties['stats_price']['2'] ? results.properties['stats_price']['2'] : '0') + '</td></tr>' +
                                '<tr><td><input type=\"checkbox\" id=\"3p\" checked=\"checked\" onclick=\"filter()\"/></td><td>' + (value*2) + ' ' + currencycode + ' - ' + (value*3) + ' ' + currencycode + '</td><td align=\'right\'>&nbsp;' + (results.properties['stats_price']['3'] ? results.properties['stats_price']['3'] : '0') +'</td></tr>' +
                                '<tr><td><input type=\"checkbox\" id=\"4p\" checked=\"checked\" onclick=\"filter()\"/></td><td>' + (value*3) + ' ' + currencycode + ' - ' + (value*4) + ' ' + currencycode + '</td><td align=\'right\'>&nbsp;' + (results.properties['stats_price']['4'] ? results.properties['stats_price']['4'] : '0') +'</td></tr>' +
                                '<tr><td><input type=\"checkbox\" id=\"5p\" checked=\"checked\" onclick=\"filter()\"/></td><td>' + (value*4) + ' ' + currencycode + ' +</td><td align=\'right\'>&nbsp;' + (results.properties['stats_price']['5'] ? results.properties['stats_price']['5'] : '0') + '</td></tr>'; 
                    }
           		    text += '</table>'; 
		     	    var filtersControl = new CenterControl(filtersDiv, 'center', text, '');
		     	    filtersDiv.index = 5
		     	    map.controls[google.maps.ControlPosition.LEFT_CENTER].push(filtersDiv);
		     	    //	     	    

		     	    var filterStr = Cookies.get('filter');
		     	    if (filterStr) {
		     	    	console.log('filter cookie: ' + filterStr);
                        var cids = filterStr.split(',');
                        if (cids.length > 0) {
                            for (var i=0;i<cids.length;i++) {
                            	var cid = cids[i];
                            	if (cid.length > 0 && document.getElementById(cid)) {
                                	document.getElementById(cid).checked = false;
                            	}
                        	}     
		     	    		filter();
		     	    	} 	
			     	} else {
		     	    	console.log('no filter cookie set');
			     	}
			    }	 
			} else if ((layer_counter + excluded_layers) == layers.length && marker_counter == 1) {
				<% if (hotelsMode) { %>
		        var message = '<bean:message key="hotels.none" />';
		        <% } else { %>
		        var message = '<bean:message key="landmarks.none" />';
		        <% } %>
				$("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
                $("#status").html(message);
				$("#status").center().show().delay(3000).queue(function(n) {
					  $(this).hide(); n();
				});
			}
      }

      function CenterControl(controlDiv, align, text, title) {

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
          controlUI.title = title;
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
      
      function filter() {
    	  var markersToAdd = [];

          for (var i = 0; i < markers.length; i++) {
               var marker = markers[i];
                
               var stars = 0;
               if (marker.stars > 0) {
				   stars = marker.stars;
               }  

               var checkedStars = true;
               if (document.getElementById(stars + 's')) {
            	   checkedStars = document.getElementById(stars + 's').checked;
               }  

               var checkedPrice = true; 

               if (eurexchangerate && marker.price) {
               		var eurrate = (marker.price / eurexchangerate);
               		if (eurrate < 50) {  
            	   		checkedPrice = document.getElementById('1p').checked;
               		} else if (eurrate >= 50 && eurrate < 100) {  
            	   		checkedPrice = document.getElementById('2p').checked;
               		} else if (eurrate >= 100 && eurrate < 150) {  
            	   		checkedPrice = document.getElementById('3p').checked;
               		} else if (eurrate >= 150 && eurrate < 200) {  
            	   		checkedPrice = document.getElementById('4p').checked;
              	 	} else if (eurrate >= 200) {  
            	   		checkedPrice = document.getElementById('5p').checked; 
               		}    
               }

               if (checkedStars && checkedPrice) {
            	   markersToAdd.push(marker);		
               }               
          }     

          console.log(markersToAdd.length + ' markers changed.');
          var modified = false;
          if (mc.getTotalMarkers() > 0) { 
          	   mc.removeMarkers(markers);
          	   modified = true;
          }
    	  if (markersToAdd.length > 0) {
        	   mc.addMarkers(markersToAdd);
        	   modified = true;
          }
          if (modified) {
    	  	   mc.redraw();   
      	  }   

      	  var message =  mc.getTotalMarkers() + ' <bean:message key="hotels.filtered" />'; 
          
          $("#status").css({"background-color": "#fff", "border" : "2px solid #fff", "border-radius": "3px", "text-align": "center", "box-shadow" : "0 2px 6px rgba(0,0,0,.3)"});
          $("#status").html(message);
		  $("#status").center().show().delay(3000).queue(function(n) {
				  $(this).hide(); n();
		  });     

		  var filter = "";
		  var filterStr = Cookies.get('filter');
		  for (var i = 0; i < 6; i++) {
			 if (document.getElementById(i + 's').checked == false) {
                 filter += i + "s,"; 
             } 
             if (i > 0 && document.getElementById(i + 'p')) {
                 if (document.getElementById(i + 'p').checked == false) {
            	 	filter += i + 'p,';   
                 }
             } else if (i > 0 && filterStr && filterStr.indexOf(i + 'p,') >= 0) {
            	 filter += i + 'p,';  //rewrite existing filter in price checkboxes are hidden
             }
		  }
		  if (filter.length > 0) {
			 Cookies.set('filter', filter, '{ expires: 365, path: '/'}');			
	      }
      }


      var checkinChildrenAges = [<%= (String)request.getAttribute("checkinChildrenAges") %>];
      
      function setupChildrenAges() {
  		var count = document.getElementById("checkinChildren").value;
  		document.getElementById("checkinChildrenHeaderRow").innerHTML='';
  		document.getElementById("checkinChildrenRow1").innerHTML='';
  		document.getElementById("checkinChildrenRow2").innerHTML='';
  		document.getElementById("checkinChildrenRow3").innerHTML=''
  		if (count > 0) {
             //checkinChildrenHeaderRow
  			 document.getElementById("checkinChildrenHeaderRow").innerHTML='<td colspan=\"2\" align=\"left\"><bean:message key="landmarks.children.ages" /></td>';
  			 //checkinChildrenRow1,2,3
  			 var iter = 1;
  			 for (k = 0; k < count; k += 4) {
  				var rowCount = Math.min(count, iter*4);
  				var checkinChildrenRowText = '<td colspan=\"2\">'
  				for (i = (iter-1)*4; i < rowCount; i++) {
  					checkinChildrenRowText += addChildrenAgeRow(i);
  				}
  				checkinChildrenRowText += '</td>';
  				document.getElementById("checkinChildrenRow" + iter).innerHTML=checkinChildrenRowText;
  				iter++; 
  			 }
  		 } 
  	 }

  	 function addChildrenAgeRow(pos) {
  		var res = "<select id=\"checkinChildren" + pos + "Age\">\n";
  		var selected = checkinChildrenAges[pos];
  		for (var i = 0;i < 18;i++) {  	  		
  	  		if (i == selected) {
  	  			res += "<option value=\"" + i + "\" selected=\"selected\">" + i + "</option>\n"
  	  	  	} else {
  				res += "<option value=\"" + i + "\">" + i + "</option>\n"
  	  	  	}
  		}
  		res += "</select>\n";
  		return res;
  	 }

      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body onload="setupChildrenAges()">
    <div id="map-canvas"></div>                                            
    <div id="status" style="color:black;font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;line-height:32px;padding-left:4px;padding-right:4px"></div>
    <div id="checkin" style="background-color:#fff;border:2px solid #fff;border-radius:3px;box-shadow:0 2px 6px rgba(0,0,0,.3);color:black;font-family:Roboto,Arial,sans-serif;font-size:<%=fontSize%>;line-height:28px;padding-left:4px;padding-right:4px;margin-right:10px">
    	<table>
    		<tr>
    			<th colspan="2"><bean:message key="landmarks.checkin.dates" /></th>
    		</tr>
    		<tr>
    			<td><bean:message key="landmarks.checkin" /></td>
    			<td><input type="text" id="checkinDate" size="10" value="<%= StringUtils.length(request.getParameter("checkin")) == 10 ? request.getParameter("checkin") : "" %>"></td>
    		</tr>
    		<tr>
    			<td><bean:message key="landmarks.checkout" /></td>
    			<td><input type="text" id="checkoutDate" size="10" value="<%= StringUtils.length(request.getParameter("checkout")) == 10 ? request.getParameter("checkout") : "" %>"></td>
    		</tr>
    		<tr>
    			<td colspan="2"><input type="checkbox" id="checkinNodate" <%= StringUtils.length(request.getParameter("checkin")) != 10 ? "checked" : "" %>><bean:message key="landmarks.checkin.nodate" /></td>
    		</tr>
    		<tr>
    			<th colspan="2"><bean:message key="landmarks.guests" /></th>
    		</tr>
    		<tr>
    			<td><bean:message key="landmarks.adults" /></td>
    			<td align="right">
    				<select id="checkinAdults">
<%
	int selected = Integer.parseInt((String)request.getAttribute("checkinAdults"));
	for (int i=1;i<=30;i++) {
		if (i == selected) {
%>
  						<option value="<%= i%>" selected="selected"><%= i%></option>
<%
		} else {
%>			
						<option value="<%= i%>"><%= i%></option>
<%			
		}
	}
%>  						
					</select> 
				</td>
    		</tr>
    		<tr>
    			<td><bean:message key="landmarks.children" /></td>
    			<td align="right">
    				<select id="checkinChildren" onchange="setupChildrenAges()">
    					<option value="0">0</option>
<%
	selected = Integer.parseInt((String)request.getAttribute("checkinChildren"));
	for (int i=1;i<=10;i++) {
		if (i == selected) {
%>
  						<option value="<%= i%>" selected="selected"><%= i%></option>
<%
		} else {
%>			
						<option value="<%= i%>"><%= i%></option>
<%			
		}
	}
%>  						</select> 
				</td>
    		</tr>
    		
    		<!-- children age input rows -->
    		<tr id="checkinChildrenHeaderRow"></tr>
    		
    		<tr id="checkinChildrenRow1"></tr>
    		
    		<tr id="checkinChildrenRow2"></tr>
    		
    		<tr id="checkinChildrenRow3"></tr>		
    		<!--  -->
    		
    		<tr>
    			<td><bean:message key="landmarks.rooms" /></td>
    			<td align="right">
    				<select id="checkinRooms">
<%
	selected = Integer.parseInt((String)request.getAttribute("checkinRooms"));
	for (int i=1;i<=30;i++) {
		if (i == selected) {
%>
  						<option value="<%= i%>" selected="selected"><%= i%></option>
<%
		} else {
%>			
						<option value="<%= i%>"><%= i%></option>
<%			
		}
	}
%>  						
					</select> 
				</td>
    		</tr>
    	</table>
    </div>
    <script type="text/javascript">
      $(function() {
	     var daysToAdd = 1;
	     $.datepicker.setDefaults($.datepicker.regional['<%= request.getLocale().getLanguage() %>']);
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
      var checkinDate = Cookies.get('checkinDate');
      if (!isEmpty(checkinDate)) {
 		   document.getElementById('checkinDate').value = checkinDate;
      } else {
          var today = new Date();
    	  document.getElementById('checkinDate').value = today.toISOString().substring(0, 10); 
      }
      var checkoutDate = Cookies.get('checkoutDate');
      if (!isEmpty(checkoutDate)) {
 		   document.getElementById('checkoutDate').value = checkoutDate;
      } else {
    	  var tomorrow = new Date();
    	  tomorrow.setDate(tomorrow.getDate() + 1);  
    	  document.getElementById('checkoutDate').value = tomorrow.toISOString().substring(0, 10); 
      }
    </script>
  </body>
</html>