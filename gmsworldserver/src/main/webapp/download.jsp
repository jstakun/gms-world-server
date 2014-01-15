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
        <title>Download Landmark Manager</title>
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

                <h3>Download Landmark Manager</h3>
                  <p>
                        <b>Landmark Manager</b> is mashup aggregating places from multiple services including
                        <b>Facebook</b>, <b>Foursquare</b>, <b>Google Places</b>, <b>Yelp</b>, <b>Qype</b>,
                        <b>Wikipedia</b>, <b>Eventful</b>, <b>Last FM</b>, <b>YouTube</b>,
                        <b>Webcam.Travel</b>, <b>Instagram</b>, <b>Flickr</b>, <b>Picasa</b>, <b>Panoramio</b>, 
                        <b>OSM</b>, <b>Foursquare Merchant</b>, <b>Groupon</b>, <b>8 Coupons</b>,
                        <b>Expedia</b> and <b>Hotels Combined</b>.
                        Check in at places you are visiting, find daily deals, book hotel, find route and many more...
                  </p>
                <p>
                    <b>Download Landmark Manager</b> application for your smartphone:
                </p>
                <p>
                    <ul>
                        <li>
                            <a href="/download/android/GMSClient3.apk">Android</a>.
                            Find Landmark Manager at <a href="http://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui">Google Play</a>,
                            <a href="http://www.amazon.com/gp/product/B004X2NR7O">Amazon Appstore</a> or <a href="http://apps.opera.com/landmark_manager.html">Opera Mobile Store</a>.
                            <i>Please note Android application is in Public Beta.</i><br/>
                            <img src="/images/qr_android.png" alt="Android download link QR Code"/>
                        </li>
                        <li>
                            <a href="/download/j2me/LandmarkManager.jar">Nokia, Motorola, Sony or any other J2ME compilant smartphone</a>.
                            <i>Please note J2ME application is in Public Beta.</i><br/>
                            <img src="/images/qr_j2me.png" alt="Nokia, Motorola, SonyErricson or any other J2ME compilant smartphone download link QR Code"/>
                        </li>
                        <li>
                            <a href="/download/blackberry/LandmarkManagerUIMIDlet.cod">Blackberry</a>.
                            <i>Please note Blackberry application is in Public Beta.</i><br/>
                            <img src="/images/qr_bb.png" alt="BlackBerry download link QR Code"/>
                        </li>
                        <li>
                            <a href="mailto:support@gms-world.net?subject=iPhone version request">iPhone</a>.
                            <i>Please note iPhone application is in Private Alfa.</i>
                        </li>
                    </ul>
                </p>
                <p>
                    <b>Download Deals Anywhere</b> application for your smartphone:
                </p>
                <p>
                    <ul>
                        <li>
                            <a href="/download/android/DealsAnywhere2.apk">Android</a>.
                            Find Deals Anywhere at <a href="http://market.android.com/details?id=com.jstakun.gms.android.ui.deals">Google Play</a>.
                            <i>Please note Android application is in Public Beta.</i>
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