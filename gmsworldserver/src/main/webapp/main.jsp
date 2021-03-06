<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils,
         net.gmsworld.server.utils.persistence.Landmark,
         com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
         net.gmsworld.server.utils.UrlUtils,
         net.gmsworld.server.utils.DateUtils,
         com.jstakun.lm.server.utils.HtmlUtils,
         com.jstakun.lm.server.utils.memcache.CacheUtil,
         com.jstakun.lm.server.config.ConfigurationManager,
         org.apache.commons.lang.StringUtils,
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
                    </div>
                    
                    <div class="post">
                        <p>
                         	<a href="<%= ConfigurationManager.HOTELS_URL %>" name="Discover hotels around selected location">Select location on the map and discover hotels around</a>.<br/>
                         	<a href="<%= ConfigurationManager.HOTELS_URL %>"><img src="/images/hotel2.jpg" alt="Discover hotels around selected location" title="Click to discover hotel around selected location" height="250" width="500" class="float-left"/></a>
                    	</p>
                    </div>
                    
                    <div class="post">
                        <p>
                         	Don't know where to go? <a href="<%= HtmlUtils.getRandomUrl(pageContext.getServletContext()) %>">Let us choose for you!</a>
                         </p>
                    </div>
                    
                    <div class="post">
                    	<p>
                    		<a href="/share">Share your location</a> or <a href="/landmarks">select location on the map</a> and discover landmarks nearby.<br/>
                            <a href="/share"><img src="/images/LM_banner_512x250.jpg" alt="GMS World Banner" title="Click to share you location" height="250" width="512" class="float-left"/></a>                    	
                    	</p>
                    </div>
                    
                    <div class="post">
                        <p>
                         	Find out where <a href="/heatMap">GMS World</a> is most popular.<br/>
                         	<a href="/heatMap"><img src="/images/heatmap_2.jpg" alt="GMS World Heat Map" title="Click to see GMS World Heat Map" height="250" width="500" class="float-left"/></a>
                    	</p>
                    </div>
                    
                    <h3>Newest Landmarks</h3>

                    <%
                    	List<Landmark> landmarkList = HtmlUtils.getList(Landmark.class, request, "NewestLandmarks");
                        if (landmarkList != null) { 
                             for (Landmark landmark : landmarkList) {
                    %>
                    <div class="post">
                        <p>
                            <a href="<%=response.encodeURL("/showLandmark/" + landmark.getId())%>"><img src="/image/<%=landmark.getLatitude()%>/<%=landmark.getLongitude()%>" alt="Landmark on the map" title="See landmark on the map" height="128" width="128" class="float-left"/></a>
                            <h4><a href="<%=response.encodeURL("/showLandmark/" + landmark.getId())%>"><%=landmark.getName()%></a></h4>
                            <%= HtmlUtils.getLandmarkDesc(landmark, request.getLocale()) %>
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
