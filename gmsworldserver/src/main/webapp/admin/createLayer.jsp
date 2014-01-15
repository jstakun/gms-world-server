<%-- 
    Document   : createLayer
    Created on : 2010-09-15, 21:25:37
    Author     : jstakun
--%>

<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Create Layer</title>
    </head>
    <body>
        <h2>Create Layer</h2>
        <html:form action="/admin/createLayer" method="post">
      <table cellspacing="2" cellpadding="3" border="1" width="50%">
        <tr>
          <th>
            Name
          </th>
          <td width="50%">
            <html:text property="name"/>
          </td>
        </tr>
        <tr>
          <th>
            Formatted name
          </th>
          <td width="50%">
            <html:text property="formatted"/>
          </td>
        </tr>
        <tr>
          <th>
            Description
          </th>
          <td width="50%">
            <html:text property="desc"/>
          </td>
        </tr>
        <tr>
          <th>
            &nbsp;
          </th>
          <td width="50%">
            <html:submit/>
          </td>
        </tr>
      </table>
    </html:form>
    <div><br/><a href="index.jsp">Back</a></div>
    </body>
</html>
