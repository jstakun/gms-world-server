<%-- 
    Document   : template
    Created on : 2010-12-19, 10:43:11
    Author     : jstakun.appspot@gmail.com
    Pushed to GitHub
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Blogeo - Revolutionery Geolocation Blog</title>
         <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp"/>


        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                <h3>Welcome to Blogeo</h3>


                <p><b>Blogeo</b> is revolutionery geolocation blog with allows you to send post from your mobile phone including your
                geolocation position.</p>

                <p>
                    Start your <b>Blogeo</b> now!
                </p>
                <p>
                    It is really easy:
                    <ol type="1">
                        <li><a href="http://www.gms-world.net/register.jsp">Register at <b>Blogeo</b></a> or use your <b>Facebook</b>, <b>LinkedIn</b>, <b>Twitter</b>, <b>Google</b> or <b>Foursquare</b> account.</li>
                        <li><a href="http://www.gms-world.net/download.jsp">Download blogging application for your smartphone</a></li>
                        <li>Start sending posts to your <b>Blogeo</b>. It will be available at: <code>http://www.gms-world.net/blogeo/username</code></li>
                    </ol>
                </p>

                <p>
                    Sounds interesting? Please check <a href="http://www.gms-world.net/blogeo/jstakun">our sample blog</a>.
                </p>
                    <!-- /main -->
                </div>
                <%@ include file="/WEB-INF/jspf/sidebar.jspf" %>
                <!-- content -->
            </div>
            <!-- /content-out -->
        </div>

       <jsp:include page="/WEB-INF/jspf/footer.jsp" />

    </body>
</html>