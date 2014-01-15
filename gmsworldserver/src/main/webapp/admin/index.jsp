<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=windows-1250"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=windows-1250"/>
    <script type="text/javascript"> function confirm_action() {   return confirm('Are you sure?'); } </script>
    <title>GMS World Administrator Console</title>
  </head>
  <body>
    <h2>
      Welcome to GMS World Administrator Console
    </h2>
    <h4>
      Available actions:
    </h4>
    <h4>
      <a href="listLandmarks.do" onclick="return confirm_action()">List Landmarks</a>
    </h4>
    <!--h4>
      <a href="deleteAllLandmarks.do">Delete All Landmarks</a>
    </h4-->
    <h4>
      <a href="createLandmark.jsp">Create Landmark</a>
    </h4>
    <h4>
      <a href="createLayer.jsp">Create Layer</a>
    </h4>
    <h4>
      Available tasks:
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=purge&entity=log" onclick="return confirm_action()">Purge service log data</a>
    </h4>
     <h4>
      <a href="/admin/taskExecute?action=purge&entity=screenshot" onclick="return confirm_action()">Purge screenshot data</a>
    </h4>
    <h4>
      <a href="/admin/listHotelFiles.do">Load Hotels</a>
    </h4>
    <!--h4>
      <a href="/admin/hotelLoader?file=x" onclick="return confirm_action()">Update Hotels</a>
    </h4-->
    <h4>
      <a href="/admin/taskExecute?action=purge&entity=hotel" onclick="return confirm_action()">Purge hotels</a>
    </h4>
    <!--h4>
      <a href="/admin/taskExecute?action=geocells&entity=hotel" onclick="return confirm_action()">GeoCells Hotels in 5 mins</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=geocells&entity=landmark" onclick="return confirm_action()">GeoCells Landmarks in 5 mins</a>
    </h4>
    <!--h4>
      <a href="/admin/taskExecute?action=emailing" onclick="return confirm_action()">Emailing</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=personalize" onclick="return confirm_action()">Personalization API call task</a>
    </h4-->
    <h4>
      Reports:
    </h4>
    <h4>
      <a href="/admin/serviceLogAnalytics?forDays=7" onclick="return confirm_action()">Services last week</a>
    </h4>
    <h4>
      <a href="/admin/serviceLogAnalytics?forDays=30" onclick="return confirm_action()">Services last month</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@fb,@fs,@gg,@tw,@ln,@gl,@gw&resultProperty=username&forDays=7" onclick="return confirm_action()">Social users last week</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@fb,@fs,@gg,@tw,@ln,@gl,@gw&resultProperty=username&forDays=31" onclick="return confirm_action()">Social users last month</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@fb,@fs,@gg,@tw,@ln,@gl,@gw&resultProperty=username" onclick="return confirm_action()">All social users</a>
    </h4>
    <!--h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@fb&resultProperty=username" onclick="return confirm_action()">Facebook Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@fs&resultProperty=username" onclick="return confirm_action()">Foursquare Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@gg&resultProperty=username" onclick="return confirm_action()">Google Plus Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@tw&resultProperty=username" onclick="return confirm_action()">Twitter Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@ln&resultProperty=username" onclick="return confirm_action()">LinkedIn Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@gl&resultProperty=username" onclick="return confirm_action()">Google Blogger Users</a>
    </h4>
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=username&pattern=@gw&resultProperty=username" onclick="return confirm_action()">Gowalla Users</a>
    </h4-->
    <h4>
      <a href="/admin/taskExecute?action=filter&filterProperty=email&pattern=@&resultProperty=email" onclick="return confirm_action()">Users emails</a>
    </h4>
    <!--h4>
      <a href="/mapreduce/status" onclick="return confirm_action()">MapReduce</a>
    </h4-->
    <h4>
      <a href="https://apps.admob.com/" onclick="return confirm_action()">AdMob</a>
    </h4>
    <h4>
      <a href="https://www.google.com/adsense" onclick="return confirm_action()">AdSence</a>
    </h4>
  </body>
</html>