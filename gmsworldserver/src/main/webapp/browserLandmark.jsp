<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang.StringUtils,
                                      com.jstakun.lm.server.utils.HtmlUtils,
                                      net.gmsworld.server.config.Commons" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>                 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
 String token = null;
 if (StringUtils.equals(request.getParameter("generatetoken"),"true")) {
	  token = com.jstakun.lm.server.config.ConfigurationManager.getParam(com.jstakun.lm.server.config.ConfigurationManager.GMS_WORLD_ACCESS_TOKEN, null);  
 } else {	
	  token = request.getHeader(Commons.TOKEN_HEADER) != null ? request.getHeader(Commons.TOKEN_HEADER) : request.getParameter("gmstoken");
 }	
 boolean hotelsMode = StringUtils.equals(request.getParameter("enabled"), "Hotels");
 String enabled = request.getParameter("enabled");
 if (enabled != null) {
	 enabled = "?enabled=" + enabled; 
 } else {
	 enabled = "";
 }
 
 Double latitude = HtmlUtils.decodeDouble(request.getParameter("latitudeEnc"));
 if (latitude == null) {
 	latitude = Double.parseDouble(request.getParameter("latitude"));
 }
 
 Double longitude = HtmlUtils.decodeDouble(request.getParameter("longitudeEnc"));
 if (longitude == null) {
 	longitude = Double.parseDouble(request.getParameter("longitude"));
 }
%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="robots" content="noindex,nofollow" />
	<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="no-cache, no-store, must-revalidate"/>
	<meta HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
	<meta HTTP-EQUIV="EXPIRES" CONTENT="0"/>
<% if (hotelsMode) { %>	
	<title><bean:message key="hotels.wait" /></title>
<% } else { %>
    <title><bean:message key="landmarks.wait" /></title>
<% } %>
	<style type="text/css">
	.loader {
		position: fixed;
		left: 0px;
		top: 0px;
		width: 100%;
		height: 100%;
		z-index: 9999;
		background: url('/images/loading_spinner.gif') 50% 50% no-repeat rgb(249,249,249);
	}
	</style>
	<script type="text/javascript" src="/js/jquery.min.js"></script>
	<script type="text/javascript">
	$(window).load(function() {
		$("loader").fadeOut("slow");
	})
	</script>
</head>
<body>
 <div class="loader">
	<% if (hotelsMode) { %>	
	<p>&nbsp;<bean:message key="hotels.wait" /></p> 
	<% } else { %>
    <p>&nbsp;<bean:message key="landmarks.wait" /></p>
	<% } %>
 </div>
 <script>
	(function() {
   		var gmsAPI = "/services/browserLandmark?callback=landmarkCallback&hotelsMode=<%= hotelsMode %>";
   		$.ajaxSetup({
   		    timeout: 60000 //Time in milliseconds
   		});
   		$.ajax({
  			dataType: "json",
  			url: gmsAPI,
  			data: {
  	    		latitude: "<%= latitude %>",
  	    		longitude: "<%= longitude %>"
  			},
    		beforeSend: function( xhr ) {
        		xhr.setRequestHeader("<%= Commons.TOKEN_HEADER %>", "<%= token  %>");
        		xhr.setRequestHeader("<%= Commons.SCOPE_HEADER %>", "lm");
        		xhr.setRequestHeader("<%= Commons.APP_HEADER %>", "10");
    		}})
  		.done(function( data ) {
  	  		var link = "/showLandmark/" + data.id + "<%= enabled %>";
  	  		var message = '<bean:message key="landmarks.redirect.prefix"/>' + link + '<bean:message key="landmarks.redirect.suffix"/>';
  	   		$( 'div' ).remove();   
  	  		$( 'body' ).append(message); 
  	    	console.log("Created landmark: " + data.id);
  	    	window.location.replace(link);
  		})
  		.error(function(jqXHR, textStatus, errorThrown){ /* assign handler */
  		    $( 'div' ).remove();   
  		    var errorMessage = "<bean:message key="errors.occured"/>: " + errorThrown + "!"; //translate
	  		$( 'body' ).append("<p>" + errorMessage + "<br/><a href=\"<%= hotelsMode ? "/hotels" : "/landmarks" %><%= "?lat=" + latitude + "&lng=" + longitude + "&zoom=7"%>\"><bean:message key="landmarks.try.again"/></a></p>"); 
	  		console.log(errorMessage);
  	    	alert(errorMessage);
  		});
	})();
  </script>
 </body>
</html>