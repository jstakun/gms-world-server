<%-- 
    Document   : navigation
    Created on : 2010-12-20, 10:32:48
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<div class="paginate">
   <ul>
<%
 String pageLink = request.getParameter("page");

 String user = request.getParameter("user");

 String layer = request.getParameter("layer");

 String month = request.getParameter("month");
 String year = request.getParameter("year");
 if (year != null) {
	 month = month + "-" + year;
 }

 String nav = "";

 if (user != null) {
     nav = "&user=" + user;
 } else if (layer != null) {
     nav = "&layer=" + layer;
 }

 if (request.getAttribute("next") != null)
 {
    Integer next = (Integer)request.getAttribute("next");
    if (next.intValue() >= 0)
    {
%>
    <li><a href="/<%= pageLink %>.do?first=<%= next.intValue() %><%= nav %><%= month==null?"":"&month="+month %>">&laquo; Older Entries</a></li>
<%
    }
 }
%>
<%
 if (request.getAttribute("prev") != null)
 {
    Integer prev = (Integer)request.getAttribute("prev");
    if (prev.intValue() >= 0)
    {
%>
    <li><a href="/<%= pageLink %>.do?first=<%= prev.intValue() %><%= nav %><%= month==null?"":"&month="+month %>">Newer Entries &raquo;</a></li>
<%
    }
 }
%>
  </ul> 
</div>
