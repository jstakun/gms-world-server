<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
 String token = null;
 if (StringUtils.equals(request.getParameter("generatetoken"),"true")) {
	  token = com.jstakun.lm.server.config.ConfigurationManager.getParam(com.jstakun.lm.server.config.ConfigurationManager.GMS_WORLD_ACCESS_TOKEN, null);  
 } else {	
	  token = request.getHeader("X-GMS-Token") != null ? request.getHeader("X-GMS-Token") : request.getParameter("gmstoken");
 }	
 boolean hotelsMode = StringUtils.equals(request.getParameter("enabled"), "Hotels");
 String enabled = request.getParameter("enabled");
 if (enabled != null) {
	 enabled = "?enabled=" + enabled; 
 } else {
	 enabled = "";
 }
 String latitude = request.getParameter("latitude");
 String longitude = request.getParameter("longitude");
%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="robots" content="noindex,nofollow" />
	<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE"/>
	<meta HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
	<meta HTTP-EQUIV="EXPIRES" CONTENT="0"/>
	<title>Please wait for <%= hotelsMode ? "hotel" : "landmark" %> creation and layer loading...</title> <!-- //translate -->
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
<p>&nbsp;Please wait. I'm loading <%= hotelsMode ? "hotels" : "landmarks" %> nearby selected location...</p> <!-- //translate -->
</div>
<script>
(function() {
  var gmsAPI = "/services/browserLandmark?callback=landmark_callback";
  $.ajax({
  	dataType: "json",
  	url: gmsAPI,
  	data: {
  	    	latitude: "<%= latitude %>",
  	    	longitude: "<%= longitude %>"
  	},
    beforeSend: function( xhr ) {
        xhr.setRequestHeader("X-GMS-Token", "<%= token  %>");
        xhr.setRequestHeader("X-GMS-Scope", "lm");
        xhr.setRequestHeader("X-GMS-AppId", "10");
    }})
  	.done(function( data ) {
  	   		$( 'div' ).remove();   
  	  		$( 'body' ).append("<p>Redirecting to <a href=\"/showLandmark/" + data.id + "<%= enabled %>\">selected location page</a>...<br/>" +
  	  				           "If you won't be redirected automatically click the link above.</p>"); //translate
  	    	console.log("Created landmark: " + data.id);
  	    	window.location.replace('/showLandmark/' + data.id + '<%= enabled %>');
  	})
  	.error(function(jqXHR, textStatus, errorThrown){ /* assign handler */
  		    $( 'div' ).remove();   
  		    var errorMessage = "Error occured: " + errorThrown + "!"; //translate
	  		$( 'body' ).append("<p>" + errorMessage + "<br/><a href=\"<%= hotelsMode ? "/hotels" : "/landmarks" %>\">Please try again.</a></p>"); //translate
	  		console.log(errorMessage);
  	    	alert(errorMessage);
  	});
})();
</script>
</body>
</html>