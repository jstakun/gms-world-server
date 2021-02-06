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
        <title>GMS World Action Confirmation</title>
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
                <p>We have just sent you verification mail to <%= request.getAttribute("email") %>. Your account will be activated after you'll confirm your registration.</p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") != null) {
 %>
                <h3>Account <%= request.getAttribute("login") %>  registered</h3>
                <p>Thank you for your registration. Your account is now registered in GMS World.</p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                <h3>Email address <%= request.getAttribute("email") %>  registered</h3>
                <p>Thank you for your registration. Your email is now registered to Device Locator notifications service.</p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") != null) {
 %>
                <h3>Account <%= request.getAttribute("login") %> unregistered</h3>
                <p>Your account has been successfully unregistered from GMS World.</p>
<%
    } else if (step.equals("3") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                <h3>Email address <%= request.getAttribute("email") %> unregistered</h3>
                <p>Your email address has been successfully unregistered from Device Locator notifications service.</p>
<%
    } else if (step.equals("5") && request.getAttribute("secret")  != null  && request.getAttribute("login")  != null) {
 %>
                <h3>Account unregistration confirmation</h3>
                <form action="/verify.do">
                    <p>
  						<label for="login">Enter your account login:</label><br/>
  						<input type="text" id="login" name="k"/>
  						<input type="hidden" name="se" value="<%= request.getAttribute("secret") %>"/>
  						<input type="hidden" name="u" value="1"/>
  					</p>
  					<p>
  						<input type="submit" value="Submit"/>
  					</p>
				</form> 
<%
    } else if (step.equals("5") && request.getAttribute("secret") != null  && request.getAttribute("email")  != null) {
 %>
                <h3>Email unregistration confirmation</h3>
                <form action="/verify.do">
                    <p>
  						<label for="email">Enter your email address:</label><br/>
  						<input type="text" id="email" name="k"/>
  						<input type="hidden" name="sc" value="<%= request.getAttribute("secret") %>"/>
  						<input type="hidden" name="u" value="1"/>
  					</p>
  					<p>
  						<input type="submit" value="Submit"/>
  					</p>
				</form> 
 <%
    } else {
 %>
                <h3>Internal error</h3>
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
