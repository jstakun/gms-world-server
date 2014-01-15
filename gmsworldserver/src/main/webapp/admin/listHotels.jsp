<%-- 
    Document   : listHotels
    Created on : 2012-08-29, 15:21:33
    Author     : jstakun
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type="text/javascript"> function confirm_action() {   return confirm('Are you sure?'); } </script>
        <title>List Hotels</title>
    </head>
    <body>
        Load files: <br/><br/>
        <table>
            <c:forEach items="${files}" var="file">
                <tr>
                    <c:url var="nextLink" value="/admin/hotelLoader">
                        <c:param name="file" value="${file.name}"/>
                    </c:url>
                    <td><a href="<c:out value="${nextLink}" escapeXml="true" />" onclick="return confirm_action()"><c:out value="${file.name}" /></a></td>
                </tr>
            </c:forEach>
        </table>
        <div><BR/><a href="index.jsp">Back</a></div>
    </body>
</html>
