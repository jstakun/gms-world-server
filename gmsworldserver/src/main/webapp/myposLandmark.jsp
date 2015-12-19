<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>                 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
 <title><bean:message key="landmarks.mypos.title" /></title>
 <script>
 function getLocation() {
	var x = document.getElementById("status");
    if (navigator.geolocation) {
    	x.innerHTML = "<bean:message key="landmarks.mypos.wait" />";
        navigator.geolocation.getCurrentPosition(showPosition, errorCallback, {maximumAge: 60000, timeout: 30000});
    } else { 
    	x.innerHTML = "<bean:message key="errors.occured" />: geolocation is not supported by this browser!"; //translate
    }
 }

 function showPosition(position) {
	document.getElementById("status").innerHTML = "<bean:message key="landmarks.redirect" />";
	window.location.replace("/newLandmark/" + position.coords.latitude + "/" + position.coords.longitude);
 }

 function errorCallback(error) {
 	var x = document.getElementById("status");
	console.log("Error: " + error);
	switch(error.code) {
    	case error.PERMISSION_DENIED:
        	x.innerHTML = "<bean:message key="errors.occured" />: user denied the request for geolocation!"; //translate
        	break;
    	case error.POSITION_UNAVAILABLE:
        	x.innerHTML = "<bean:message key="errors.occured" />: location information is unavailable!"; //translate
        	break;
    	case error.TIMEOUT:
        	x.innerHTML = "<bean:message key="errors.occured" />: the request to get user location timed out!"; //translate
        	break;
    	case error.UNKNOWN_ERROR:
        	x.innerHTML = "<bean:message key="errors.occured" />: unknown!"; 
        	break;
	}
	window.location.replace('/landmarks');
 }
 </script>
</head>
<body onLoad="getLocation()">
<p id="status"></p>
</body>
</html>