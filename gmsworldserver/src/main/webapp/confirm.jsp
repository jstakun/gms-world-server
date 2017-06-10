<%-- 
    Document   : confirm
    Created on : 2010-12-18, 15:02:35
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Landmark Manager Action Confirmation</title>
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
                <h3>Verification in progress...</h3>
                <p>Verification mail has been sent to you. Your account will be activated after you'll confirm your registration.</p>
 <%
    } else if (step.equals("2")) {
 %>
                <h3>Account/Email Registration Confirmation</h3>
                <p>Thank you for your registration. Your account/email is now active.</p>
 <%
    } else if (step.equals("3")) {
 %>
                <h3>Account Unregistration Confirmation</h3>
                <p>Your account has been successfully unregistered.</p>
 <%
    } else {
 %>
                <h3>Account Action Error</h3>
                <p>Oops! Something went wrong. Please try again.</p>
 <%
    }
 %>

                    <!-- main -->
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jsp" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

       <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>
