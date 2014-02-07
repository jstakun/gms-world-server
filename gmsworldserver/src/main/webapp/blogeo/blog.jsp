<%-- 
    Document   : blog.jsp
    Created on : 2010-12-23, 10:20:51
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
                 com.jstakun.lm.server.persistence.Landmark,
                 java.util.List,
                 java.util.Date,
                 com.jstakun.lm.server.utils.DateUtils,
                 com.jstakun.lm.server.utils.StringUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Blogeo - GeoLocation Blog</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp">
            <jsp:param name="mode" value="blogeo"/>
        </jsp:include>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

 <%
   String user = null;
   if (request.getAttribute("userLandmarks") == null)
   {
%>
<h3>No posts in this blog.</h3>
<%
   }
   else
   {
        List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("userLandmarks");

        if (landmarkList.isEmpty())
        {
%>
<h3>No posts in this blog.</h3>
<%
        }
        else
        {
            
            for (Landmark landmark : landmarkList)
            {
                user = landmark.getUsername();
                Date creationDate = landmark.getCreationDate();
                String time = DateUtils.getTimeString(creationDate);
                String month = DateUtils.getMonthString(creationDate);
                String day = DateUtils.getDayOfMonthString(creationDate);
%>

                    <div class="post">

                        <div class="right">

                            <h3><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>"><%= landmark.getName() %></a></h3>

                            <p>Description: <%= landmark.getDescription() %></p>

                            <p>
                                <img src="http://maps.google.com/maps/api/staticmap?center=<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>&zoom=9&size=146x146&sensor=false&markers=color:blue|<%= landmark.getLatitude()%>,<%= landmark.getLongitude()%>" alt="Landmark on Google Map" height="146" width="146"></img><br/>
                            </p>

                        </div>

                        <div class="left">

                            <p class="dateinfo"><%= month %><span><%= day %></span></p>

                            <div class="post-meta">
                                <h4>Post Info</h4>
                                <ul>
                                    <li class="user"><a href="/showUser/<%= landmark.getUsername() %>"><%= landmark.getUsername() %></a></li>
                                    <li class="time"><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>"><%= time %></a></li>
                                    <li class="permalink"><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>">Permalink</a></li>
<%
    if (StringUtil.isSocialUser(user))
    {
%>
                                     <li class="comment">
                                         <a href="/socialProfile?uid=<%= landmark.getUsername() %>">My Social profile</a>
                                     </li>
<%
    }
%>
                                </ul>
                            </div>

                        </div>

                    </div>

<%
            }
       }
}
%>
                      <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                      <jsp:param name="page" value="blogeo"/>
                      <jsp:param name="user" value="<%= user %>"/>
                </jsp:include>
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
