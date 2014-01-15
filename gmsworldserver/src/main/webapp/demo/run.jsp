<%-- 
    Document   : run
    Created on : 2010-12-18, 14:31:54
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>

        <title>Landmark Manager Live Demo Page</title>
        <meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
        <meta name="author" content="Erwin Aligam - styleshout.com" />
        <meta name="description" content="Site Description Here" />
        <meta name="keywords" content="keywords, here" />
        <meta name="robots" content="index, follow, noarchive" />
        <meta name="googlebot" content="noarchive" />

        <link rel="stylesheet" type="text/css" media="screen" href="/css/screen.css" />

    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp">
            <jsp:param name="current" value="demo" />
        </jsp:include>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">
                    <h3>Landmark Manager Live Demo</h3><br/>
                    <p align="center">
                    <applet code="org.microemu.applet.Main" width="292" height="618"
                            archive="lib/microemu-javase-applet.jar,../download/j2me/108/LandmarkManager.jar,lib/microemu-device-large.jar,lib/microemu-jsr-75.jar,lib/microemu-jsr-135.jar,lib/openlapi-jsr179.jar">
                        <param name="midlet" value="com.jstakun.lm.ui.LandmarkManagerUIMIDlet"/>
                        <param name="device" value="org/microemu/device/large/device.xml"/>
                        <%
                                    String locale = "0";
                                    if (request.getParameter("locale") != null) {
                                        locale = request.getParameter("locale");
                                    }
                        %>
                        <param name="locale" value="<%= locale%>"/>
                        <%
                                    String mapProvider = "3";
                                    if (request.getParameter("mapProvider") != null) {
                                        mapProvider = request.getParameter("mapProvider");
                                    }
                        %>
                        <param name="mapProvider" value="<%= mapProvider%>"/>
                    </applet>
                    </p>
                    <p>
                        In order to make this applet running in your web browser you need to grant
                        following java permissions:
                    </p>
                    <p>
                      <code>
                            grant codebase &quot;http://www.gms-world.net/-&quot;<br/>
                                { <br/>
                                    &nbsp;&nbsp; permission java.util.PropertyPermission &quot;user.home&quot;,&quot;read&quot;;<br/>
                                    &nbsp;&nbsp; permission java.net.SocketPermission &quot;*&quot;, &quot;connect, resolve&quot;;<br/>
                                    &nbsp;&nbsp; permission java.io.FilePermission &quot;&#36;&#123;user.home&#125;/-&quot;,&quot;read, write&quot;;<br/>
                                    &nbsp;&nbsp; permission java.util.PropertyPermission &quot;microedition.platform&quot;, &quot;read&quot;;<br/>
                                 };
                      </code>
                    </p>
                    <p>
                        The easiest way to do that is to create .java.policy file in your home directory and copy/paste grant statement from above.<br/><br/>
                        For more details please refer to <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/PolicyFiles.html">Java Policy Files Documentation</a>.</p>
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
