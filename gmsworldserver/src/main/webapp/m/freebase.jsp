<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.gms.android.landmarks.old.ExtendedLandmark,
                com.jstakun.lm.server.utils.MathUtils" %>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Freebase Landmark Details</title>
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
    			ExtendedLandmark landmark = (ExtendedLandmark) request.getAttribute("landmark");
            	String mid = (String) request.getAttribute("mid");
            	if (landmark == null && mid == null) {
            %>
                <h3>No Freebase landmark selected.</h3>
            <% } else if (landmark == null) { %>
                <h3>This landmark coldn't be found at Freebase.</h3>
            <% } else { %>
                <h3><%= landmark.getName()%></h3>
                 
                <% if (landmark.getThumbnail() != null) 
                   {
                %>
                <a href="/freebaseView/<%= mid %>">
                 	<img src="<%= landmark.getThumbnail() %>"/>
                </a> 	
                <% 
                   } 
                %>        
                
               <p>
                <%= landmark.getDescription() %> 
               </p>
               
               <!-- a href="/freebaseView/<%= mid %>">
                   <img src="http://maps.google.com/maps/api/staticmap?center=<%= MathUtils.normalizeE6(landmark.getLatitudeE6()) %>,<%= MathUtils.normalizeE6(landmark.getLongitudeE6()) %>&zoom=9&size=146x146&sensor=false&markers=icon:http://gms-world.appspot.com/images/flagblue.png|<%= MathUtils.normalizeE6(landmark.getLatitudeE6()) %>,<%= MathUtils.normalizeE6(landmark.getLongitudeE6()) %>" alt="Landmark on Google Map" height="146" width="146"/><br/>
               </a--> 
             
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
