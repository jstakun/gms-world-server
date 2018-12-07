<%-- 
    Document   : download
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Download GMS World applications</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp">
             <jsp:param name="current" value="download" />
        </jsp:include>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                <h3>Download GMS World applications</h3>
                  <p>
                        <b>Landmark Manager</b> is mashup aggregating places from multiple services including
                        <b>Facebook</b>, <b>Foursquare</b>, <b>Yelp</b>, <b>Booking.com</b> and others.
                        Check in at places you are visiting, find daily deals, book hotel, find route and many more...
                  </p>
                <p>
                    <b>Download Landmark Manager</b> for your smartphone:
                </p>
                <p>
                    <ul>
                        <li>
                            <a href="https://github.com/jstakun/gms-world-client/raw/master/GMSClientNew/GMSClientNext.apk">Android</a>.
                            Find Landmark Manager at <a href="http://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui">Google Play</a>,
                            <a href="http://www.amazon.com/gp/product/B004X2NR7O">Amazon Appstore</a> or <a href="http://apps.opera.com/landmark_manager.html">Opera Mobile Store</a>.
                            <i>Please note Android application is in Public Beta.</i><br/>
                        </li>
                        <li>
                            <a href="mailto:support@gms-world.net?subject=iPhone version request">iPhone</a>.
                            <i>Please note iPhone application is in Private Alfa.</i>
                        </li>
                    </ul>
                </p>
                <br/>
                <p>
                        With <b>Device Locator</b> you could manage your device remotely with SMS or cloud commands. 
                        You can also track your device location and record route. Read more about Device Locator commands <a href="/dl">here</a>.
                 </p> 
                <p>
                    <b>Download Device Locator</b>  for your smartphone:
                </p>
                <p>
                    <ul>
                        <li>
                            <a href="https://github.com/jstakun/device-locator/raw/master/app/app-release.apk">Android</a>.
                            Find Device Locator at <a href="http://market.android.com/details?id=net.gmsworld.devicelocator">Google Play</a>.
                           <i>Please note Android application is in Public Beta.</i><br/>
                        </li>
                    </ul>
                </p>
                <p>
                    &nbsp;
                </p>
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