<%-- 
    Document   : navigation
    Created on : 2010-12-20, 10:32:48
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<div class="navigation clear">
<%
 String pageLink = request.getParameter("page");

 String user = request.getParameter("user");

 String layer = request.getParameter("layer");

 String month = request.getParameter("month");

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
    <div><a href="/<%= pageLink %>.do?first=<%= next.intValue() %><%= nav %><%= month==null?"":"&month="+month %>">&laquo; Older Entries</a></div>
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
    <div><a href="/<%= pageLink %>.do?first=<%= prev.intValue() %><%= nav %><%= month==null?"":"&month="+month %>">Newer Entries &raquo;</a></div>
<%
    }
 }
%>
 
</div>
