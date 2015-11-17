<%-- any content can be specified here e.g.: --%>
<%@ page pageEncoding="utf-8" %>
<!--header -->
<div id="header-wrap">
    <div id="header">

        <a name="top"></a>
<% String mode = request.getParameter("mode") != null ? request.getParameter("mode") : "";
   String display = request.getParameter("display") != null ? request.getParameter("display") : "";
   if (mode.equals("blogeo"))
   {
%>
        <h1 id="logo-text"><a href="/index.jsp" title="">Blogeo</a></h1>
        <p id="slogan">Revolutionery GeoLocation Blog!</p>
<% } else { %>
        <h1 id="logo-text"><a href="/index.jsp" title="">GMS World</a></h1>
        <p id="slogan">Discover interesting places anywhere!</p>
<% }
   String current = request.getParameter("current");
   if (current == null)
      current = "";
   if (display.length()==0) {
%>
        <div  id="nav">
            <ul>
                <li<%= current.equals("hotels") ? " id=\"current\"" : "" %>><a href="/hotels">Hotels</a></li>
                <li<%= current.equals("landmarks") ? " id=\"current\"" : "" %>><a href="/landmarks">Landmarks</a></li>
                <li<%= current.equals("home") ? " id=\"current\"" : "" %>><a href="/index.jsp">Home</a></li>
                <li<%= current.equals("download") ? " id=\"current\"" : "" %>><a href="/download.jsp">Download</a></li>
                <li<%= current.equals("register") ? " id=\"current\"" : "" %>><a href="/register.jsp">Register</a></li>
                <li><a href="http://blog.gms-world.net">Blog</a></li>
                <li<%= current.equals("archive") ? " id=\"current\"" : "" %>><a href="/archive.do">Archives</a></li>
                <!-- li<%= current.equals("demo") ? " id=\"current\"" : "" %>><a href="/demo/run.jsp">Online demo</a></li -->
                <li<%= current.equals("about") ? " id=\"current\"" : "" %>><a href="/about.jsp">About</a></li>
            </ul>
        </div>
<%
   }
%>
        <!--/header-->
    </div>
</div>
