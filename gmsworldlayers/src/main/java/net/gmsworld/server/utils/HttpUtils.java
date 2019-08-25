package net.gmsworld.server.utils;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;

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
        return processFileRequest(fileUrl, false, null, null, "GET", locale, null, null, null, false, null, null);
    }

    public static String processFileRequestWithAuthn(URL fileUrl, String authn) throws IOException {
        return processFileRequest(fileUrl, true, null, authn, "GET", null, null, null, null, false, null, null);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String authn, boolean compress) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, "GET", null, null, null, null, compress, null, null);
    }

    public static String processFileRequest(URL fileUrl) throws IOException {
        return processFileRequest(fileUrl, false, null, null, "GET", null, null, null, null, false, null, null);
    }
    
    public static String processFileRequest(URL fileUrl, String headerName, String headerValue) throws IOException {
        return processFileRequest(fileUrl, false, null, null, "GET", null, null, null, null, false, headerName, headerValue);
    }

    public static String processFileRequest(URL fileUrl, String method, String accept, String content) throws IOException {
        return processFileRequest(fileUrl, false, null, null, method, null, accept, content, null, false, null, null);
    }
    
    public static String processFileRequest(URL fileUrl, String method, String accept, String content, String contentType) throws IOException {
        return processFileRequest(fileUrl, false, null, null, method, null, accept, content, contentType, false, null, null);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String method, String accept, String urlParams, String authn) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, method, null, accept, urlParams, null, false, null, null);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String method, String accept, String urlParams, String contentType, String authn) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, method, null, accept, urlParams, contentType, false, null, null);
    }
    
    public static String processFileRequestWithOtherAuthn(URL fileUrl, String method, String accept, String urlParams, String contentType, String authn) throws IOException {
        return processFileRequest(fileUrl, true, null, authn, method, null, accept, urlParams, contentType, false, null, null);
    }
    
    public static String processFileRequestWithBasicAuthn(URL fileUrl, String method, String accept, String urlParams, String contentType, String authn, String headerName, String headerValue) throws IOException {
        return processFileRequest(fileUrl, true, authn, null, method, null, accept, urlParams, contentType, false, headerName, headerValue);
    }

    private static String processFileRequest(URL fileUrl, boolean authn, String userpassword, String authnOther, String method, String locale, String accept, String content, String contentType, boolean compress, String customHeaderName, String customHeaderValue) throws IOException {
        InputStream is = null;
        String file = null;
        long start = System.currentTimeMillis();

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            
            conn.setRequestProperty("User-Agent", "http://www.gms-world.net HTTP client");
            conn.setRequestProperty("Referer", ConfigurationManager.SERVER_URL + Commons.getProperty(Property.REFERER_KEY));
            
            if (authn) {
            	if (StringUtils.isNotEmpty(userpassword)) {
            		//username : password
            		String encodedAuthorization = Base64.encode(userpassword.getBytes());
            		conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            	} else if (StringUtils.isNotEmpty(authnOther)) {
            		conn.setRequestProperty("Authorization", authnOther);
            	}
            }	

            if (StringUtils.isNotEmpty(locale)) {
                conn.setRequestProperty("Accept-Language", locale);
            }

            conn.setRequestProperty("Accept-Charset", "utf-8");
  
            if (StringUtils.isNotEmpty(accept)) {
                conn.setRequestProperty("Accept", accept);
            }
            
            if (StringUtils.isNotEmpty(customHeaderName) && StringUtils.isNotEmpty(customHeaderValue)) {
            	conn.setRequestProperty(customHeaderName, customHeaderValue);
            }
             
            if (StringUtils.isNotEmpty(content)) {
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
                
                //Map<String, List<String>> props = conn.getRequestProperties();
                //logger.log(Level.INFO, props.toString());
                
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //Send request
                IOUtils.write(content, conn.getOutputStream(), Charset.forName("UTF-8"));
            } else {
                conn.connect();
            }
            
            int responseCode = conn.getResponseCode();
            httpResponseStatuses.put(fileUrl.toExternalForm(), responseCode);
            
            final String urlSafe = fileUrl.toString().split("\\?")[0];

            if (responseCode == HttpServletResponse.SC_OK) {
                is = conn.getInputStream(); 
                logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, urlSafe});
            } else if (responseCode >= 400 && responseCode !=  HttpServletResponse.SC_NOT_FOUND){
                is = conn.getErrorStream();
                logger.log(Level.SEVERE, "Received http status code {0} for url {1}", new Object[]{responseCode, urlSafe});   
            } else if (responseCode >= 300) {
            	logger.log(Level.WARNING, "Received http status code {0} for url {1}", new Object[]{responseCode, urlSafe});   
            } else if (responseCode > 200) {
            	logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, urlSafe});
            }
            
            if (responseCode != HttpServletResponse.SC_NO_CONTENT && is != null) {
            	file = IOUtils.toString(is, "UTF-8");
            	int length = file.length();
            	if (length > 0) {
            		logger.log(Level.INFO, "Received " + conn.getContentType() + " document containing " + length + " characters");
            	}
            	//logger.log(Level.INFO, file);
            }
            
            logger.log(Level.INFO, "Request with status " + responseCode + " processed in " + (System.currentTimeMillis()-start) + " millis.");
            
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

    public static int processImageFileRequest(OutputStream out, String imageUrl) throws IOException {
    	InputStream is = null;
    	int total = 0;
        try {
            URL fileUrl = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("User-Agent", "GMS World HTTP client " + ConfigurationManager.SERVER_URL);
            conn.setRequestProperty("Referer", ConfigurationManager.SERVER_URL + Commons.getProperty(Property.REFERER_KEY));

            conn.connect();
            int responseCode = conn.getResponseCode();
            httpResponseStatuses.put(fileUrl.toExternalForm(), responseCode);

            if (responseCode == HttpServletResponse.SC_OK) { 
            	if (StringUtils.indexOf(conn.getContentType(), "deflate") > -1) {
    				is = new GZIPInputStream(conn.getInputStream());
    			} else {
    				is = conn.getInputStream();
    			}
                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = is.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                    total += count;
                }
                logger.log(Level.INFO, "Received " + conn.getContentType() + " image having " + total + " bytes");
            } else if (responseCode >= 400 ){
                logger.log(Level.SEVERE, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});   
            } else if (responseCode >= 300 && responseCode < 400) {
            	logger.log(Level.WARNING, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});   
            } else if (responseCode > 200) {
            	logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            total = -1;
        } finally {
            if (is != null) {
                is.close();
            }
            if (out != null) {
            	out.close();
            }
        }
        
        return total;
    }
    
    public static List<ExtendedLandmark> loadLandmarksList(String landmarksUrl, String token, String scope) throws IOException {
    	ObjectInputStream ois = null;
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	
    	try {
            URL fileUrl = new URL(landmarksUrl);
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("User-Agent", "http://www.gms-world.net HTTP client");
            conn.setRequestProperty("Referer", ConfigurationManager.SERVER_URL + Commons.getProperty(Property.REFERER_KEY));

            if (token != null && scope != null) {
            	conn.setRequestProperty(Commons.TOKEN_HEADER, token);
            	conn.setRequestProperty(Commons.SCOPE_HEADER, scope);
            }

            conn.connect();
            int responseCode = conn.getResponseCode();
            httpResponseStatuses.put(fileUrl.toExternalForm(), responseCode);

            if (responseCode == HttpServletResponse.SC_OK) { 
            	logger.log(Level.INFO, "Received " + conn.getContentType() + " content"); 
            	if (conn.getContentType().indexOf("deflate") != -1) {
					ois = new ObjectInputStream(new InflaterInputStream(conn.getInputStream(), new Inflater(false)));
				} else if (conn.getContentType().indexOf("application/x-java-serialized-object")  != -1) {
					ois = new ObjectInputStream(conn.getInputStream());
				} else {
					logger.log(Level.WARNING, "Received " + conn.getContentType() + " content: " + IOUtils.toString(new GZIPInputStream(conn.getInputStream()), "UTF-8"));
				}
            	int size = 0;
            	if (ois != null) {
        			size = ois.readInt();
        			logger.log(Level.INFO, "Reading " + size + " landmarks");
        			if (size > 0) {
        				for(int i = 0;i < size;i++) {
        					try {
        						logger.log(Level.INFO, "Reading landmark " + i); 
        						ExtendedLandmark landmark = new ExtendedLandmark(); 
        						landmark.readExternal(ois);
        						landmarks.add(landmark);
        					} catch (IOException e) {
        						e.printStackTrace();
        					}
        				}
        				logger.log(Level.INFO, "Done");
        			}
        		} else {
        			logger.log(Level.SEVERE, "Object stream is null");
        		}
                logger.log(Level.INFO, "Received " + conn.getContentType() + " file having " + size + " landmarks");
            } else if (responseCode >= 400 ){
                logger.log(Level.SEVERE, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});   
            } else if (responseCode >= 300 && responseCode < 400) {
            	logger.log(Level.WARNING, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});   
            } else if (responseCode > 200) {
            	logger.log(Level.INFO, "Received http status code {0} for url {1}", new Object[]{responseCode, fileUrl.toString().split("\\?")[0]});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (ois != null) {
                ois.close();
            }
            
        }
        
        return landmarks;
    }

    public static boolean isEmptyAny(HttpServletRequest request, String... params) {
       try {
    	   for (String p : params) {
    		   if (StringUtils.isEmpty(request.getParameter(p))) {
    			   logger.log(Level.INFO, "Request parameter {0} not found", p);
    			   return true;
    		   }
    	   }
       } catch (Exception e) {
    	   logger.log(Level.SEVERE, e.getMessage(), e);
       }
       return false;
    }
    
    public static boolean isEmptyAnyDebug(HttpServletRequest request, String... params) {
        boolean isMissing = false;
    	for (String p : params) {
    		String value= null;
    		try {
    			value = request.getParameter(p);
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, "Request parameter " + p + " not found: " + e.getMessage(), e);
    		}
    		if (StringUtils.isEmpty(value)) {
                logger.log(Level.INFO, "Request parameter {0} not found", p);
                isMissing = true;
            } else {
            	logger.log(Level.INFO, "Found request parameter {0} : {1}", new Object[] {p, value});
            }
        }
        return isMissing;
    }
    
    public static Integer getResponseCode(String url) {
    	return httpResponseStatuses.remove(url);
    }
}
