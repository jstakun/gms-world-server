<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.Screenshot,
        com.jstakun.lm.server.utils.UrlUtils,net.gmsworld.server.utils.DateUtils,net.gmsworld.server.utils.StringUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <%
       Screenshot screenshot = (Screenshot)request.getAttribute("screenshot");
       String address = (String) request.getAttribute("address");
    %>
    <head>
        <title>GMS World - Screenshot page</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
<% if (screenshot != null) { 
    String imageLink;
    String myposcode = request.getParameter("myposcode");
    if (myposcode != null) {
    	imageLink = "/showLandmark/" + myposcode; 
    } else {
    	imageLink = "/showLocation/" + screenshot.getLatitude() + "/" + screenshot.getLongitude();
    }
%>

                <h3>GMS World screenshot</h3>

                <br/>
                    <!-- /main -->
                 <p class="image-section">
                     <a href="<%= imageLink %>">
                     	<img src="<%= screenshot.getUrl() %>" alt="GMS World screenshot"/>
                     </a>
                 </p>
                 <p class="post-details">
                     <%= address != null ? "Geocode address: " + address + "<br/>" : ""%>
                     Latitude: <%= StringUtil.formatCoordE6(screenshot.getLatitude()) %>, Longitude: <%= StringUtil.formatCoordE6(screenshot.getLongitude()) %><br/>
                     Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), screenshot.getCreationDate()) %> by 
    <% if (screenshot.getUsername() != null) { %>
                     <a href="<%= response.encodeURL("/showUser/" + screenshot.getUsername()) %>"><%= UrlUtils.createUsernameMask(screenshot.getUsername()) %></a>
    <% } else { %>
                     <%= UrlUtils.createUsernameMask(screenshot.getUsername()) %>
    <% } %>
                  <br/>
                 </p>
<% } else { %>
                 <h3>Image not found.</h3>
                 <div class="post">
                    <p>
                 Image has been archived or you have provided wrong image key.
                    </p>
                 </div>   
<% } %>
                 <br/>
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