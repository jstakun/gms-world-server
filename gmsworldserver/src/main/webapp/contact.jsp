<%-- 
    Document   : contact
    Created on : 2010-12-22, 18:44:11
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="net.gmsworld.server.config.Commons" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Contact Page</title>
        <script src="https://www.google.com/recaptcha/api.js" async defer></script>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <h3>Contact Form</h3>



                    <html:form action="/contact" method="post">

                        <p class="no-border"><strong>
                                <%
                                            if (request.getAttribute("status") != null) {
                                                if (request.getAttribute("status").equals("success")) {
                                %>
                                Message sent successfully!
                                <%                   } else if (request.getAttribute("status").equals("failed")) {
                                %>
                                Failed to send the message!
                                <%                    }
                                                } 
                                %>
                                <br />
                                <html:messages id="errors">
                                    <bean:write name="errors" />
                                    <br />
                                </html:messages>
                            </strong></p>

                        <p>
                            <label for="subject">Subject</label><br />
                            <html:text property="subject" size="32" tabindex="1"/>

                        </p>

                        <p>
                            <label for="name">Your Name</label><br />
                            <html:text property="name" size="32" tabindex="2" />
                        </p>

                        <p>
                            <label for="email">Your Email Address</label><br />
                            <html:text property="email" size="32" tabindex="3" />*
                        </p>
                        
                        <p>
                            <label for="message">Your Message</label><br />
                            <html:textarea property="message" rows="10" cols="48" tabindex="4"></html:textarea>*
                        </p>
                        
                        <p>
  							<div class="g-recaptcha" data-sitekey="<%= Commons.RECAPTCHA_PUBLIC_KEY %>"></div>
                        </p>
                        
                        <p class="no-border">
                            <html:submit value="Submit"  styleClass="button" tabindex="5" />
                            <html:reset value="Reset"  styleClass="button" tabindex="6" />
                        </p>
                        <p class="no-border"><strong>*) Required</strong></p>

                    </html:form>

                    <!-- /main -->
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
