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
    	
    	<h2>Device Locator commands</h2>
    	
    	<p></p>
    	
    	In order to get some commands to work on your device you'll need to grant number of permissions in the application.
    	Starting from Device Locator 0.4 in addition to SMS commands you can send cloud commands directly from Device Locator to other device with Device Locator installed using our cloud messaging system.  
    	In order to execute command on remote device with Device Locator installed you need to send one of following commands using SMS, <a href="https://telegram.org/">Telegram Messenger</a> or Device Locator:<br/><br/>
    	
    	Device Locator: Go to Your devices card and click on device you want to send the command<br/> 
    	SMS to device: <b>&lt;command&gt; &lt;security pin&gt; &lt;optional parameters&gt;</b><br/>
    	Telegram Messenger to @device_locator_bot: <b>&lt;command&gt;  &lt;security pin&gt; &lt;device id&gt; -p &lt;optional parameters&gt;</b><br/>
    	Telegram Messenger to @device_locator_bot: <b>&lt;command&gt;  &lt;security pin&gt; &lt;device name&gt; &lt;device username&gt; -p &lt;optional parameters&gt;</b><br/><br/>
    	
    	<b>Security PIN</b> is set in Device Locator at Security PIN card<br/>
    	<b>Device name</b> - is set in Device Locator at Device manager card<br/>
		<b>Device username</b> - is set in Device Locator at Device manager card<br/>
		<b>Device id</b> - is set in Device Locator at Device manager card<br/><br/>
    	
    	<b>Commands</b> available in Device Locator:
    	
    	<ul>
    	   <li><a id="locate"><b>Locate</b></a> - send current device GPS location. Requires Location permission granted.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Locatedl 1234</b><br/>
    	   Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Locate 1234 1234567890123456</b><br/>
    	   You can also use this command to enable periodical execution of locate command on the device. Parameter value from 1 to 24 set command execution interval in hours, 0 - disable command execution.<br/> 
    	   SMS Example (assuming your security pin is 1234 and you want to execute command every 12 hours): <b>Locatedl 1234 12</b><br/>
    	   Telegram Messenger Example (assuming your security pin is 1234, device id is 1234567890123456 and you want to execute command every 12 hours): <b>Locate 1234 1234567890123456 -p 12</b><br/>
    	   </li>
    	   <li><a id="hello"><b>Hello</b></a> - send hello message to check connectivity with the device. Requires Device Locator version 0.4-22. In previous versions use Ping command instead of Hello.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Hellodl 1234</b><br/>
    	   Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Hello 1234 1234567890123456</b></li>  
    	   <li><a id="ring"><b>Ring</b></a> - start playing ring tone on the device. If your ring tone is stored on device storage requires Storage permission granted.<br/><font color="red">Starting from version 0.4-49 use ringoff command to stop playing ring tone. In previous versions send ring command again to stop playing ring tone.</font><br/>
    	   SMS Example (assuming your security pin is 1234): <b>Ringdl 1234</b><br/>  
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Ring 1234 1234567890123456</b></li>  
    	   <li><a id="ringoff"><b>Ringoff</b></a> - stop playing ring tone on the device.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Ringoffdl 1234</b><br/>  
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Ringoff 1234 1234567890123456</b></li>  
    	   <li><a id="photo"><b>Photo</b></a> -  take photo with front camera. You'll receive link to photo image. Requires enabled failed login service and granted Camera permission.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Photodl 1234</b><br/>
    	   Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Photo 1234 1234567890123456</b></li>  
    	   <li><a id="lock"><b>Lock</b></a> -  lock device screen. Requires Lock Screen permission granted.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Lockdl 1234</b><br/>
    	   Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Lock 1234 1234567890123456</b></li>  
    	   <li><a id="call"><b>Call</b></a> -  call sender. Device should initiate phone call to SMS sender number or number specified as command parameter. Requires Phone permission granted.<br/>
           SMS Example (assuming your security pin is 1234): <b>Calldl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234, device id is 1234567890123456, call to number is 123456789): <b>Call 1234 1234567890123456 -p 123456789</b></li>  
    	   <li><a id="start"><b>Start</b></a> - start device location tracking and location recoding. Requires Location permission granted.<br/>
           SMS Example (assuming your security pin is 1234): <b>Startdl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Start 1234 1234567890123456</b></li>  
           <li><a id="perimeter"><b>Perimeter</b></a> - start device location tracking and receive notifications when device is within specific perimeter from you. Requires Location permission granted.<br/>
           SMS Example (assuming your security pin is 1234, and perimeter is 500 meters): <b>Startdl 1234 500</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234, device id is 1234567890123456 and perimeter is 500 meters): <b>Start 1234 1234567890123456 -p 500</b></li>  
           <li><a id="stop"><b>Stop</b></a> - stop device location tracking.<br/>
           SMS Example (assuming your security pin is 1234): <b>Stopdl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Stop 1234 1234567890123456</b></li>  
           <li><a id="mute"><b>Mute</b></a> - mute device audio. Requires Don't Disturb permission on some devices with Android 6+<br/>
           SMS Example (assuming your security pin is 1234): <b>Mutedl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Mute 1234 1234567890123456</b></li>  
           <li><a id="unmute"><b>Unmute</b></a> - unmute device audio.<br/>
           SMS Example (assuming your security pin is  1234): <b>Unmutedl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Unmute 1234 1234567890123456</b></li>  
           <li><a id="reset"><b>Reset</b></a> - reset device to factory defaults and wipe all data and applications. Requires Device Reset permission granted.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Resetdl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Reset 1234 1234567890123456</b></li>  
    	   <li><a id="resume"><b>Resume</b></a> - resume or start device location tracking and location recoding. Requires Location permission granted.<br/>
           SMS Example (assuming your security pin is 1234): <b>Resumedl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Resume 1234 1234567890123456</b></li>  
           <li><a id="route"><b>Route</b></a> - save currently recorded locations route to our backend. You'll receive link to route map.<br/>
           SMS Example (assuming your security pin is 1234): <b>Routedl 1234</b><br/> 
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Route 1234 1234567890123456</b></li>  
    	   <li><a id="config"><b>Config</b></a> - change Device Locator configuration.<br/>
           This command accepts following parameters: <b>lm:on</b> to enable Acknowledge Location notification message, <b>lm:off</b> to disable Acknowledge Location notification message, <b>gpsm:on</b> to enable GPS Location notification message, <b>gpsm:off</b> to disable GPS Location notification message, <b>mapm:on</b> to enable Map link Location notification message, <b>mapm:off</b> to disable Map link Location notification message, 
           <b>gpsb:on</b> and <b>gpsh:off</b> to set balanced GPS accuracy for device location tracking service, <b>gpsh:on</b> and <b>gpsb:off</b> to set high GPS accuracy for device location tracking service, <b>nt:on</b> to send test notification to registered notifiers. You could set all parameters separately or together. <br/>
           SMS Example (assuming your security pin is 1234, and you want to enable Acknowledge Location messages and Map link Location notification message): <b>Configdl 1234 lm:on mapm:on</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234, assuming your security pin is 1234, you want to enable Acknowledge Location messages and Map link Location notification message and device id is 1234567890123456): <b>Config 1234 1234567890123456 -p lm:on mapm:on</b></li>
           <li><a id="radius"><b>Radius</b></a> - change device tracking service radius in meters. Every time device will move outside the area within the radius you'll be notified according to your Notification settings.<br/>
           SMS Example (assuming your security pin is 1234, and radius is 500 meters): <b>Radiusdl 1234 500</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234, device id is 1234567890123456 and radius is 500 meters): <b>Radius 1234 1234567890123456 -p 500</b></li>  
           <li><a id="notify"><b>Nofity</b></a> - set or change who should be notified by location tracking service.<br/>
           SMS Example (assuming your security pin is 1234, phone number is  123456789, email address is name@domain.com and Telegram chat id is 987654321): <b>Notifydl 1234 p:123456789 m:name@domain.com t:987654321</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234, phone number is  123456789, email address is name@domain.com and Telegram chat id is 987654321 and device id is 1234567890123456): <b>Notify 1234 1234567890123456 -p p:123456789 m:name@domain.com t:987654321</b></li>  
           <li><a id="screen"><b>Screen</b></a> - start tracking device screen wake up and sleep events.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Screendl 1234</b><br/>  
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Screen 1234 1234567890123456</b></li>  
    	   <li><a id="screenoff"><b>Screenoff</b></a> - stop tracking device screen wake up and sleep events.<br/>
    	   SMS Example (assuming your security pin is 1234): <b>Screenoffdl 1234</b><br/>  
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>Screenoff 1234 1234567890123456</b></li>  
           <li><a id="about"><b>About</b></a> - send Device Locator version information.<br/>
           SMS Example (assuming your security pin is 1234): <b>Aboutdl 1234</b><br/>
           Telegram Messenger Example (assuming your security pin is 1234 and device id is 1234567890123456): <b>About 1234 1234567890123456</b></li>    
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
