<%-- 
    Document   : createParameter
    Created on : 2011-03-30, 21:26:42
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.ConfigPersistenceUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create config param</title>
    </head>
    <body>
        <h2>Create parameter</h2>
<%
    if (request.getParameter("key") != null && request.getParameter("value") != null) {
        String key = request.getParameter("key");
        String value = request.getParameter("value");
        ConfigPersistenceUtils.persistConfig(key, value);
        out.println("Parameter " + key + " created.");
    }
    else {
        out.println("Wrong parameters.");
    }

%>
    <div><br/><a href="index.jsp">Back</a></div>
    </body>
</html>
