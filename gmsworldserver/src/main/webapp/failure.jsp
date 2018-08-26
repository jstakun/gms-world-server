<%-- 
    Document   : failure
    Created on : 2010-12-18, 15:02:48
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>GMS World Action Failure</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>
       

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
<%
    String step = request.getParameter("step");
    if (step == null || step.length() != 1) {
       step = "0";
    }

    if (step.equals("1")) {
 %>
                    <h3>Account registration failed</h3>
                    <p><a href="register.jsp">Please try again</a></p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") != null) {
 %>
                    <h3>Account <%= request.getAttribute("login") %> verification failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account verification failed">System Administrator</a></p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                    <h3>Email <%= request.getAttribute("email") %> verification failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Email verification failed">System Administrator</a></p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") != null) {
 %>
                    <h3>Account <%= request.getAttribute("login") %> unregister failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account unregister failed">System Administrator</a></p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                    <h3>Email <%= request.getAttribute("email") %> unregister failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account unregister failed">System Administrator</a></p>
 <%
    } else {
 %>
                    <h3>Unregister error</h3>
                     <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account unregister failed">System Administrator</a></p>
 <%
    }
 %>
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
