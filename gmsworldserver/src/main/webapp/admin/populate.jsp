<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.jstakun.lm.server.config.ConfigurationManager" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Populate config</title>
</head>
<body>
<%
   ConfigurationManager.populateConfig();
   request.setAttribute(ConfigurationManager.CONFIG, ConfigurationManager.getConfiguration());
%>   
 <c:forEach var="item" items="${config}">
    ${item.key}: ${item.value} <br/><br/>
 </c:forEach>   
  <div><BR/><a href="index.jsp">Back</a></div>
</body>
</html>