<%-- any content can be specified here e.g.: --%>
<%@ page pageEncoding="utf-8" %>
<%@ page import="net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils,
                 net.gmsworld.server.utils.persistence.GeocodeCache,
                 com.jstakun.lm.server.utils.memcache.CacheUtil,
                 java.util.List,
                 net.gmsworld.server.utils.DateUtils" %>
<div id="sidebar">

    <div class="about-me">

        <h3>About Us</h3>
        <p>
            <a href="/index.jsp"><img src="/images/gms_world_qr_code.png" width="40" height="40" alt="GMS qr code" class="float-left" /></a>
            We are startup delivering mobile solutions based on geolocation functionality.
            <a href="/about.jsp">Learn more...</a>
        </p>
        <p>
            <a href="http://appengine.google.com" target="_blank">
            	<img src="/images/appengine-noborder-120x30.gif" alt="Powered by Google App Engine" title="Powered by Google App Engine"/>
            </a>
        </p>
        
        <p>
            <a href="http://www.openshift.com" target="_blank">
            	<img src="/images/openshift.png" alt="Powered by OpenShift" title="Powered by OpenShift"/>
            </a>
        </p>
        
        <p>
            <a href="https://play.google.com/store/apps/developer?id=GMS+World" target="_blank">
  				<img alt="Get it on Google Play" title="Get it on Google Play" src="/images/en_generic_rgb_wo_60.png" />
			</a>
        </p>
       
        <!--p>
            <a href="http://www.amazon.com/gp/product/B004X2NR7O/ref=mas_pm_Landmark_Manager" target="_blank">
            	<img alt="Get it on Amazon AppStore" title="Get it on Amazon AppStore" src="/images/amazon-apps-store.png"/>
            </a>	
        </p-->
        
        <!--p>
            <a href="http://apps.opera.com/badge.php?a=c&v=dark&did=7629&pid=36609" target="_blank">
            	<img src="https://apps.opera.com/badge.php?a=s&v=dark&did=7629&pid=36609" width="173" height="55" alt="Get it on Opera Mobile Store" title="Get in on Opera Mobile Store"/>
            </a>  
        </p-->
        
        <!--p>
            <a href="https://store.yandex.com/" target="_blank">
            	<img alt="Get it on Yandex Mobile Store" title="Get in on Yandex Mobile Store" src="/images/yandex.jpg" width="160" height="52"/>
            </a>  
        </p-->
    </div>

    <div class="sidemenu">

        <h3>Sidebar Menu</h3>
        <ul>
            <li><a href="/hotels">Hotels</a></li>
            <li><a href="/landmarks">Landmarks</a></li>
            <li><a href="/index.jsp">Home</a></li>
            <li><a href="/download.jsp">Download</a></li>
            <li><a href="/register.jsp">Register</a></li>
            <li><a href="http://blog.gms-world.net">Blog</a></li>
            <li><a href="/archive.do">Archives</a></li>
            <li><a href="/about.jsp">About</a></li>
        </ul>
        <h3 id="rgc">Recent Geocodes</h3>
        <ul>
<%
	
	List<GeocodeCache> geocodeCacheList = CacheUtil.getList(GeocodeCache.class, "newestGeocodes");
	if (geocodeCacheList == null) {
		geocodeCacheList = GeocodeCachePersistenceUtils.selectNewestGeocodes();
		CacheUtil.put("newestGeocodes", geocodeCacheList, CacheUtil.CacheType.FAST);
	}
	    
    for (GeocodeCache geocodeCache : geocodeCacheList)
    {
%>
            <li><a href="/showGeocode/<%= geocodeCache.getId() %>"><%= geocodeCache.getLocation() %></a><br/><span>Posted on <%= DateUtils.getFormattedDateTime(request.getLocale(), geocodeCache.getCreationDate()) %></span></li>
<%
    }
%>
        </ul>
        <h3 id="vistors">Visitors</h3>
        <p>
            <a href="http://www.clustrmaps.com/map/gms-world.net" title="Visitor Map for www.gms-world.net">
            	<img src="//www.clustrmaps.com/map_v2.png?u=TB9e&d=gO7aj1HRShvSvwKzJya-Xle_S979_yAtXMEddRQ9Qu0"/>
            </a>
        	<!-- script type="text/javascript" id="clustrmaps" src="//cdn.clustrmaps.com/map_v2.js?u=TB9e&d=gO7aj1HRShvSvwKzJya-Xle_S979_yAtXMEddRQ9Qu0"></script-->
        </p>
    </div>
    <!-- /sidebar -->
</div>
