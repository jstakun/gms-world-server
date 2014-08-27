<%-- 
    Document   : user
    Created on : 2010-12-19, 10:41:33
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
                 com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
                 com.jstakun.lm.server.utils.UrlUtils,net.gmsworld.server.utils.StringUtil,
                 com.jstakun.lm.server.persistence.Landmark,net.gmsworld.server.utils.DateUtils,java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Layer Landmarks</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>
    <body>
        <jsp:include page="/WEB-INF/jspf/header.jsp"/>
        <%
            String layer = null, formatted = null;
            if (request.getAttribute("layer") != null) {
               layer = (String)request.getAttribute("layer");
               formatted = LayerPersistenceUtils.getLayerFormattedName(layer);
            } else {
               layer = "none";
            }
        %>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                <h3>Landmarks in layer <%= formatted %></h3>

                 <br/>
                 <p>
                     <a href="/showLayer/<%= layer %>/fullScreen">See layer landmarks on the map</a>
                 </p>

                <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                      <jsp:param name="page" value="showLayer"/>
                      <jsp:param name="layer" value="<%= layer %>"/>
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
          Creation Date
        </th>
        <th>
          Validity Date
        </th>
      </tr>
<%
   if (request.getAttribute("layerLandmarks") == null)
   {
%>
<tr>
    <td colspan="5">No landmarks.</td>
</tr>
<%
   }
   else
   {
        List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("layerLandmarks");

        if (landmarkList.isEmpty())
        {
%>
<tr>
    <td colspan="5">No landmarks.</td>
</tr>
<%
        }
        else
        {
            for (Landmark landmark : landmarkList)
            {
%>
      <tr>
        <td width="32%"><a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>"><%= landmark.getName() %></a></td>
        <td width="17%"><%= StringUtil.formatCoordE6(landmark.getLatitude()) %></td>
        <td width="17%"><%= StringUtil.formatCoordE6(landmark.getLongitude()) %></td>
        <td width="17%"><%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate()) %></td>
        <td width="17%"><%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getValidityDate()) %></td>
      </tr>
<%
            }
       }
}
%>
                    
				</table>
                  <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                      <jsp:param name="page" value="showLayer"/>
                      <jsp:param name="layer" value="<%= layer %>"/>
                  </jsp:include>
                  <p>
                     <a href="/showLayer/<%= layer %>/fullScreen">See layer landmarks on the map</a>
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
