<%-- 
    Document   : about
    Created on : 2010-12-17, 16:42:46
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.config.ConfigurationManager" %>
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
                        GMS World is startup delivering solutions based on geolocation.
                    </p>
                    <p>
                        We are APIs company. <a href="/apis.jsp">Learn more...</a>.
                    </p>
                    <p>
                        Currently we are delivering number of projects including <b>Landmark Manager</b>, <b>Hotels on Map</b> and <b>Device Locator</b>.
                    </p>
                    <p>
                        <b>Landmark Manager</b> is mashup aggregating places from multiple services including
                        <b>Facebook</b>, <b>Foursquare</b>, <b>Yelp</b>, <b>Booking.com</b> and others.
                         Check in at places you are visiting, find daily deals, book hotel, find route and many more...<br/><br/>

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
                        <a href="<%= ConfigurationManager.HOTELS_URL %>"><b>Hotels on Map</b></a> is hotels booking facility.<br/>
                        <b>Hotels on Map</b> let's you search for hotels around location you'll choose on the map or based on your browser location. <a href="<%= ConfigurationManager.HOTELS_URL %>">Try it now!</a>
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