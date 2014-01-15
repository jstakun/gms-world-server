<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.Screenshot,
        com.jstakun.lm.server.utils.UrlUtils,
        com.jstakun.lm.server.utils.DateUtils,
        com.jstakun.lm.server.utils.StringUtil" %>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Screenshot Page</title>
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

  String imageUrl = (String)request.getAttribute("imageUrl");
  Screenshot screenshot = (Screenshot)request.getAttribute("screenshot");
  String address = (String) request.getAttribute("address");
  
  if (screenshot != null) { %>

                <h3>GMS World screenshot</h3>

                 <a href="/showLocation.do?lat=<%= screenshot.getLatitude() %>&lon=<%= screenshot.getLongitude() %>">
                    <img src="<%= imageUrl %>" alt="GMS World screenshot"/>
                 </a>
                 
                 <p>
                     <%=  address != null ? "Geocode address: " + address + "<br/>" : ""%>
                     Latitude: <%= StringUtil.formatCoordE6(screenshot.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(screenshot.getLongitude()) %><br/>
                     <div class="date"><span>Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), screenshot.getCreationDate()) %> | by 
    				 <% if (screenshot.getUsername() != null) { %>
                     	<a href="<%= response.encodeURL("/showUser/" + screenshot.getUsername()) %>"><%= UrlUtils.createUsernameMask(screenshot.getUsername()) %></a>
    				 <% } else { %>
                     	<%= UrlUtils.createUsernameMask(screenshot.getUsername()) %>
    				 <% } %>
                     </span></div>
                 </p>
<% } else { %>
                 <h3>Image not found</h3>
                 Image has been archived or you have provided wrong image key.
<% } %>
                 <br/>
        
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
