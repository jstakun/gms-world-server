<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.persistence.GeocodeCache,
                net.gmsworld.server.utils.StringUtil,
                org.ocpsoft.prettytime.PrettyTime,
                net.gmsworld.server.utils.DateUtils" %>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Geocode location on the map</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
    	<% 
    	    GeocodeCache gc = (GeocodeCache) request.getAttribute("geocodeCache");
    	    String key = request.getParameter("key");
    	   
    	    if (gc == null) {
        %>
           <h3>No geocode selected</h3>
        <%
            } else {
            	PrettyTime prettyTime = new PrettyTime(request.getLocale()); 	
        %>
           <h3>Geocode location for: <%= gc.getLocation() %></h3>

           <a href="/showGeocode/<%= key %>/fullScreen">
            <img src="/image?lat=<%= gc.getLatitude() %>&lng=<%= gc.getLongitude() %>" alt="Geocode on Google Map" height="128" width="128"/><br/>
           </a>
                                      
           <p>
             <a href="/showGeocode/<%= key %>/fullScreen">See full screen map</a><br/>
             Latitude: <%= StringUtil.formatCoordE6(gc.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(gc.getLongitude()) %><br/>
             <div class="date"><span>Posted <%= prettyTime.format(gc.getCreationDate()) %></span></div>
           </p>

        <%
            }
        %>
        
    	</article>
    	
    	<%@ include file="/WEB-INF/jspf/ad_small_baner.jspf" %>
    </div>
    <%@ include file="/WEB-INF/jspf/footer_mobile.jspf" %>
  </div>
 
  <script src="/js/jquery.min.js"></script>
  <script type="text/javascript">
    window.addEventListener("load",function() {
	  // Set a timeout...
	  setTimeout(function(){
	    // Hide the address bar!
	    window.scrollTo(0, 1);
	  }, 0);
	});
    $('.search-box,.menu' ).hide();   
    $('.options li:first-child').click(function(){	
   		$(this).toggleClass('active'); 	
   		$('.search-box').toggle();        			
   		$('.menu').hide();  		
   		$('.options li:last-child').removeClass('active'); 
    });
    $('.options li:last-child').click(function(){
   		$(this).toggleClass('active');      			
   		$('.menu').toggle();  		
   		$('.search-box').hide(); 
   		$('.options li:first-child').removeClass('active'); 		
    });
    $('.content').click(function(){
   		$('.search-box,.menu' ).hide();   
   		$('.options li:last-child, .options li:first-child').removeClass('active');
    });
  </script>
</body>

</html>
