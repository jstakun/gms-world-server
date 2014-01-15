<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
                 com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
                 com.jstakun.lm.server.utils.UrlUtils,
                 com.jstakun.lm.server.utils.StringUtil,
                 com.jstakun.lm.server.persistence.Landmark,
                 com.jstakun.lm.server.utils.DateUtils,
                 java.util.List"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Layer Landmarks</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	
        <%
           String layer = (String)request.getAttribute("layer");
           if (layer == null) {
               layer = "none";
           }   
        %>   	

        <h3>Landmarks in layer <%= LayerPersistenceUtils.getLayerFormattedName(layer) %></h3>

        <%
           List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("layerLandmarks");
   		   
           if (landmarkList == null || landmarkList.isEmpty())
           {
        %>
        	Found 0 landmarks in layer.
        <%
           }
           else
           {
                for (Landmark landmark : landmarkList)
                {
        %>
                    
        <article class="underline">
        
            <h4><a href="/showLandmark/<%= landmark.getKeyString() %>"><%= landmark.getName()%></a></h4>
            
            <p>
                Latitude: <%= StringUtil.formatCoordE6(landmark.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(landmark.getLongitude()) %> |
                Created in layer <a href="/showLayer/<%= landmark.getLayer() %>"><%= LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer()) %></a>
                 <div class="date"><span>Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate()) %> | by <a href="/showUser/<%= landmark.getUsername() %>"><%= UrlUtils.createUsernameMask(landmark.getUsername()) %></a></span></div>
            </p>
        
        </article>
           
        <% 
                } 
           }
        %>
        
        <jsp:include page="/WEB-INF/jspf/navigation_mobile.jsp">
             <jsp:param name="page" value="showLayer"/>
             <jsp:param name="layer" value="<%= layer %>"/>
        </jsp:include>        
            	
        <br/><br/>    	
            	
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
