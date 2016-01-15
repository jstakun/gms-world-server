package net.gmsworld.server.utils;

import com.google.gdata.util.common.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class HttpUtils {

    private static final Logger logger = Logger.getLogger(HttpUtils.class.getName());
    private static final int timeoutMs = 30000;
    private static final Map<String, Integer> httpResponseStatuses = new HashMap<String, Integer>();

    public static String processFileRequestWithLocale(URL fileUrl, String locale) throws IOException {
        return processFileRequest(fileUrl, false, null, null, "GET", locale, null, null, null, false);
    }

    public static String processFileRequestWithAuthn(URL fileUrl, String authn) throws IOException {
        return processFileRequest(fileUrl, true, null, authn, "GET", null, null, null, null, false);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String authn, boolean compress) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, "GET", null, null, null, null, compress);
    }

    public static String processFileRequest(URL fileUrl) throws IOException {
        return processFileRequest(fileUrl, false, null, null, "GET", null, null, null, null, false);
    }

    public static String processFileRequest(URL fileUrl, String method, String accept, String content) throws IOException {
        return processFileRequest(fileUrl, false, null, null, method, null, accept, content, null, false);
    }
    
    public static String processFileRequest(URL fileUrl, String method, String accept, String content, String contentType) throws IOException {
        return processFileRequest(fileUrl, false, null, null, method, null, accept, content, contentType, false);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String method, String accept, String urlParams, String authn) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, method, null, accept, urlParams, null, false);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String method, String accept, String urlParams, String contentType, String authn) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, method, null, accept, urlParams, contentType, false);
    }

    private static String processFileRequest(URL fileUrl, boolean authn, String userpassword, String authnOther, String method, String locale, String accept, String content, String contentType, boolean compress) throws IOException {
        InputStream is = null;
        String file = null;
        long start = System.currentTimeMillis();

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            if (authn && userpassword != null) {
                //username : password
                String encodedAuthorization = Base64.encode(userpassword.getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            } else if (authn && authnOther != null) {
                conn.setRequestProperty("Authorization", authnOther);
            }

            if (StringUtils.isNotEmpty(locale)) {
                conn.setRequestProperty("Accept-Language", locale);
            }

            conn.setRequestProperty("Accept-Charset", "utf-8");

            if (StringUtils.isNotEmpty(accept)) {
                conn.setRequestProperty("Accept", accept);
            }
            
            if (content != null) {
                conn.setRequestProperty("Content-Length", Integer.toString(content.getBytes().length));
                //conn.setRequestProperty("Content-Language", "en-US");
                
                if (contentType != null) {
                	conn.setRequestProperty("Content-Type", contentType);
                } else {
                	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                }
                
                if (compress) {
                	conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                }
                
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //Send request
                IOUtils.write(content, conn.getOutputStream());
            } else {
                conn.connect();
            }
            
            int responseCode = conn.getResponseCode();
            httpResponseStatuses.put(fileUrl.toExternalForm(), responseCode);

            if (responseCode == HttpServletResponse.SC_OK) {
                is = conn.getInputStream();   
            } else if (responseCode >= 400 ){
                is = conn.getErrorStream();
                logger.log(Level.SEVERE, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});   
            } else if (responseCode >= 300 && responseCode < 400) {
            	logger.log(Level.WARNING, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});   
            } else if (responseCode > 200) {
            	logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});
            }
            
            if (is != null) {
            	file = IOUtils.toString(is, "UTF-8");
            	int length = file.length();
            	if (length > 0) {
            		logger.log(Level.INFO, "Received " + conn.getContentType() + " document having " + length + " characters");
            	}
            }
            
            logger.log(Level.INFO, "Request processed with status " + responseCode + " in " + (System.currentTimeMillis()-start) + " millis.");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            //httpResponseStatuses.remove(fileUrl.toExternalForm());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        
        return file;
    }

    public static void processImageFileRequest(OutputStream out, String imageUrl) throws IOException {
    	InputStream is = null;
        try {
            URL fileUrl = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpServletResponse.SC_OK) { 
            	if (StringUtils.indexOf(conn.getContentType(), "gzip") > -1) {
    				is = new GZIPInputStream(conn.getInputStream());
    			} else {
    				is = conn.getInputStream();
    			}
                byte[] buf = new byte[1024];
                int count = 0, total = 0;
                while ((count = is.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                    total += count;
                }
                logger.log(Level.INFO, "Received " + conn.getContentType() + " image having " + total + " bytes");
            } else if (responseCode >= 400 ){
                logger.log(Level.SEVERE, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});   
            } else if (responseCode >= 300 && responseCode < 400) {
            	logger.log(Level.WARNING, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});   
            } else if (responseCode > 200) {
            	logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString()});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (is != null) {
                is.close();
            }
            if (out != null) {
            	out.close();
            }
        }
    }

    public static boolean isEmptyAny(HttpServletRequest request, String... params) {
        for (String p : params) {
            if (StringUtils.isEmpty(request.getParameter(p))) {
                logger.log(Level.INFO, "Missing required parameter {0}", p);
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEmptyAnyDebug(HttpServletRequest request, String... params) {
        boolean isMissing = false;
    	for (String p : params) {
    		String value = request.getParameter(p);
            if (StringUtils.isEmpty(value)) {
                logger.log(Level.INFO, "Missing required parameter {0}", p);
                isMissing = true;
            } else {
            	logger.log(Level.INFO, "Found parameter {0} : {1}", new Object[] {p, value});
            }
        }
        return isMissing;
    }
    
    public static Integer getResponseCode(String url) {
    	return httpResponseStatuses.remove(url);
    }
}
