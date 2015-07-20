<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create landmark based on your location</title>
<script>
var token = "<%= request.getHeader("X-GMS-Token") != null ? request.getHeader("X-GMS-Token") : request.getParameter("gmstoken")  %>";

function getLocation() {
    if (token == "null") {
    	document.getElementById("status").innerHTML = "Token is required to proceed with your request!";
    } else if (navigator.geolocation) {
    	document.getElementById("status").innerHTML = "Reading your geolocation...";
        navigator.geolocation.getCurrentPosition(showPosition, errorCallback);
    } else { 
    	document.getElementById("status").innerHTML = "Geolocation is not supported by this browser!";
    }
}

function showPosition(position) {
	document.getElementById("status").innerHTML = "Redirecting to landmark page...";
	window.location.href = "/newLandmark/" + position.coords.latitude + "/" + position.coords.longitude + "/" + token; 	
}

function errorCallback(error) {
	document.getElementById("status").innerHTML = "Failed to read your location!";
	console.log("Error: " + error)
}
</script>
</head>
<body onLoad="getLocation()">
<p id="status"></p>
</body>
</html>