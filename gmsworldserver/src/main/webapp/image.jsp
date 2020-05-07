<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.persistence.Screenshot,
                com.jstakun.lm.server.utils.HtmlUtils,
				net.gmsworld.server.utils.UrlUtils,
				net.gmsworld.server.utils.DateUtils,
				net.gmsworld.server.utils.StringUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <%
    	Screenshot screenshot = (Screenshot) request.getAttribute("screenshot");
        String address = (String) request.getAttribute("address");
        String imageUrl = "http://www.gms-world.net/images/LM_banner_512x250.jpg";
        if (screenshot != null) {
        	imageUrl = screenshot.getUrl();
        }
    %>
    <head>
        <title>GMS World - Discover interesting places around!</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="no-cache, no-store, must-revalidate"/>
        <meta HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
        <meta HTTP-EQUIV="EXPIRES" CONTENT="0"/>
        <meta name="author" content="GMS World - www.gms-world.net" />
        <meta name="description" content="GMS World - discover power of geo location" />
        <meta name="keywords" content="geo, location, geolocation, blogeo, j2me, nokia, blackberry, gps, iphone, android, htc, palm, windows phone, windows ce, samsung, galaxy" />
        <meta name="robots" content="index, follow, noarchive" />
        <meta name="googlebot" content="noarchive" />
        <meta property="fb:pages" content="165436696841663"/>
        <meta property="fb:app_id" content="197785136902139"/>
        <meta property="og:image" content="<%= imageUrl %>"/>
        <link rel="stylesheet" type="text/css" media="screen" href="/css/screen.css" />
        <%@ include file="/WEB-INF/jspf/gms_mobile_tracker.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
<%
	if (screenshot != null) { 
    String imageLink;
    String myposcode = request.getParameter("myposcode");
    if (myposcode != null) {
    	imageLink = "/showLandmark/" + myposcode; 
    } else {
    	imageLink = "/showLocation/" + HtmlUtils.encodeDouble(screenshot.getLatitude()) + "/" + HtmlUtils.encodeDouble(screenshot.getLongitude()) + "/fullScreen";
    }
%>

                <h3>Discover interesting places around</h3>

                <br/>
                    <!-- /main -->
                 <p class="image-section">
                     <a href="<%=imageLink%>">
                     	<img src="<%=screenshot.getUrl()%>" title="Discover interesting places around" alt="GMS World screenshot"/>
                     </a>
                 </p>
                 <p class="post-details">
                     <a href="<%=imageLink%>">Go to map</a><br/>
                     <%=address != null ? "Geocode address: " + address + "<br/>" : ""%>
                     Latitude: <%=StringUtil.formatCoordE6(screenshot.getLatitude())%>, Longitude: <%=StringUtil.formatCoordE6(screenshot.getLongitude())%><br/>
                     Posted on <%=DateUtils.getFormattedDateTime(request.getLocale(), screenshot.getCreationDate())%> by 
<%  if (screenshot.getUsername() != null) { %>
                     <a href="<%=response.encodeURL("/showUser/" + screenshot.getUsername())%>"><%=UrlUtils.createUsernameMask(screenshot.getUsername())%></a>
<%  } else { %>
                    <%=UrlUtils.createUsernameMask(screenshot.getUsername())%>
<%  } %>
                   <br/><b><a href="<%= HtmlUtils.getHotelLandmarkUrl(screenshot.getLatitude(), screenshot.getLongitude()) %>" target="_blank">Discover hotels around!</a></b><br/>
                  
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