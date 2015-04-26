<%-- 
    Document   : template
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun
--%>
<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.ImageUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>GMS World - Selected Location</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <% 
                Double lat = null, lon = null;    
                try {    
    				lat = Double.valueOf((String)request.getAttribute("lat"));
            		lon = Double.valueOf((String)request.getAttribute("lon"));
                } catch (Exception e) {
                	
                }
            	if (lat == null || lon == null) {
            %>
            	<h3>No location specified</h3>
            <%                    
                } else {      
            %>
                <h3>Your location</h3>
                <h4>You've selected following location:</h4>

                <img src="<%= ImageUtils.getGoogleMapsImageUrl(lat.doubleValue(), lon.doubleValue(), "640x256", 10) %>" alt="Landmark on Google Map" height="256" width="640"/><br/>
                
                <p>
                   <%= request.getAttribute("address")!=null ? "Geocode address: " + request.getAttribute("address") : "" %><br/>
                   Latitude: <%= lat %>, Longitude: <%= lon %><br/>
                </p>
             <%
                }
             %>

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