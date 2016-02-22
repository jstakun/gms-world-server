<%-- any content can be specified here e.g.: --%>
<%@ page pageEncoding="utf-8" %>
<%@ page import="net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils,
                 net.gmsworld.server.utils.persistence.Landmark,
                 com.jstakun.lm.server.utils.HtmlUtils,
                 net.gmsworld.server.utils.UrlUtils,
                 org.apache.commons.lang.StringUtils,
         		java.util.List"%>
<div id="footer-outer" class="clear"><div id="footer-wrap">

        <div class="col-a">

            <h3>Contact Info</h3>

            <p><strong>Address: </strong>Warsaw, Poland</p>
            <p><strong>E-mail: </strong><a href="mailto:support@gms-world.net">support@gms-world.net</a></p>
            <p>Want more info - go to our <a href="/contact.jsp">contact page</a></p>

            <h3>Updates</h3>

            <ul class="subscribe-stuff">
                <li><a title="Blog" href="http://blog.gms-world.net" rel="nofollow" target="_blank">
                        <img alt="Blog" title="Blog" src="/images/blogger.png" /></a>
                </li>
                <li><a title="Facebook" href="http://www.facebook.com/pages/GMS-World/165436696841663" rel="nofollow" target="_blank">
                        <img alt="Facebook" title="Facebook" src="/images/social_facebook.png" /></a>
                </li>
                <li><a title="Twitter" href="http://twitter.com/geolocationms" rel="nofollow" target="_blank">
                        <img alt="Twitter" title="Twitter" src="/images/social_twitter.png" /></a>
                </li>
                <li><a title="Google+" href="https://plus.google.com/117623384724994541747" rel="nofollow" target="_blank">
                        <img alt="Google+" title="Google+" src="/images/google_plus.png" /></a>
                </li>
            </ul>

            <p>Stay up to date. Subscribe via
                <a href="http://blog.gms-world.net" target="_blank">Blog</a>,
                <a href="http://www.facebook.com/pages/GMS-World/165436696841663" target="_blank">Facebook</a>,
                <a href="http://twitter.com/geolocationms" target="_blank">Twitter</a> or 
                <a href="https://plus.google.com/117623384724994541747" rel="publisher" target="_blank">Google+</a>
            </p>

        </div>

        <div class="col-a">

            <h3>Site Links</h3>

            <div class="footer-list">
                <ul>
                    <li><a href="http://www.hotelsonmap.net">Hotels</a></li>
                    <li><a href="/landmarks">Landmarks</a></li>
                    <li><a href="/index.jsp">Home</a></li>
                    <li><a href="/download.jsp">Download</a></li>
                    <li><a href="http://blog.gms-world.net">Blog</a></li>
                    <li><a href="/archive.do">Archives</a></li>
                    <li><a href="/about.jsp">About</a></li>
                    <li><a href="/register.jsp">Register</a></li>
                    <li><a href="/privacy.jsp">Privacy policy</a></li>
                </ul>
            </div>


        </div>

        <div class="col-a">

            <h3>Newest Landmarks</h3>

            <div class="recent-comments">
                <ul>
<%
	List<Landmark> landmarkList1 = HtmlUtils.getList(Landmark.class, request, "newestLandmarkList");

   if (landmarkList1 == null) {
	   landmarkList1 = LandmarkPersistenceUtils.selectNewestLandmarks();
   }
   
   if (landmarkList1 != null) {
   
   		for (Landmark landmark : landmarkList1) {
   			String name = landmark.getDescription();
       	 	if (StringUtils.isEmpty(name)) {
       			name = landmark.getName();
       	 	}
%>
 <li><a href="<%=response.encodeURL("/showLandmark/" + landmark.getId())%>" title="<%= landmark.getName() %>"><%=name%></a><br/> &#45; <cite><a href="<%=response.encodeURL("/showUser/" + landmark.getUsername())%>"><%=UrlUtils.createUsernameMask(landmark.getUsername())%></a></cite></li>
 <%
   		}
   
   }
 %>
                </ul>
            </div>

        </div>

        <div class="col-b">

            <h3>Archives</h3>

            <div class="footer-list">
                <ul>
					<%= HtmlUtils.getArchivesUrls() %>        
                </ul>
            </div>

        </div>

        <!-- /footer-outer -->
    </div></div>

<!-- footer-bottom -->
<div id="footer-bottom">

    <p class="bottom-left">
        &copy; 2010-16 <strong>GMS World</strong>&nbsp; &nbsp; &nbsp;
        <a href="http://www.bluewebtemplates.com/" title="Website Templates">website templates</a> by <a href="http://www.styleshout.com/">styleshout</a>
    </p>

    <p class="bottom-right">
        <a href="http://jigsaw.w3.org/css-validator/check/referer">CSS</a> |
        <a href="http://validator.w3.org/check/referer">XHTML</a>	|
        <a href="/index.jsp">Home</a> |
        <strong><a href="#top">Back to Top</a></strong>
    </p>
    
    <a name="bottom"></a>
   
</div>



