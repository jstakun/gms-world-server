<%@page contentType="text/html" pageEncoding="utf-8" %>
<%@page import="com.jstakun.lm.server.utils.HtmlUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>GMS World - Route page</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
                  <!-- /main -->
<% 
	if (request.getAttribute("routeQueryString") != null) {
		String routeQueryString = (String)request.getAttribute("routeQueryString");
%>	
                <h3>GMS World route</h3>
                 <br/>
                 <p class="image-section">
                     <a href="/showLandmark/<%= request.getParameter("lat_start") %>/<%= request.getParameter("lng_start") %>">
                     	<img src="/image?<%= routeQueryString %>" title="See route on the map" alt="GMS World route" width="640" height="256"/>
                     </a>
                 </p>
                 <p class="post-details">
                     <b>Discover hotels around <a href="<%= HtmlUtils.getHotelLandmarkUrl(request.getParameter("lat_start"), request.getParameter("lng_start")) %>" target="_blank">route starting point</a> or around 
                     <a href="<%= HtmlUtils.getHotelLandmarkUrl(request.getParameter("lat_end"), request.getParameter("lng_end")) %>" target="_blank">route end point</a>!</b><br/>  
                 </p>
<%
	} else {
%>
                <h3>Route not found.</h3>
                 <div class="post">
                    <p>
                      Route has been archived or you have provided wrong parameters.
                    </p>
                 </div>  
<% 
	}
%>
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