<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="robots" content="noindex,nofollow" />
<title>Please wait for landmark creation and layer loading...</title>
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
<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.2.min.js"></script>
<script type="text/javascript">
$(window).load(function() {
	$("loader").fadeOut("slow");
})
</script>
</head>
<body>
<div class="loader"></div>
<script>
(function() {
  var gmsAPI = "/admin/browserLandmark?callback=landmark_callback";
  $.getJSON( gmsAPI, {
    	latitude: "<%= request.getParameter("latitude") %>",
    	longitude: "<%= request.getParameter("longitude") %>"
  })
  .done(function( data ) {
		$( 'div' ).remove();  
		$( 'body' ).append("Redirecting to <a href=\"/showLandmark/" + data.id + "\">landmark page</a>...<br/>" +
				           "If you won't be redirected automatically click the link above.");
    	console.log("created landmark: " + data.id);
    	window.location.href = '/showLandmark/' + data.id;
  })
  .error(function(jqXHR, textStatus, errorThrown){ /* assign handler */
    	console.log("error: " + errorThrown);
    	alert("Internal Error Occurred!");
  });
})();
</script>
</body>
</html>