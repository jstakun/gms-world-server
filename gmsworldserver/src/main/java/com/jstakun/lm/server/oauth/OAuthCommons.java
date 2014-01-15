package com.jstakun.lm.server.oauth;

public class OAuthCommons {
     public static String getOAuthSuccessHTML(String title) {
    	 return "<html>" +
    	 		" <head>" +
    	 		"  <title>" + title + "</title>" +
    	 	    " </head>" +
    	 	    " <body>" +
    	 	    "   <h3>Logon successful</h3>" +
                "  <p>Web browser will be closed automatically and you'll be redirected back to <b>Landmark Manager</b>.<br/><br/>" +
                "   If web browser won't close automatically please do it manually and come back to <b>Landmark Manager</b>.</p>" +
    	 	    " </body>" +
    	 	    "</html>";
     }
}
