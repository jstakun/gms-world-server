<%-- 
    Document   : user
    Created on : 2010-12-19, 10:41:33
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils,
                 com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
                 com.jstakun.lm.server.utils.HtmlUtils,
                 net.gmsworld.server.utils.StringUtil,
                 net.gmsworld.server.utils.persistence.Landmark,
                 org.ocpsoft.prettytime.PrettyTime,
                 java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>User or Device Landmarks</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>

        <%
            String user = (String)request.getAttribute("user");
            if (user == null) {
               user = "none";
            }   
        %>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                <h3>Landmarks created by 
<%
    if (StringUtil.isSocialUser(user))
    {
%>
<a href="/socialProfile?uid=<%= user %>"><%= user %></a>
<%
    }
    else
    {
%>   
        <a href="/showUser/<%= user %>"><%= user %></a>
<%
    }
%>
                </h3>

                 <br/>
                 <p>
                     <a href="/showUser/<%= user %>/fullScreen">See this landmarks on the map</a>
                 </p>

                <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                      <jsp:param name="page" value="showUser"/>
                      <jsp:param name="user" value="<%= user %>"/>
                </jsp:include>

                <table><tr>
        <th>
          Name
        </th>
        <th>
          Latitude
        </th>
        <th>
          Longitude
        </th>
        <th>
          Created
        </th>
        <th>
          Layer
        </th>
      </tr>
<%
   if (request.getAttribute("userLandmarks") == null)
   {
%>
<tr>
    <td colspan="5">No landmarks.</td>
</tr>
<%
   }
   else
   {
        List<Landmark> landmarkList = HtmlUtils.getList(Landmark.class, request, "userLandmarks");

        if (landmarkList == null || landmarkList.isEmpty())
        {
%>
<tr>
    <td colspan="5">No landmarks.</td>
</tr>
<%
        }
        else
        {
        	PrettyTime prettyTime = new PrettyTime(request.getLocale());
            for (Landmark landmark : landmarkList)
            {
%>
      <tr>
        <td width="32%"><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>"><%= landmark.getName() %></a></td>
        <td width="17%"><%= StringUtil.formatCoordE6(landmark.getLatitude()) %></td>
        <td width="17%"><%= StringUtil.formatCoordE6(landmark.getLongitude()) %></td>
        <td width="17%"><%= prettyTime.format(landmark.getCreationDate()) %></td>
        <td width="17%"><a href="/showLayer/<%=landmark.getLayer()%>"><%=LayerPersistenceUtils.getLayerFormattedName(landmark.getLayer())%></a></td>
      </tr>
<%
            }
       }
}
%>
                    
				</table>
                  <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                      <jsp:param name="page" value="showUser"/>
                      <jsp:param name="user" value="<%= user %>"/>
                  </jsp:include>
                  <p>
                     <a href="/showUser/<%= user %>/fullScreen">See user landmarks on the map</a>
                  </p>
                  <br/>
                     <!-- /main -->
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
