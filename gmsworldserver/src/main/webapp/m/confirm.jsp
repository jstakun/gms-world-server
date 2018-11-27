<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World Action Confirmation</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
</head>

<body>
 <div class="wrap">

    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
  <%
    String step = request.getParameter("step");
    if (step == null || step.length() != 1) {
       step = "0";
    }

    if (step.equals("1")) {
 %>
                <h3>Verification in progress...</h3>
                <p>We have just sent you verification mail to <%= request.getAttribute("email") %>. Your account will be activated after you'll confirm your registration.</p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") != null) {
 %>
                <h3>Account <%= request.getAttribute("login") %>  registered</h3>
                <p>Thank you for your registration. Your account is now registered in GMS World.</p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                <h3>Email address <%= request.getAttribute("email") %>  registered</h3>
                <p>Thank you for your registration. Your email is now registered to Device Locator notifications service.</p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") != null) {
 %>
                <h3>Account <%= request.getAttribute("login") %> unregistered</h3>
                <p>Your account has been successfully unregistered from GMS World.</p>
<%
    } else if (step.equals("3") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                <h3>Email address <%= request.getAttribute("email") %> unregistered</h3>
                <p>Your email address has been successfully unregistered from Device Locator notifications service.</p>
 <%
    } else if (step.equals("4") && request.getAttribute("login") != null) {
 %>
    	                <h3>User <%= request.getAttribute("login") %> password reset</h3>
    	                <p>Your password has been successfully reset.</p>
 <%
    } else {
 %>
                <h3>Internal error</h3>
                <p>Oops! Something went wrong. Please try again.</p>
 <%
    }
 %>   	</article>
    	
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
