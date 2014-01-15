<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; utf-8"/>
    <title>Create Landmark Page</title>
  </head>
  <body>
      <h2>Create Landmark</h2>
    <html:form action="/admin/createLandmark" method="post">
      <table cellspacing="2" cellpadding="3" border="1" width="50%">
        <tr>
          <th>
            Name
          </th>
          <td width="50%">
            <html:text property="name"/>
          </td>
        </tr><tr>
          <th>
            Description
          </th>
          <td width="50%">
            <html:textarea property="description"/>
          </td>
        </tr><tr>
          <th>
            Latitude
          </th>
          <td width="50%">
            <html:text property="latitude"/>
          </td>
        </tr><tr>
          <th>
            Longitude
          </th>
          <td width="50%">
            <html:text property="longitude"/>
          </td>
        </tr><tr>
          <th>
            Created By
          </th>
          <td width="50%">
            <html:text property="createdBy"/>
          </td>
        </tr><tr>
          <th>
            Validity Date
          </th>
          <td width="50%">
            <html:text property="validityDate"/>
          </td>
        </tr><tr>
          <th>
            Layer
          </th>
          <td width="50%">
            <html:select property="layer">
                <html:optionsCollection property="layers"/>
            </html:select>
          </td>
        </tr><tr>
          <th>
            &nbsp;
          </th>
          <td width="50%">
            <html:submit/>
          </td>
        </tr>
      </table>
    </html:form>
  </body>
</html>