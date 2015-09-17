<%-- 
    Document   : archive.jsp
    Created on : 2010-12-17, 17:41:58
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils,
         com.jstakun.lm.server.persistence.Landmark,
         com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils,
         com.jstakun.lm.server.utils.HtmlUtils,
         org.ocpsoft.prettytime.PrettyTime,
         net.gmsworld.server.utils.UrlUtils,
         org.apache.commons.lang.StringUtils,
         net.gmsworld.server.utils.DateUtils,
         java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- content-outer -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <title>Archives</title>
        <%@ include file="/WEB-INF/jspf/head.jspf" %>
    </head>

    <body>

        <jsp:include page="/WEB-INF/jspf/header.jsp">
            <jsp:param name="current" value="archive" />
        </jsp:include>

        <div id="content-wrap" class="clear" >

            <!-- content -->
            <div id="content">

                <!-- main -->
                <div id="main">

                    <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                        <jsp:param name="page" value="archive"/>
                    </jsp:include>

                    <%
                    	String month = (String)request.getAttribute("month");
                        if (month == null) {
                             month = "";
                        }
                    %>
                    <h3>Archives <%=month%></h3>
                    <ul class="archive">
                        <%
                        	if (request.getAttribute("landmarkList") != null) {
                            	List<Landmark> landmarkList = (List<Landmark>) request.getAttribute("landmarkList");
                              	PrettyTime prettyTime = new PrettyTime(request.getLocale());
                              	for (Landmark landmark : landmarkList) {
                        %>
                        <li>
                            <div class="post-title"><a href="<%=response.encodeURL("/showLandmark/" + landmark.getId())%>"><%= landmark.getName() %></a></div>
                            <div class="post-details">
                            	<%= HtmlUtils.getLandmarkDesc(landmark, request.getLocale()) %>
                            </div>
                        </li>
                        <%
                                }
                            }
                        %>
                    </ul>
                    <jsp:include page="/WEB-INF/jspf/navigation.jsp">
                        <jsp:param name="page" value="archive"/>
                    </jsp:include>

                    <%@ include file="/WEB-INF/jspf/ad_medium_baner.jspf" %>
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
