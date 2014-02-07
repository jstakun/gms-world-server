<%-- 
    Document   : index
    Created on : 2010-12-18, 14:22:27
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
         com.jstakun.lm.server.persistence.Landmark,
         com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
         org.ocpsoft.prettytime.PrettyTime,
         com.jstakun.lm.server.utils.UrlUtils,
         com.jstakun.lm.server.utils.DateUtils,
         com.jstakun.lm.server.utils.memcache.CacheUtil,
         java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>GMS World - discover interesting places anywhere!</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>
    <body>
        <jsp:include page="/WEB-INF/jspf/header.jsp" flush="true">
            <jsp:param name="current" value="home" />
        </jsp:include>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
                    <h3>Welcome to GMS World</h3>
                    <div class="post">
                    <p>
                        Discover interesting places anywhere with <a href="/download.jsp"><b>Landmark Manager</b></a>.
                    </p>
                    
                    </div>

                    <!-- AddThis Button BEGIN -->
<p class="addthis_toolbox addthis_default_style ">
<a class="addthis_button_facebook_like" fb:like:layout="button_count"></a>
<a class="addthis_button_tweet"></a>
<a class="addthis_button_google_plusone" g:plusone:size="medium"></a>
<a class="addthis_button_linkedin_counter"></a>  
<a class="addthis_counter addthis_pill_style"></a>
<!--a class="addthis_button_pinterest_pinit" pi:pinit:url="http://www.gms-world.net" pi:pinit:media="http://www.gms-world.net/images/heatmap_2.jpg" pi:pinit:layout="horizontal" pi:pinit:description="GMS World heat map"></a-->
</p>
<script type="text/javascript">var addthis_config = {"data_track_addressbar":true};</script>
<script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-514c9bf26802d421"></script>
<!-- AddThis Button END -->
                    
                    <div class="post">
                        <p>
                         Find out where <a href="/heatMap">GMS World</a> is most popular.<br/>
                         <a href="/heatMap"><img src="/images/heatmap_2.jpg" alt="GMS World heat map" height="250" width="500" class="float-left"/></a><br/><br/>
                    </p>
                    </div>

                    <h3>Latest Landmarks</h3>

                    <%
                        List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("newestLandmarkList");
                        if (landmarkList != null) { 
                        	PrettyTime prettyTime = new PrettyTime(request.getLocale());
                        	for (Landmark landmark : landmarkList) {
                    %>
                    <div class="post">
                        <p>
                            <a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>"><img src="http://maps.google.com/maps/api/staticmap?center=<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>&zoom=9&size=128x128&sensor=false&markers=icon:http://gms-world.appspot.com/images/flagblue.png|<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>" alt="Landmark on Google Map" height="128" width="128" class="float-left"/></a>
                            <h4><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId())%>"><%= landmark.getName() %></a></h4>
                            Posted <%= prettyTime.format(landmark.getCreationDate()) %> on <%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate())%>
                            by <a href="<%= landmark.getLayer().equals("Social") ? response.encodeURL("/blogeo/" + landmark.getUsername()) : response.encodeURL("/showUser/" + landmark.getUsername())%>"><%= UrlUtils.createUsernameMask(landmark.getUsername())%></a> | Filed in layer <a href="/showLayer/<%= landmark.getLayer() %>"><%= LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer())%></a>
                        </p>
                    </div>
                    <%
                        	}
                        }
                    %>
                    <%@ include file="/WEB-INF/jspf/ad_medium_baner.jspf" %>
                    <!-- /main -->
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

        <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>
