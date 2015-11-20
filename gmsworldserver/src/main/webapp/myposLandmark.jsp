<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE"/>
 <meta HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
 <meta HTTP-EQUIV="EXPIRES" CONTENT="0"/>
 <title>Create landmark based on your location</title>
 <script>
 function getLocation() {
	var x = document.getElementById("status");
    if (navigator.geolocation) {
    	x.innerHTML = "Please wait. I'm reading now your geolocation...";
        navigator.geolocation.getCurrentPosition(showPosition, errorCallback, {maximumAge: 60000, timeout: 30000});
    } else { 
    	x.innerHTML = "Geolocation is not supported by this browser!";
    }
 }

 function showPosition(position) {
	document.getElementById("status").innerHTML = "Redirecting to landmark page...";
	//window.location.href = "/newLandmark/" + position.coords.latitude + "/" + position.coords.longitude; 	
	window.location.replace("/newLandmark/" + position.coords.latitude + "/" + position.coords.longitude);
 }

 function errorCallback(error) {
 	var x = document.getElementById("status");
	console.log("Error: " + error);
	switch(error.code) {
    	case error.PERMISSION_DENIED:
        	x.innerHTML = "User denied the request for geolocation!"
        	break;
    	case error.POSITION_UNAVAILABLE:
        	x.innerHTML = "Location information is unavailable!"
        	break;
    	case error.TIMEOUT:
        	x.innerHTML = "The request to get user location timed out!"
        	break;
    	case error.UNKNOWN_ERROR:
        	x.innerHTML = "An unknown error occurred!"
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