<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Selected Location</title>
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
    			String lat = (String) request.getAttribute("lat");
            	String lon = (String) request.getAttribute("lon");
    		    if (lat == null || lon == null) {
            %>
            	<h3>No location specified</h3>
            <%                    
                } else {      
            %>
                <h3>Your location</h3>
                <h4>You've selected following location:</h4>

                <img src="http://maps.google.com/maps/api/staticmap?center=<%= lat%>,<%= lon%>&zoom=9&size=146x146&sensor=false&markers=icon:http://gms-world.appspot.com/images/flagblue.png|<%= lat %>,<%= lon %>" alt="Landmark on Google Map" height="146" width="146"/><br/>
                
                <p>
                   <%= request.getAttribute("address")!=null ? "Geocode address: " + request.getAttribute("address") : "" %><br/>
                   Latitude: <%= lat %>, Longitude: <%= lon %><br/>
                </p>
             <%
                }
             %>
        
    	</article>
    	
    	<jsp:include page="/WEB-INF/jspf/ad_small_baner.jspf"/>
    </div>
    <footer>
    	<p>&copy; GMS World 2010-14. Design by <a href="http://mobifreaks.com" target="_blank">Mobifreaks</a></p>
    </footer>
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
