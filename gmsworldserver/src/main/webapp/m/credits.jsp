<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Credits</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
    	<h3>GMS World applications contains code and resource from following projects:</h3>
       
        <ul> 
        
        <li><b>ACRA</b><br/>
        Application Crash Reporting for Android Copyright 2010 Emmanuel Astier &amp; Kevin Gaudin.<br/><br/>
        </li>
        
        <li>
        <b>Google AdMob Ads SDK</b><br/>
        Google mobile advertising featuring refined ad formats and streamlined APIs. Copyright 2011 Google Inc. All rights reserved.<br/><br/>
        </li>
        
        <li>
        <b>Apache Commons</b><br/>
        Collection of open source reusable Java components from the Apache/Jakarta community.<br/><br/>
        </li>
 
        <li>
        <b>Bouncy Castle</b><br/>
        Crypto APIs for Java. Copyright (c) 2000 - 2011 The Legion Of The Bouncy Castle (http://www.bouncycastle.org).<br/><br/>
        </li>

        <li>
        <b>Guava</b><br/>
        Google Core Libraries for Java 1.5+. Copyright 2011 Google Inc. All rights reserved.<br/><br/>
        </li>

        <li>
        <b>JSR-275</b><br/>
        Measurements and Units Specification. Copyright (c) 2010 JScience.org.<br/><br/>
        </li>

        <li>
        <b>JSR-305</b><br/>
        Annotations for Software Defect Detection in Java. Copyright 2011 Google Inc. All rights reserved.<br/><br/>
        </li>

        <li>
        <b>kXML</b><br/>
        Small XML pull parser. Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany.<br/><br/>
        </li>
        
        <li>
        <b>Google Analytics Mobile SDK</b><br/>
        Google Analytics Mobile API for Android-based applications. Copyright 2011 Google Inc. All rights reserved.<br/><br/>
        </li>

        <li>
        <b>OpenLAPI</b><br/>
        Location aware software for mobile devices. Copyright ThinkTank Maths Limited 2006-2008.<br/><br/>
        </li>

        <li>        
        <b>Osmdroid</b><br/>
        OpenStreetMap tools for Android.<br/><br/>
        </li>

        <li>
        <b>Microfloat</b><br/>
        Copyright (C) 2003, 2004 David Clausen.<br/><br/>
        </li>

        <li> 
        <b>ZXing</b><br/>
        Barcode Scanner. Copyright 2008 ZXing authors.<br/><br/>
        </li>
        
        <li>
        <b>Icons and graphics</b><br/>
        <a href="http://www.bergmanicum.com/">BERGMANICUM</a>.
        </li>
        
        </ul>
        License: Apache License 2.0 <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
        
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
