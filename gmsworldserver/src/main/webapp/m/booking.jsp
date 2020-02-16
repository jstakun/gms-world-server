<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.NumberUtils,
                               net.gmsworld.server.utils.persistence.GeocodeCache,
                               net.gmsworld.server.layers.GeocodeUtils,
                               org.apache.commons.lang.StringUtils"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>Book your stay. Select accommodation place on the map...</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
               <h3>Select accommodation place on the map</h3>
<% 
Double latitude = null, longitude = null;
String address = null;

if (request.getAttribute("geocodeCache") != null) {
	GeocodeCache gc = (GeocodeCache) request.getAttribute("geocodeCache");
   if (gc != null) {
   		latitude = gc.getLatitude();
        longitude = gc.getLongitude();
        address =  StringUtils.capitalize(gc.getLocation());
   }    
} else if (request.getParameter("lat") != null && request.getParameter("lng") != null) {
	address = request.getParameter("address");
	latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
	longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
} else if (request.getAttribute("lat") != null && request.getAttribute("lng") != null) {
	address = (String)request.getAttribute("address");
	latitude = GeocodeUtils.getLatitude((String)request.getAttribute("lat"));
	longitude = GeocodeUtils.getLongitude((String)request.getAttribute("lng"));
}
if (latitude != null && longitude != null) 
{
	if (address == null) {
		address = "Selected location"; 
	}
	int zoom = NumberUtils.getInt(request.getParameter("zoom"), 12);
%>

                <ins class="bookingaff" data-aid="864526" data-target_aid="864526" 
                        data-prod="map" data-width="100%" data-height="1000" 
                        data-lang="ualng" data-dest_id="0" data-dest_type="landmark" 
                        data-latitude="<%= latitude %>" 
                        data-longitude="<%= longitude %>" 
                        data-landmark_name="<%= address %>" data-mwhsb="0" 
                        data-address="<%= address %>" data-zoom="<%= zoom %>">
    					<!-- Anything inside will go away once widget is loaded. -->
        				<a href="//www.booking.com?aid=864526">Booking.com</a>
				</ins>
<% 
} else { 
%>
				<p><b>Please specify location!</b></p>
<% 
} 
%>
				<script type="text/javascript">
    				(function(d, sc, u) {
      				var s = d.createElement(sc), p = d.getElementsByTagName(sc)[0];
      				s.type = 'text/javascript';
      				s.async = true;
      				s.src = u + '?v=' + (+new Date());
      				p.parentNode.insertBefore(s,p);
      				})(document, 'script', '//aff.bstatic.com/static/affiliate_base/js/flexiproduct.js');
</script>
        
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
