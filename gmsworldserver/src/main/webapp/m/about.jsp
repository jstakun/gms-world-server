<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - About Us</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    
     <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>     
           
    <div class="content">
    	<article>
    	
    	<h3>About Us</h3>

                    <p>
                        GMS World is startup delivering solutions based on smartphone geolocation functionality.
                    </p>
                    <p>
                        Currently we are delivering number of projects including <b>Landmark Manager</b>, <b>Hotels on Map</b> and <b>Device Locator</b>.
                    </p>
                    <p>
                        <b>Landmark Manager</b> is mashup aggregating places from multiple services including
                        <b>Facebook</b>, <b>Foursquare</b>, <b>Yelp</b>, <b>Booking.com</b> and others.
                         Check in at places you are visiting, find daily deals, book hotel, find route and many more...<br/>

						Key features:<br/>

                        <ul>
						  <li>Check-in at <b>Facebook Places</b>, <b>Foursquare</b> or <b>Google</b>,</li>
						  <li>Auto Check-In: check-in automatically to your favourite places,</li>
						  <li>Find out where your <b>Facebook</b> or <b>Foursquare</b> friends checked-in,</li>
						  <li>Create custom layers,</li>
						  <li>Send updates to <b>Facebook</b>, <b>Twitter</b>, <b>Google Blogger</b> and <b>LinkedIn</b>,</li>
						  <li>Find daily deals from <b>Groupon</b>, <b>8 Coupons</b> and <b>Foursquare Merchant</b>,</li>
						  <li>Check hotel prices and book rooms with <b>Booking.com</b>, <b>Hotels Combined</b> and <b>Expedia</b>,</li>
						  <li>Find ATMs, parkings or routes,</li>
						  <li>Find events from <b>LastFM</b>, <b>MeetUp</b> and <b>Eventful</b>,</li>
						  <li>Record and save your route,</li>
						  <li>Import points of interest from kml files,</li>
						  <li>See photos from <b>Panoramio</b>, <b>Flickr</b> and <b>Picasa</b>,</li>
						  <li>See Webcams from <b>Webcam.Travel</b>,</li>
						  <li>Link your <b>Facebook</b>, <b>LinkedIn</b>, <b>Twitter</b>, <b>Google</b>, <b>Foursquare</b> or <b>GMS World</b> accounts,</li>
						  <li>Share selected landmark details via Mail, SMS, <b>Facebook</b>, <b>Twitter</b> and many more,</li>
						  <li>Send geo messages to Blogeo,</li>
						  <li>Call selected place,</li>
						  <li>Calculate route from your current position to selected point of interest.</li>
                        </ul>
                    </p>
                    <p>
                        <a href="http://www.hotelsonmap.net"><b>Hotels on Map</b></a> is hotels booking facility.<br/>
                        <b>Hotels on Map</b> let's you search for hotels around location you'll choose on the map or based on your browser location. <a href="http://www.hotelsonmap.net">Try it now!</a>
                    </p>
                    <p>
                        With <b>Device Locator</b> you could manage your device remotely with SMS or cloud commands. 
                        You can also track your device location and record route. Read more about Device Locator commands <a href="/dl">here</a>.
                    </p> 
                    <p>
                        Using our technology we are delivering following solutions:
                    	<ul>
                        	<li><b>Mobile marketing</b></li>
                        	<li><b>QR codes based location</b></li>
                        	<li><b>Augmented reality apps</b></li>
                        	<li><b>Geo Games</b></li>
                        	<li><b>Mobile security</b></li>
                    	</ul>
                    </p>
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
