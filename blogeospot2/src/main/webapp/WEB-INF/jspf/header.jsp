<%-- any content can be specified here e.g.: --%>
<%@ page pageEncoding="utf-8" %>
<!--header -->
<div id="header-wrap">
    <div id="header">

        <a name="top"></a>

        <h1 id="logo-text"><a href="/index.jsp" title="">Blogeo</a></h1>
        <p id="slogan">Revolutionery GeoLocation Blog... </p>

<%
   String current = request.getParameter("current");
   if (current == null)
      current = "";
%>
        <div  id="nav">
            <ul>
                <li<%= current.equals("home") ? " id=\"current\"" : "" %>><a href="/index.jsp">Home</a></li>
                <li<%= current.equals("register") ? " id=\"current\"" : "" %>><a href="http://www.gms-world.net/register.jsp">Register</a></li>
                <li><a href="http://blog.gms-world.net">Blog</a></li>
                <li<%= current.equals("about") ? " id=\"current\"" : "" %>><a href="http://www.gms-world.net/about.jsp">About</a></li>
            </ul>
        </div>

        <!--/header-->
    </div>
</div>
