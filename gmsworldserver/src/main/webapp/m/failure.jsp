<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World Action Failure</title>
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
                    <h3>Account registration failed</h3>
                    <p><a href="register.jsp">Please try again</a></p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") != null) {
 %>
                    <h3>Account <%= request.getAttribute("login") %> verification failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account verification failed">System Administrator</a></p>
 <%
    } else if (step.equals("2") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                    <h3>Email <%= request.getAttribute("email") %> verification failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Email verification failed">System Administrator</a></p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") != null) {
 %>
                    <h3>Account <%= request.getAttribute("login") %> unregister failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account unregister failed">System Administrator</a></p>
 <%
    } else if (step.equals("3") && request.getAttribute("login") == null && request.getAttribute("email") != null) {
 %>
                    <h3>Email <%= request.getAttribute("email") %> unregister failed</h3>
                    <p>Please try again or contact <a href="mailto:support@gms-world.net?subject=Account unregister failed">System Administrator</a></p>
 <%
    } else {
 %>
                    <h3>Internal error</h3>
                    <p>Oops! Something went wrong. Please try again.</p>
 <%
    }
 %>
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
