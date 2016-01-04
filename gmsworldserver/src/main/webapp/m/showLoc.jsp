<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.utils.HtmlUtils" %>
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
    			Double lat = null, lon = null;    
            	try {    
					lat = Double.valueOf((String)request.getAttribute("lat"));
        			lon = Double.valueOf((String)request.getAttribute("lng"));
            	} catch (Exception e) {
            	
            	}
    			if (lat == null || lon == null) {
            %>
            	<h3>No location specified</h3>
            <%                    
                } else {      
            %>
                <h3>Selected location</h3>
                <h4>You've selected following location:</h4>

                <a href="/showLocation/<%= HtmlUtils.encodeDouble(lat) %>/<%= HtmlUtils.encodeDouble(lon) %>/fullScreen">
                	<img src="/image?lat=<%= lat%>&lng=<%= lon%>" alt="Location on Google Map" height="128" width="128"/><br/>
                </a>
                
                <p>
                   <a href="/showLocation/<%= HtmlUtils.encodeDouble(lat) %>/<%= HtmlUtils.encodeDouble(lon) %>/fullScreen">See full screen map</a><br/>
                   <%= request.getAttribute("address")!=null ? "Geocode address: " + request.getAttribute("address") : "" %><br/>
                   Latitude: <%= lat %>, Longitude: <%= lon %><br/>
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
