<%-- any content can be specified here e.g.: --%>
<%@ page pageEncoding="utf-8" %>
<%@ page import="com.jstakun.lm.server.utils.persistence.GeocodeCachePersistenceUtils,
                com.jstakun.lm.server.persistence.GeocodeCache,
                java.util.List,
                com.jstakun.lm.server.utils.memcache.CacheUtil,
                com.jstakun.lm.server.utils.DateUtils,
                com.google.appengine.api.datastore.KeyFactory" %>
<div id="sidebar">

    <div class="about-me">

        <h3>About Us</h3>
        <p>
            <a href="/index.jsp"><img src="/images/qr_main.jpg" width="40" height="40" alt="GMS qr code" class="float-left" /></a>
            We are startup delivering mobile solutions based on geolocation functionality.
            <a href="/about.jsp">Learn more...</a>
        </p>
        <p>
            <a href="http://appengine.google.com">
            	<img src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" alt="Powered by Google App Engine" />
            </a>
        </p>
        
        <p>
            <a href="http://www.openshift.com">
            	<img src="/images/openshift.png" alt="Powered by OpenShift" />
            </a>
        </p>
        
        <p>
            <a href="https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui">
  				<img alt="Get it on Google Play" src="/images/en_generic_rgb_wo_60.png" />
			</a>
        </p>
       
        <p>
            <a href="http://www.amazon.com/gp/product/B004X2NR7O/ref=mas_pm_Landmark_Manager">
            	<img alt="Get it on Amazon AppStore" src="/images/amazon-apps-store.png"/>
            </a>	
        </p>
        
        <p>
            <a href="http://apps.opera.com/badge.php?a=c&v=dark&did=7629&pid=36609" target="_blank"><img src="https://apps.opera.com/badge.php?a=s&v=dark&did=7629&pid=36609" width="173" height="55" alt="Opera Mobile Store" title="Opera Mobile Store"/></a>  
        </p>
    </div>

    <div class="sidemenu">

        <h3>Sidebar Menu</h3>
        <ul>
            <li><a href="/index.jsp">Home</a></li>
            <li><a href="/download.jsp">Download</a></li>
            <li><a href="/register.jsp">Register</a></li>
            <li><a href="/demo/run.jsp">Online Demo</a></li>
            <li><a href="http://blog.gms-world.net">Blog</a></li>
            <li><a href="/archive.do">Archives</a></li>
            <li><a href="/about.jsp">About</a></li>
        </ul>
        <h3 id="rgc">Recent Geocodes</h3>
        <ul>
<%
	List<GeocodeCache> geocodeCacheList = (List<GeocodeCache>)CacheUtil.getObject("newestGeocodes");
	if (geocodeCacheList == null) {
		geocodeCacheList = GeocodeCachePersistenceUtils.selectNewestGeocodes();
		CacheUtil.putToShortCache("newestGeocodes", geocodeCacheList);
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
        
        <p id="clustrmaps-widget">      	
         	<!-- a href="http://www2.clustrmaps.com/counter/maps.php?url=http://www.gms-world.net" id="clustrMapsLink">
         		<img src="http://www2.clustrmaps.com/counter/index2.php?url=http://www.gms-world.net" style="border:0px;" alt="Locations of visitors to GMS World page" title="Locations of visitors to GMS World page" id="clustrMapsImg" />
			</a>
			<script type="text/javascript">
				function cantload() {
					img = document.getElementById("clustrMapsImg");
					img.onerror = null;
					img.src = "http://clustrmaps.com/images/clustrmaps-back-soon.jpg";
					document.getElementById("clustrMapsLink").href = "http://clustrmaps.com";
				}
				img = document.getElementById("clustrMapsImg");
				img.onerror = cantload;
			</script -->
			
        </p> 
            <script type="text/javascript">var _clustrmaps = {'url' : 'http://www.gms-world.net', 'user' : 1119080, 'server' : '2', 'id' : 'clustrmaps-widget', 'version' : 1, 'date' : '2013-11-11', 'lang' : 'en', 'corners' : 'square' };
             (function (){ var s = document.createElement('script'); s.type = 'text/javascript'; s.async = true; s.src = 'http://www2.clustrmaps.com/counter/map.js'; var x = document.getElementsByTagName('script')[0]; x.parentNode.insertBefore(s, x);})();
            </script>
            <noscript>
              <a href="http://www2.clustrmaps.com/user/a1f111368"><img src="http://www2.clustrmaps.com/stats/maps-no_clusters/www.gms-world.net-thumb.jpg" alt="Locations of visitors to this page" /></a>
            </noscript>
    </div>

    <!-- /sidebar -->
</div>
