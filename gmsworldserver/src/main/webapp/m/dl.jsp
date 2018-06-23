<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Device Locator Commands</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
    	<h3>Device Locator SMS commands</h3>
    	
    	In order to get some commands to work on your device you'll need to grant number of permissions in the application. In order to execute remotely command on Device Locator you need to send following SMS message: 
    	
    	<p><b>&lt;command&gt;&lt;security pin&gt; &lt;optional parameters&gt;</b></p>
    	
    	<b>Security PIN</b> is generated by Device Locator during application installation and you can find it and change it anytime on Security PIN card. Security PIN must be at least 4 digits. <font color="red">Remember to protect your PIN!</font><br/><br/>
    	
    	<b>Commands</b> available in Device Locator:
    	
    	<ul>
    	   <li><b>Locatedl</b> - send current device GPS location. Requires Location permission granted.<br/>Usage: Locatedl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Locatedl1234</b><br/></li>
    	   <li><b>Pingdl</b> - send test message to check connectivity with the device.<br/>Usage: Pingdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Pingdl1234</b><br/></li>  
    	   <li><b>Ringdl</b> - start playing ring tone on the device. If your ring tone is stored on device storage requires Storage permission granted. <font color="red">You can send this command again to stop playing ring tone.</font><br/>Usage: Ringdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Ringdl1234</b><br/></li>  
           <li><b>Calldl</b> -  call sender. Device should initiate phone call to SMS sender number. Requires Phone permission granted.<br/>Usage: Calldl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Calldl1234</b><br/></li>
    	   <li><b>Photodl</b> -  take photo with front camera. You'll receive link to photo image. Requires enabled failed login service and granted Camera permission.<br/>Usage: Photodl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Photodl1234</b><br/></li>
    	   <li><b>Lockdl</b> -  lock device screen. Requires Lock Screen permission granted.<br/>Usage: Lockdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Lockdl1234</b><br/></li>
    	   <li><b>Startdl</b> - start device location tracking and location recoding. Requires Location permission granted.<br/>Usage: Startdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Startdl1234</b><br/></li>
           <li><b>Stopdl</b> - stop device location tracking.<br/>Usage: Stopdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Stopdl1234</b><br/></li>
           <li><b>Resumedl</b> - resume or start device location tracking and location recoding. Requires Location permission granted.<br/>Usage: Resumedl&lt;security pin&gt;.<br/>Example (assuming your security pin is 1234): <b>Resumedl1234</b><br/></li>
           <li><b>Routedl</b> - send currently recorded locations route to the GMS World. You'll receive link to route map.<br/>Usage: Routedl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Routedl1234</b><br/></li> 
           <li><b>Radiusdl</b> - change device tracking service radius in meters. Every time device will move outside the area within the radius you'll be notified according to your Notification settings.<br/>Usage: Radiusdl&lt;security pin&gt; x  where x is number of meters &gt; 0. <br/>Example (assuming your security pin is 1234, and radius is 500 meters): <b>Radiusdl1234 500</b><br/></li>
           <li><b>Mutedl</b> - mute device audio.<br/>Usage: Mutedl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Mutedl1234</b><br/></li>
           <li><b>Normaldl</b> - restore normal device audio setting (turn off  mute).<br/>Usage: Normaldl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Normaldl1234</b><br/></li>
           <li><b>Nofitydl</b> - set or change who should be notified by location tracking service.<br/>Usage: Notifydl&lt;security pin&gt; x  where x is p:y for phone number, m:y for email address or t:y for Telegram chat id. You could set all parameters separately or together. Email address and Telegram chat id are subject for validation by it's owners.<br/>Example (assuming your security pin is 1234, phone number is 123456789, email address is name@domain.com and Telegram chat id is 987654321): <b>Notifydl1234 p:123456789 m:name@domain.com t:987654321</b><br/></li>
           <li><b>Gpshighdl</b> - set high GPS accuracy for device location tracking (this will consume faster device battery, and location will be more accurate).<br/>Usage: Gpshighdl&lt;security pin&gt;<br/>Example (assuming your security pin is 1234): <b>Gpshighdl1234</b><br/></li>
           <li><b>Gpsbalancedl</b> - set balanced GPS accuracy for device location tracking service (this will consume slower device battery and location will be less accurate).<br/>Usage: Gpsbalanceddl&lt;security pin&gt;. <br/>Example (assuming your security pin is 1234): <b>Gpsbalancedl1234</b><br/></li>
    	</ul> 
       
    	</article>
    	
    	<%@ include file="/WEB-INF/jspf/ad_small_baner.jspf" %>
    </div>
    <%@ include file="/WEB-INF/jspf/footer_mobile.jspf" %>
  </div>
 
  <script src="/js/jquery.min.js"></script>
  <script type="text/javascript">
    window.addEventListener("load",function() {
	  // Set a timeout...
	  setTimeout(function(){
	    // Hide the address bar!
	    window.scrollTo(0, 1);
	  }, 0);
	});
    $('.search-box,.menu' ).hide();   
    $('.options li:first-child').click(function(){	
   		$(this).toggleClass('active'); 	
   		$('.search-box').toggle();        			
   		$('.menu').hide();  		
   		$('.options li:last-child').removeClass('active'); 
    });
    $('.options li:last-child').click(function(){
   		$(this).toggleClass('active');      			
   		$('.menu').toggle();  		
   		$('.search-box').hide(); 
   		$('.options li:first-child').removeClass('active'); 		
    });
    $('.content').click(function(){
   		$('.search-box,.menu' ).hide();   
   		$('.options li:last-child, .options li:first-child').removeClass('active');
    });
  </script>
</body>

</html>
