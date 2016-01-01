<%-- 
    Document   : about
    Created on : 2010-12-17, 16:42:46
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>GMS World - About Us</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp">
            <jsp:param name="current" value="about" />
        </jsp:include>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <h3>About Us</h3>

                    <p>
                        GMS World is startup delivering solutions based on smartphone geolocation functionality.
                    </p>
                    <p>
                        We are APIs company. <a href="/apis.jsp">Learn more...</a>.
                    </p>
                    <p>
                        Currently we are delivering number of projects including <b>Landmark Manager</b>, <b>Hotels on Map</b> and <b>Blogeo</b>.
                    </p>
                    <p>
                        <b>Landmark Manager</b> is mashup aggregating places from multiple providers including <b>Facebook</b>, <b>Twitter</b>, <b>Foursquare</b>, <b>Yelp</b>, <b>Google Places</b>, <b>Freebase</b>, <b>Wikipedia</b>, <b>Eventful</b>,
                        <b>Last FM</b>, <b>YouTube</b>, <b>Flickr</b>, <b>Picasa</b>, <b>Foursquare Merchant</b>, <b>Panoramio</b>, <b>Groupon</b>, <b>8 Coupons</b>, <b>Expedia</b>, <b>Booking.com</b> and <b>Hotels Combined</b>.
                        Automatically check-in at places you are visiting, find daily deals, book hotel, find parking, route or ATM and many more...<br/><br/>

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
                        <a href="http://gms-blogeo.appspot.com"><b>Blogeo</b></a> is revolutionery GeoLocation Blog.<br/>
                        <b>Blogeo</b> combines regular blog features with mobile geolocation allowing smartphones users to send post including
                        their location. For more details please check my <a href="/blogeo/jstakun"><b>Blogeo</b></a>.
                    </p>
                    <p>
                        Using our technology we are delivering following solutions:
                    	<ul>
                        	<li><b>Mobile marketing</b></li>
                        	<li><b>QR codes based location</b></li>
                        	<li><b>Augmented reality apps</b></li>
                        	<li><b>Geo Games</b></li>
                    	</ul>
                    </p>
                    <br/>
                    <!-- /main -->
                    <%@ include file="/WEB-INF/jspf/ad_medium_baner.jspf" %>
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

        <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>