package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import twitter4j.TwitterException;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.utils.GoogleThreadProvider;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.restfb.exception.FacebookOAuthException;

import fi.foyt.foursquare.api.FoursquareApiException;

/**
 *
 * @author jstakun
 */
public class LayersProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LayersProviderServlet.class.getName());
	private static enum Format {BIN, XML, KML, JSON};

	/**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String formatParam = StringUtil.getStringParam(request.getParameter("format"), "json");
        Format outFormat = null;
        PrintWriter outPrinter = null;
        String outString = null;
        int version = NumberUtils.getVersion(request.getParameter("version"), 1);

        if (formatParam.equals("kml")) {
            response.setContentType("text/kml;charset=UTF-8");
            outPrinter = response.getWriter();
            outString = "<kml/>";
            outFormat = Format.KML; 
        } else if (formatParam.equals("xml")) {
            response.setContentType("text/xml;charset=UTF-8");
            outPrinter = response.getWriter();
            outString = "<results/>";
            outFormat = Format.XML;
        } else if (formatParam.equals("bin")) {
        	if (version >= 12) {
        		response.setContentType("deflate");
        	} else {
        		//version = 11; //this will use only serialization
        		response.setContentType("application/x-java-serialized-object"); 
        	}
            outFormat = Format.BIN;
        } else {
            response.setContentType("text/json;charset=UTF-8");
            outPrinter = response.getWriter();
            outString = "{ResultSet:[]}";
            outFormat = Format.JSON;
        }

        try {

            double latitude;
            if (request.getParameter("lat") != null) {
                latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
            } else {
                latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
            }

            double longitude;
            if (request.getParameter("lng") != null) {
                longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
            } else {
                longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
            }

            Locale l = request.getLocale();
            String language;
            if (request.getParameter("lang") != null) {
                language = StringUtil.getLanguage(request.getParameter("lang"), "en", 2);
            } else if (request.getParameter("language") != null) {
                language = StringUtil.getLanguage(request.getParameter("language"), "en", 2);
            } else {
                language = StringUtil.getLanguage(l.getLanguage(), "en", 2);
            }
            String locale = StringUtil.getLanguage(l.getLanguage() + "_" + l.getCountry(), "en_US", 5);

            double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
            double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
            double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
            double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));

            String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");
            int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 100);
            int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
            int dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
            int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
            
            String uri = request.getRequestURI();

            if (StringUtils.contains(uri, "facebookProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "distance") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (request.getParameter("distance") != null) {
                		radius = NumberUtils.getRadius(request.getParameter("distance"), 1, 6371);
                	}
                	String query = request.getParameter("q");
                    String token = null;
                    if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                        token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    }
                    if (outFormat.equals(Format.BIN)) {
                        List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, version, limit, stringLimit, token, null, l, true);
                        LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                        LayerHelperFactory.getFacebookUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FACEBOOK_LAYER, l, null);
                        //new JSONObject().put("ResultSet", landmarks).toString();                    
                    } else {
                    	outString = LayerHelperFactory.getFacebookUtils().processRequest(latitude, longitude, query, radius * 1000, version, limit, stringLimit, token, null).toString();
                    }
                }    
            } else if (StringUtils.contains(uri, "foursquareProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, "checkin", language, l, true);               	
                		LayerHelperFactory.getFoursquareUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getFoursquareUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FOURSQUARE_LAYER, l, null);
                	} else {
                		outString = LayerHelperFactory.getFoursquareUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, "checkin", language).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "yelpProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    radius = NumberUtils.getRadius(request.getParameter("radius"), 1000, 40000);
                    int deals = NumberUtils.getInt(request.getHeader("X-GMS-AppId"), 0);
                    String hasDeals = "false";
                    if (deals == 1) {
                        hasDeals = "true";
                    }
                    if (outFormat.equals(Format.BIN)) {
                        List<ExtendedLandmark> landmarks = LayerHelperFactory.getYelpUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, hasDeals, language, l, true);
                    	LayerHelperFactory.getYelpUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getYelpUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.YELP_LAYER, l, null);
                    } else {
                    	outString = LayerHelperFactory.getYelpUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, hasDeals, language).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "googlePlacesProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getGooglePlacesUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null, l, true);
                		LayerHelperFactory.getGooglePlacesUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getGooglePlacesUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.GOOGLE_PLACES_LAYER, l, null);
                	} else {			
                        outString = LayerHelperFactory.getGooglePlacesUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "couponsProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (GeocodeUtils.isNorthAmericaLocation(latitude, longitude)) {
                        String categoryid = "";
                        if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                            categoryid = request.getParameter("categoryid");
                        }
                    	if (outFormat.equals(Format.BIN)) {
                    		List<ExtendedLandmark> landmarks = LayerHelperFactory.getCouponsUtils().processBinaryRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, language, l, true);
                    		LayerHelperFactory.getCouponsUtils().serialize(landmarks, response.getOutputStream(), version);
                    		LayerHelperFactory.getCouponsUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.COUPONS_LAYER, l, null);
                    	} else {
                    		outString = LayerHelperFactory.getCouponsUtils().processRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, language).toString();
                    	}	                        
                    }
                }
            } else if (StringUtils.contains(uri, "grouponProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (GeocodeUtils.isNorthAmericaLocation(latitude, longitude)) {
                        String categoryid = null;
                        if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                            categoryid = request.getParameter("categoryid");
                        }
                        if (outFormat.equals(Format.BIN)) {
                        	List<ExtendedLandmark> landmarks = LayerHelperFactory.getGrouponUtils().processBinaryRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, null, l, true);
                    		LayerHelperFactory.getGrouponUtils().serialize(landmarks, response.getOutputStream(), version);
                    		LayerHelperFactory.getGrouponUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.GROUPON_LAYER, l, null);
                    	} else {
                    	    outString = LayerHelperFactory.getGrouponUtils().processRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, null).toString();
                    	}	
                    }
                }
            } else if (StringUtils.contains(uri, "atmProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getMcOpenApiUtils().processBinaryRequest(latitude, longitude, null, radius, 1, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getMcOpenApiUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getMcOpenApiUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.MC_ATM_LAYER, l, null);
                    } else {	
                		outString = LayerHelperFactory.getMcOpenApiUtils().processRequest(latitude, longitude, null, radius, 1, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "flickrProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
            		if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFlickrUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getFlickrUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getFlickrUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FLICKR_LAYER, l, null);
            		} else { 
            			if (version > 4) {
                        	outString = LayerHelperFactory.getFlickrUtils().processRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, null, null).toString();
                    	} else {
                        	outString = LayerHelperFactory.getFlickrUtils().processRequest(latitudeMin, latitudeMax, longitudeMin, longitudeMax, null, version, limit, stringLimit, formatParam);
                    	}
            		} 
                }
            } else if (StringUtils.contains(uri, "downloadLandmark")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                	 	List<ExtendedLandmark> landmarks = LayerHelperFactory.getGmsUtils().processBinaryRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, layer, null, l, true);
                	 	LayerHelperFactory.getGmsUtils().serialize(landmarks, response.getOutputStream(), version);
                	 	LayerHelperFactory.getGmsUtils().cacheGeoJson(landmarks, latitude, longitude, layer, l, null);
                	} else {
                		if (version > 4) {
                			outString = LayerHelperFactory.getGmsUtils().processRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, layer, null).toString();
                		} else {
                			outString = LayerHelperFactory.getGmsUtils().processRequest(latitudeMin, longitudeMin, latitudeMax, longitudeMax, version, limit, stringLimit, layer, formatParam);
                		}
                	}	
                }
            } else if (StringUtils.contains(uri, "picasaProvider")) { //TODO remove
                if (request.getParameter("bbox") == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    String bbox = request.getParameter("bbox");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(); //LayerHelperFactory.getPicasaUtils().processBinaryRequest(0.0, 0.0, null, 0, version, limit, stringLimit, bbox, null, l, true);
                    	LayerHelperFactory.getPicasaUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getPicasaUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.PICASA_LAYER, l, null);
                    } else {
                    	outString = LayerHelperFactory.getPicasaUtils().processRequest(0.0, 0.0, null, 0, version, limit, stringLimit, bbox, null).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "meetupProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getMeetupUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getMeetupUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getMeetupUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.MEETUP_LAYER, l, null);                      
                	} else {	
                		outString = LayerHelperFactory.getMeetupUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "youTubeProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getYoutubeUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getYoutubeUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getYoutubeUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.YOUTUBE_LAYER, l, null);                      
                    } else {
                		outString = LayerHelperFactory.getYoutubeUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, formatParam, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "eventfulProvider")) {
            	if (outFormat.equals(Format.BIN)) {
            		if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
            			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            		} else {
            			List<ExtendedLandmark> landmarks = LayerHelperFactory.getEventfulUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
            			LayerHelperFactory.getEventfulUtils().serialize(landmarks, response.getOutputStream(), version);
            			LayerHelperFactory.getEventfulUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.EVENTFUL_LAYER, l, null);                      
                    } 
            	} else {
            		if (HttpUtils.isEmptyAny(request, "location", "within", "date", "page_size")) {
            			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            		} else {
            			if (outFormat.equals(Format.JSON)) {
            				outString = LayerHelperFactory.getEventfulUtils().processRequest(null, version, stringLimit, request.getQueryString());
            			} else {
            				outString = LayerHelperFactory.getEventfulUtils().processRequest(request.getQueryString());
            			}
            		}
            	}
            } else if (StringUtils.contains(uri, "osmProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin", "latitudeMax", "longitudeMax")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (Math.abs(latitudeMax - latitudeMin) < 10.0 && Math.abs(longitudeMax - longitudeMin) < 10.0) {
                		String amenity = StringUtil.getStringParam(request.getParameter("amenity"), "atm");
                		String bbox = StringUtil.formatCoordE6(latitudeMin) + "," + StringUtil.formatCoordE6(longitudeMin) + "," + 
                				StringUtil.formatCoordE6(latitudeMax) + "," + StringUtil.formatCoordE6(longitudeMax);
                		LayerHelper layerHelper = null;
            			if (StringUtils.equals(amenity, "parking")) {
            				layerHelper = LayerHelperFactory.getOsmParkingsUtils();
            			} else {
            				layerHelper = LayerHelperFactory.getOsmAtmUtils();
            			}
                		List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(0.0, 0.0, null, -1, 1, limit, stringLimit, amenity, bbox, l, true);
                		if (outFormat.equals(Format.BIN)) {
                			layerHelper.serialize(landmarks, response.getOutputStream(), version);
                    		layerHelper.cacheGeoJson(landmarks, latitude, longitude, amenity, l, null);                      
                        } else {	
                        	outString = new JSONObject().put("ResultSet", landmarks).toString();
                		}
                	} else {
                		logger.log(Level.WARNING, "OSM API: Maximum bounding box area is 10.0 square degrees.");
                	}
                }
            } else if (StringUtils.contains(uri, "geonamesProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getGeonamesUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null, l, true);
                		LayerHelperFactory.getGeonamesUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getGeonamesUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.GEOCODES_LAYER, l, null);
                	} else {
                		outString = LayerHelperFactory.getGeonamesUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "lastfmProvider")) { //TODO remove
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(); //LayerHelperFactory.getLastfmUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getLastfmUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getLastfmUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.LASTFM_LAYER, l, null);
                    } else {
                		outString = LayerHelperFactory.getLastfmUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "webcamProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getWebcamUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getWebcamUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getWebcamUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.WEBCAM_LAYER, l, null);    
                	} else {
                		outString = LayerHelperFactory.getWebcamUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "panoramio2Provider")) {
                if (HttpUtils.isEmptyAny(request, "minx", "miny", "maxx", "maxy")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    double miny = GeocodeUtils.getLatitude(request.getParameter("miny"));
                    double minx = GeocodeUtils.getLongitude(request.getParameter("minx"));
                    double maxy = GeocodeUtils.getLatitude(request.getParameter("maxy"));
                    double maxx = GeocodeUtils.getLongitude(request.getParameter("maxx"));

                    String bbox = "minx=" + minx + "&miny=" + miny + "&maxx=" + maxx + "&maxy=" + maxy;

                    latitude = (miny + maxy) / 2;
                    longitude = (minx + maxy) / 2;

                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getPanoramioUtils().processBinaryRequest(latitude, longitude, null, 0, version, limit, stringLimit, bbox, null, l, true);
                    	LayerHelperFactory.getPanoramioUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getPanoramioUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.PANORAMIO_LAYER, l, null);    
                    } else {	
                    	outString = LayerHelperFactory.getPanoramioUtils().processRequest(latitude, longitude, null, 0, version, limit, stringLimit, bbox, null).toString();
                    }	
                }
            } else if (StringUtils.contains(uri, "foursquareMerchant")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) { 
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    String token = null;
                    if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                        token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    }
                    String categoryid = request.getParameter("categoryid");

                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareMerchantUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, token, categoryid, l, true);
                    	LayerHelperFactory.getFoursquareMerchantUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getFoursquareMerchantUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FOURSQUARE_MERCHANT_LAYER, l, null);    
                    } else {
                        outString = LayerHelperFactory.getFoursquareMerchantUtils().processRequest(latitude, longitude, categoryid, radius * 1000, version, limit, stringLimit, token, language).toString();
                    }                                  
                }
            } else if (StringUtils.contains(uri, "expediaProvider")) { //TODO remove?
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getExpediaUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, locale, null, l, true);
                    	LayerHelperFactory.getExpediaUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getExpediaUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.EXPEDIA_LAYER, l, null);    
                    } else {	
                		outString = LayerHelperFactory.getExpediaUtils().processRequest(latitude, longitude, null, radius, 1, limit, stringLimit, locale, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "hotelsProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) { //, "latitudeMax", "longitudeMax")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	List<ExtendedLandmark> landmarks = LayerHelperFactory.getHotelsBookingUtils().processBinaryRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, language, null, l, false);
                	if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getHotelsBookingUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getHotelsBookingUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.HOTELS_LAYER, l, "distance");                          
                    } else {
                		//String flex2 = null;
                        //if (version <= 2) {
                        //    flex2 = Double.toString(latitudeMax) + "_" + Double.toString(longitudeMax);
                        //}
                		//outString = LayerHelperFactory.getHotelsBookingUtils().processRequest(latitudeMin, longitudeMin, null, radius, version, limit, stringLimit, language, flex2).toString();
                		outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "twitterProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getTwitterUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null, l, true);
                    	LayerHelperFactory.getTwitterUtils().serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getTwitterUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.TWITTER_LAYER, l, null);                          
                    } else {
                	   outString = LayerHelperFactory.getTwitterUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null).toString();
                	}   
                }
            } else if (StringUtils.contains(uri, "instagramProvider")) { //TODO remove
            	if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(); //LayerHelperFactory.getInstagramUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null, l, true);               	
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getInstagramUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstagramUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.INSTAGRAM_LAYER, l, null);                          
                    } else {
                		outString = new JSONObject().put("ResultSet", landmarks).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "freebaseProvider")) { //TODO remove
            	if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(); //LayerHelperFactory.getFreebaseUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null, l, true);               	
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getFreebaseUtils().serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getFreebaseUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FREEBASE_LAYER, l, null);                          
                    } else {
                		outString = new JSONObject().put("ResultSet", landmarks).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "fbCheckins")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                  	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getMyPlaces(version, dealLimit, stringLimit, token, l, false);
                    if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "fbPhotos")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getMyPhotos(version, limit, stringLimit, token, l, false);
                	if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                 }
            } else if (StringUtils.contains(uri, "fbTagged")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getMyTaggedPlaces(version, dealLimit, stringLimit, token, l, false);
                	if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "fsCheckins")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","radius","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","radius","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else { 
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().getFriendsCheckinsToLandmarks(latitude, longitude, limit, stringLimit, version, token, l, false);
                    	LayerHelperFactory.getFoursquareUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = LayerHelperFactory.getFoursquareUtils().getFriendsCheckinsToJSon(latitude, longitude, limit, version, token, language).toString();
                    }  
                }
            } else if (StringUtils.contains(uri, "fsRecommended")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","radius","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","radius","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().exploreVenuesToLandmark(latitude, longitude, null, radius * 1000, limit, stringLimit, version, token, l, false);
                    	LayerHelperFactory.getFoursquareUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = LayerHelperFactory.getFoursquareUtils().exploreVenuesToJSon(latitude, longitude, null, radius * 1000, limit, stringLimit, version, token, language).toString();
                    }
                }    
            } else if (StringUtils.contains(uri, "twFriends")) {
            	if (HttpUtils.isEmptyAny(request, "token", "secret")) {
            		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	} else {
            		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
            		String secret = URLDecoder.decode(request.getParameter("secret"), "UTF-8");
            		List<ExtendedLandmark> landmarks = LayerHelperFactory.getTwitterUtils().getFriendsStatuses(token, secret, l, false);
                    if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getTwitterUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
            	}
            } else if (StringUtils.contains(uri, "qypeProvider") || StringUtils.contains(uri, "upcomingProvider") || StringUtils.contains(uri, "gowallaProvider") || StringUtils.contains(uri, "hotwireProvider")) {
            	logger.log(Level.WARNING, "Closed api request uri: {0}", uri);
            } else {
            	logger.log(Level.SEVERE, "Unexpected uri: {0}", uri);
            }
        } catch (FacebookOAuthException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (outPrinter != null) {
            	outString = "{\"error\":{\"message\":\"Facebook authentication error\"}}";
            } else {
            	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (TwitterException e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        	if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
        		if (outPrinter != null) {
                	outString = "{\"error\":{\"message\":\"Twitter authentication error\"}}";
                } else {
                	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
        	}
        } catch (FoursquareApiException e) {
        	if (StringUtils.equals(e.getMessage(), "Unauthorized")) {
        		if (outPrinter != null) {
                	outString = "{\"error\":{\"message\":\"Foursquare authentication error\"}}";
                } else {
                	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
        	}
        	logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	if (outFormat.equals(Format.BIN)) {
        		//if (outObj != null) {
        		//	outObj.close();
        		//}
        	} else {
        		if (outPrinter != null) {
        			outPrinter.print(outString);
        		}
        		outPrinter.close();
        	} 
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.setCacheProvider(GoogleCacheProvider.getInstance());
        LayerHelperFactory.setThreadProvider(new GoogleThreadProvider());
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Layers provider servlet";
    }
}
