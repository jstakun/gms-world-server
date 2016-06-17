<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - Landmark Manager application page</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
    	<h3>Choose your provider</h3>
    	
    	<p>
            <a href="https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui" target="_blank">
  				<img alt="Get it on Google Play" title="Get it on Google Play" src="/images/en_generic_rgb_wo_60.png" />
			</a>
        </p>
       
        <p>
            <a href="http://www.amazon.com/gp/product/B004X2NR7O/ref=mas_pm_Landmark_Manager" target="_blank">
            	<img alt="Get it on Amazon AppStore" title="Get it on Amazon AppStore" src="/images/amazon-apps-store.png"/>
            </a>	
        </p>
        
        <p>
            <a href="http://apps.opera.com/badge.php?a=c&v=dark&did=7629&pid=36609" target="_blank">
            	<img alt="Get it on Opera Mobile Store" title="Get in on Opera Mobile Store" src="https://apps.opera.com/badge.php?a=s&v=dark&did=7629&pid=36609" width="173" height="55"/>
            </a>  
        </p>
        
         <p>
            <a href="https://store.yandex.com/" target="_blank">
            	<img alt="Get it on Yandex Mobile Store" title="Get in on Yandex Mobile Store" src="/images/yandex.jpg" width="160" height="52"/>
            </a>  
        </p>
        
        <br/><br/>
        
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
