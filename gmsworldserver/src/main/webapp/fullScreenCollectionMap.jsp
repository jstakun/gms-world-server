<%-- 
    Document   : fullScreenCollectionMap
    Created on : 2011-11-02, 18:02:09
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils,
        com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
        net.gmsworld.server.utils.persistence.Landmark,
        com.jstakun.lm.server.utils.HtmlUtils,    
        java.util.List,
        net.gmsworld.server.utils.DateUtils,
        net.gmsworld.server.utils.StringUtil,
        net.gmsworld.server.utils.UrlUtils,
        org.apache.commons.lang.StringEscapeUtils,
        net.gmsworld.server.config.Commons" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <%
    	List<Landmark> landmarkList = null;
                        Double centerLat = 0.0;
                        Double centerLon = 0.0;
                        String collectionAttributeName = "userLandmarks";
                        if (request.getAttribute("collectionAttributeName") != null) {
                           collectionAttributeName = (String) request.getAttribute("collectionAttributeName");
                        }
                        if (request.getAttribute(collectionAttributeName) != null) {
                            landmarkList = HtmlUtils.getList(Landmark.class, request, collectionAttributeName);
                        }
                        if (request.getAttribute("centerLat") != null) {
                            centerLat = (Double) request.getAttribute("centerLat");
                        }
                        if (request.getAttribute("centerLon") != null) {
                            centerLon = (Double) request.getAttribute("centerLon");
                        }
    %>
    <head>
        <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
        <title>GMS World landmarks on the map</title>
        <%
        	if (landmarkList != null) {
        %>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <style type="text/css">
            html, body {width: 100%; height: 100%}
            body {margin-top: 0px; margin-right: 0px; margin-left: 0px; margin-bottom: 0px}
        </style>
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=<%= Commons.getProperty(Commons.Property.GOOGLE_API_WEB_KEY) %>">
        </script>
        <script type="text/javascript">
            function initialize()
            {
                var latlng = new google.maps.LatLng(<%=centerLat%>,<%=centerLon%>);
                var myOptions =
                    {
                    zoom: 3,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    scaleControl: true,
                };

                //var image = '/images/flagblue.png';
                var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

                var bounds  = new google.maps.LatLngBounds();
                
                for (index in landmarks) {
                    var landmark = landmarks[index];
                    if (index == 0) {
                    	setMarker(map, landmark, '/images/flagred.png', bounds); 
                    } else {  
                    	setMarker(map, landmark,  '/images/flagblue.png', bounds);
                    }
                }
                
                map.fitBounds(bounds);      

                map.panToBounds(bounds);   
            }

            var landmarks = [
            <%for (int i = 0; i < landmarkList.size(); i++) {
                     Landmark landmark = landmarkList.get(i);%>
                    ['<%=StringEscapeUtils.escapeJavaScript(landmark.getName())%>', <%=landmark.getLatitude()%>, <%=landmark.getLongitude()%>,
                        '<span style="font-family:Cursive;font-size:14px;font-style:normal;font-weight:normal;text-decoration:none;text-transform:none;color:000000;background-color:ffffff;">' +
                            '<img src="/images/flag<% if (i == 0)  out.print("red"); else out.print("blue"); %>.png"/><br/>' +
                            'Name: <%=StringEscapeUtils.escapeJavaScript(landmark.getName())%>,<br/>' +
                            'Description: <%=StringEscapeUtils.escapeJavaScript(landmark.getDescription())%>,<br/>' +
                            'Latitude: <%=StringUtil.formatCoordE6(landmark.getLatitude())%>, Longitude: <%=StringUtil.formatCoordE6(landmark.getLongitude())%>,<br/>' +
                            'Posted on <%=DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate())%> by <%=UrlUtils.createUsernameMask(landmark.getUsername())%>,<br/>' +
                            'Created in layer <%= LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) %>.</span>']
            <%
                     if (i < landmarkList.size() - 1) {
                         out.println(",");
                     }
                 }
            %>
                ];

                function setMarker(map, landmark, image, bounds) {
                    var myLatLng = new google.maps.LatLng(landmark[1], landmark[2]);

                    bounds.extend(myLatLng);

                    var infowindow = new google.maps.InfoWindow(
                    {
                        content: landmark[3]
                    });

                    var marker = new google.maps.Marker(
                    {
                        position: myLatLng,
                        title: landmark[0],
                        map: map,
                        icon: image
                    });

                    google.maps.event.addListener(marker, 'click', function() {
                        infowindow.open(map, marker);
                    });
                }
        </script>
        <% }%>
    </head>
    <body onLoad="initialize()">
        <% if (landmarkList != null) {%>
        <div id="map_canvas" style="width:100%; height:100%"></div>
        <script type="text/javascript">
            //<![CDATA[

            var map;
            if (GBrowserIsCompatible()) {

                // Monitor the window resize event and let the map know when it occurs
                if (window.attachEvent) {
                    window.attachEvent("onresize", function() {this.map.onResize()} );
                } else {
                    window.addEventListener("resize", function() {this.map.onResize()} , false);
                }
            }

            //]]>
        </script>
        <% } else {%>
        No landmark list selected
        <% }%>
    </body>
</html>
