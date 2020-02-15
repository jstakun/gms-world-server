<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@page import="net.gmsworld.server.utils.NumberUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html>

    <head>
        <title>Book your stay. Select location on the map...</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                <h3>Select location on the map</h3>
<% 
if (request.getParameter("lat") != null && request.getParameter("lng") != null) 
{
	String address = request.getParameter("address");
	if (address == null) {
		address = "Selected location"; 
	}
	int zoom = NumberUtils.getInt(request.getParameter("zoom"), 12);
%>
                <ins class="bookingaff" data-aid="864526" data-target_aid="864526" 
                        data-prod="map" data-width="100%" data-height="1000" 
                        data-lang="ualng" data-dest_id="0" data-dest_type="landmark" 
                        data-latitude="<%= request.getParameter("lat") %>" 
                        data-longitude="<%= request.getParameter("lng") %>" 
                        data-landmark_name="<%= address %>" data-mwhsb="0" 
                        data-address="<%= address %>" data-zoom="<%= zoom %>">
    					<!-- Anything inside will go away once widget is loaded. -->
        				<a href="//www.booking.com?aid=864526">Booking.com</a>
				</ins>
<% 
} else { 
%>
				<p><b>Please specify location!</b></p>
<% 
} 
%>
				<script type="text/javascript">
    				(function(d, sc, u) {
      				var s = d.createElement(sc), p = d.getElementsByTagName(sc)[0];
      				s.type = 'text/javascript';
      				s.async = true;
      				s.src = u + '?v=' + (+new Date());
      				p.parentNode.insertBefore(s,p);
      				})(document, 'script', '//aff.bstatic.com/static/affiliate_base/js/flexiproduct.js');
</script>
                
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

       <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>