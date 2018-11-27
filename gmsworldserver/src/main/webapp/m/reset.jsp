<%@page contentType="text/html" pageEncoding="utf-8"%>
<%@ page import="net.gmsworld.server.config.Commons" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<!DOCTYPE html>
<html>

<head>
  <meta charset="utf-8" />  
  <title>GMS World - User Password Reset</title>
  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0" /> 
  <link rel="stylesheet" media="all" href="/style.css" type="text/css">
  <%@ include file="/WEB-INF/jspf/head_small.jspf" %>
  <script type="text/javascript">
    function ClearPassword(){
      document.userForm.password.value = "";
      document.userForm.repassword.value = "";
    }
  </script>
  <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>

<body onLoad="ClearPassword()">
 <div class="wrap">
    <%@ include file="/WEB-INF/jspf/header_mobile.jspf" %>
       
    <div class="content">
    	<article>
    	
    	<h3 class="underline">GMS World User Password Reset</h3>
                    <html:form action="/m/reset" method="post">
                        <p>
                          <strong>
                                <html:messages id="errors">
                                    <a href="#"><bean:write name="errors" /></a>
                                    <br/>
                                </html:messages>
                           </strong>
                        </p>
                        <p>
                            <label for="password">Password <span class="red">*</span></label><br />
                            <html:password property="password"/>
                        </p>
                        <p>
                            <label for="repassword">Retype password <span class="red">*</span></label><br />
                            <html:password property="repassword"/>
                        </p>
                        <p>
                            <html:hidden property="login"/>
                        	<html:hidden property="email"/>
                            <html:hidden property="action"/>
                        </p>
                        <p>
  							<div class="g-recaptcha" data-sitekey="<%=  Commons.getProperty(Commons.Property.RECAPTCHA_PUBLIC_KEY) %>"></div>
                        </p>

                        <p>
                            <html:submit/>
                        </p>
                        <p><a href="#">*) Required</a></p>

                    </html:form>
        
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
