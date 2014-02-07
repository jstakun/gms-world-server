<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
                 com.jstakun.lm.server.persistence.Landmark,
                 com.jstakun.lm.server.utils.StringUtil,
                 com.jstakun.lm.server.utils.DateUtils,
                 java.util.List"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>List of Landmarks</title>
</head>
  <body>
    <h2>
      List of Landmarks
    </h2>
    <table cellspacing="2" cellpadding="3" border="1" align="center"
           width="100%">
      <tr>
        <th>
          Key
        </th>
        <th>
          Name
        </th>
        <th>
          Description
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
          Created By
        </th>
        <th>
          Validity Date
        </th>
        <th>
          Layer
        </th>
        <th>
          Action
        </th>
      </tr>
<% 
   if (request.getAttribute("landmarkList") == null)
   {
%>
<tr>
    <td colspan="10">No landmarks.</td>
</tr>
<%
   }
   else
   {
        List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("landmarkList");

        if (landmarkList.isEmpty())
        {
%>
<tr>
    <td colspan="10">No landmarks.</td>
</tr>
<%
        }
        else
        {
            for (Landmark landmark : landmarkList)
            {
%>
      <tr>
          <td width="10%"><%= landmark.getId() %></td>
        <td width="10%"><%= landmark.getName() %></td>
        <td width="10%"><%= landmark.getDescription() %></td>
        <td width="10%"><%= StringUtil.formatCoordE6(landmark.getLatitude()) %></td>
        <td width="10%"><%= StringUtil.formatCoordE6(landmark.getLongitude()) %></td>
        <td width="10%"><%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getCreationDate()) %></td>
        <td width="10%"><%= landmark.getUsername() %></td>
        <td width="10%"><%= DateUtils.getFormattedDateTime(request.getLocale(), landmark.getValidityDate()) %></td>
        <td width="10%"><%= landmark.getLayer() %></td>
        <td width="10%">
            <a href="updateLandmark.jsp?key=<%= landmark.getId() %>">Update</a>,&nbsp;
            <a href="deleteLandmark.do?key=<%= landmark.getId() %>">Delete</a>,&nbsp;
            <a href="<%= response.encodeURL("/showLandmark/" + landmark.getId()) %>">Show</a>
        </td>
      </tr>
<%
            }
       }
}
%>
    </table>
    <div><BR/><a href="index.jsp">Back</a></div>
  </body>
</html>
