<%-- 
    Document   : register
    Created on : 2010-12-18, 13:52:45
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="com.jstakun.lm.server.personalization.ReCaptchaUtils" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Landmark Manager User Registration</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
        <script type="text/javascript">
<!-- //
function ClearPassword(){
    document.userForm.password.value = "";
    document.userForm.repassword.value = "";
}
// -->
</script>

    </head>

    <body onLoad="ClearPassword()">


        <jsp:include page="/WEB-INF/jspf/header.jsp">
            <jsp:param name="current" value="register" />
        </jsp:include>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <h3>User Registration</h3>
                    <html:form action="/register" method="post">
                        <p class="no-border">
                            <strong>
                                <html:messages id="errors">
                                    <bean:write name="errors"/><br/>
                                </html:messages>
                            </strong>
                        </p>
                        <p>
                            <label for="login">Login</label><br />
                            <html:text property="login"/>*
                        </p>
                        <p>
                            <label for="password">Password</label><br />
                            <html:password property="password"/>*
                        </p>
                        <p>
                            <label for="repassword">Retype password</label><br />
                            <html:password property="repassword"/>*
                        </p>
                        <p>
                            <label for="email">Email</label><br />
                            <html:text property="email" size="32"/>*
                        </p>
                        <p>
                            <label for="firstname">First Name</label><br />
                            <html:text property="firstname"/>
                        </p>
                        <p>
                            <label for="lastname">Last Name</label><br />
                            <html:text property="lastname"/>
                        </p>
                        <p>
  							<%= ReCaptchaUtils.getRecaptchaHtml() %>
                        </p>
                        <p class="no-border">
                            <html:submit styleClass="button"/>
                        </p>
                        <p class="no-border"><strong>*) Required</strong></p>

                    </html:form>

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
