<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils,
         net.gmsworld.server.utils.persistence.Landmark,
         com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
         org.ocpsoft.prettytime.PrettyTime,
         net.gmsworld.server.utils.UrlUtils,
         net.gmsworld.server.utils.DateUtils,
         com.jstakun.lm.server.utils.HtmlUtils,
         com.jstakun.lm.server.utils.memcache.CacheUtil,
         com.jstakun.lm.server.config.ConfigurationManager,
         java.util.List"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - discover interesting places anywhere!</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article class="underline">
    	<h3>Welcome to GMS World</h3>
    	</article>
    	
        <article class="underline">
        	<a href="<%= ConfigurationManager.HOTELS_URL %>">Select location on the map and discover hotels around</a>.
        </article>
        
        <article class="underline">
        	Don't know where to go? <a href="<%= HtmlUtils.getRandomUrl(pageContext.getServletContext()) %>">Let us choose for you!</a>
        </article>
        
        <article class="underline">
    		Discover interesting places anywhere with <a href="/download.jsp"><b>Landmark Manager</b></a>.
    	</article>
    		
    	<article class="underline">
        	<a href="/share">Share your location</a> or <a href="/landmarks">select location on the map</a> and discover landmarks nearby.
        </article>
        
        <article class="underline">	
        	Find out where <a href="/heatMap">GMS World</a> is most popular.     
        </article>
        
        <article>
        <h3>Newest Landmarks</h3>
        </article>
        <%
        	List<Landmark> landmarkList = HtmlUtils.getList(Landmark.class, request, "newestLandmarkList");
            if (landmarkList != null) {
                 for (Landmark landmark : landmarkList) {
        %>
        <article class="underline">
			<h4><a href="<%=response.encodeURL("/showLandmark/" + landmark.getId() + "/" + HtmlUtils.encodeDouble(landmark.getLatitude()) + "/" + HtmlUtils.encodeDouble(landmark.getLongitude())) %>"><%=landmark.getName()%></a></h4>
            <a href="<%=response.encodeURL("/showLandmark/" + landmark.getId() + "/" + HtmlUtils.encodeDouble(landmark.getLatitude()) + "/" + HtmlUtils.encodeDouble(landmark.getLongitude()))%>"><img src="/image?lat=<%=landmark.getLatitude()%>&lng=<%=landmark.getLongitude()%>" alt="Landmark on the map" /></a>                           
			<p>
				<%= HtmlUtils.getLandmarkDesc(landmark, request.getLocale()) %>
			</p>
        </article>        
        <%
            	}
           }
        %>
    	<br/><br/>
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
