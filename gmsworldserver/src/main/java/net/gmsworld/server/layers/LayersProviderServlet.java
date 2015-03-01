package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import twitter4j.TwitterException;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

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
        //ObjectOutputStream outObj = null;
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
                        String key = LayerHelperFactory.getFacebookUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FACEBOOK_LAYER);
                	    logger.log(Level.INFO, "Saved geojson list to cache: " + key);
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
                		String key = LayerHelperFactory.getFoursquareUtils().cacheGeoJson(landmarks, latitude, longitude, Commons.FOURSQUARE_LAYER);
                	    logger.log(Level.INFO, "Saved geojson list to cache: " + key);
                	} else {
                		outString = LayerHelperFactory.getFoursquareUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, "checkin", language).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "yelpProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (LayerHelperFactory.getYelpUtils().hasNeighborhoods(latitude, longitude)) {
                        radius = NumberUtils.getRadius(request.getParameter("radius"), 1000, 40000);
                        int deals = NumberUtils.getInt(request.getHeader("X-GMS-AppId"), 0);
                        String hasDeals = "false";
                        if (deals == 1) {
                            hasDeals = "true";
                        }
                        if (outFormat.equals(Format.BIN)) {
                        	List<ExtendedLandmark> landmarks = LayerHelperFactory.getYelpUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, hasDeals, language, l, true);
                    		LayerHelperFactory.getYelpUtils().serialize(landmarks, response.getOutputStream(), version);
                    	} else {
                    		outString = LayerHelperFactory.getYelpUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, hasDeals, language).toString();
                    	}
                    }
                }
            } else if (StringUtils.contains(uri, "googlePlacesProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getGooglePlacesUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null, l, true);
                		LayerHelperFactory.getGooglePlacesUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {			
                        outString = LayerHelperFactory.getGooglePlacesUtils().processRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "couponsProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (GeocodeUtils.isNorthAmericaLocation(request.getParameter("latitude"), request.getParameter("longitude"))) {
                        String categoryid = "";
                        if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                            categoryid = request.getParameter("categoryid");
                        }
                    	if (outFormat.equals(Format.BIN)) {
                    		List<ExtendedLandmark> landmarks = LayerHelperFactory.getCouponsUtils().processBinaryRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, language, l, true);
                    		LayerHelperFactory.getCouponsUtils().serialize(landmarks, response.getOutputStream(), version);
                    	} else {
                    		outString = LayerHelperFactory.getCouponsUtils().processRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, language).toString();
                    	}	                        
                    }
                }
            } else if (StringUtils.contains(uri, "grouponProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                        String categoryid = null;
                        if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                            categoryid = request.getParameter("categoryid");
                        }
                        if (outFormat.equals(Format.BIN)) {
                        	List<ExtendedLandmark> landmarks = LayerHelperFactory.getGrouponUtils().processBinaryRequest(latitude, longitude, null, radius, version, dealLimit, stringLimit, categoryid, null, l, true);
                    		LayerHelperFactory.getGrouponUtils().serialize(landmarks, response.getOutputStream(), version);
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
                	} else {	
                		outString = LayerHelperFactory.getMcOpenApiUtils().processRequest(latitude, longitude, null, radius, 1, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "flickrProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
            		if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFlickrUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, layer, null, l, true);
                		LayerHelperFactory.getFlickrUtils().serialize(landmarks, response.getOutputStream(), version);
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
                	} else {
                		if (version > 4) {
                			outString = LayerHelperFactory.getGmsUtils().processRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, layer, null).toString();
                		} else {
                			outString = LayerHelperFactory.getGmsUtils().processRequest(latitudeMin, longitudeMin, latitudeMax, longitudeMax, version, limit, stringLimit, layer, formatParam);
                		}
                	}	
                }
            } else if (StringUtils.contains(uri, "picasaProvider")) {
                if (request.getParameter("bbox") == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    String bbox = request.getParameter("bbox");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getPicasaUtils().processBinaryRequest(0.0, 0.0, null, 0, version, limit, stringLimit, bbox, null, l, true);
                    	LayerHelperFactory.getPicasaUtils().serialize(landmarks, response.getOutputStream(), version);
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
                		String bbox = StringUtil.formatCoordE6(longitudeMin) + "," + StringUtil.formatCoordE6(latitudeMin) + "," + 
                				StringUtil.formatCoordE6(longitudeMax) + "," + StringUtil.formatCoordE6(latitudeMax);
                		if (outFormat.equals(Format.BIN)) {
                    		List<ExtendedLandmark> landmarks = LayerHelperFactory.getOsmXapiUtils().processBinaryRequest(0.0, 0.0, null, -1, 1, limit, stringLimit, amenity, bbox, l, true);
                    		LayerHelperFactory.getOsmXapiUtils().serialize(landmarks, response.getOutputStream(), version);
                		} else {	
                			outString = LayerHelperFactory.getOsmXapiUtils().processRequest(0.0, 0.0, null, -1, 1, limit, stringLimit, amenity, bbox).toString();
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
                	} else {
                		outString = LayerHelperFactory.getGeonamesUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "lastfmProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getLastfmUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getLastfmUtils().serialize(landmarks, response.getOutputStream(), version);
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
                	} else {
                		outString = LayerHelperFactory.getWebcamUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "hotwireProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    outString = LayerHelperFactory.getHotwireUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();
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
                    if (StringUtils.isEmpty(token)) {
                        token = Commons.getProperty(Property.FS_OAUTH_TOKEN);
                    }
                    String categoryid = request.getParameter("categoryid");

                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareMerchantUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, token, categoryid, l, true);
                    	LayerHelperFactory.getFoursquareMerchantUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                        outString = LayerHelperFactory.getFoursquareMerchantUtils().processRequest(latitude, longitude, categoryid, radius * 1000, version, limit, stringLimit, token, language).toString();
                    }                                  
                }
            } else if (StringUtils.contains(uri, "expediaProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getExpediaUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, locale, null, l, true);
                    	LayerHelperFactory.getExpediaUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {	
                		outString = LayerHelperFactory.getExpediaUtils().processRequest(latitude, longitude, null, radius, 1, limit, stringLimit, locale, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "hotelsProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) { //, "latitudeMax", "longitudeMax")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getHotelsCombinedUtils().processBinaryRequest(latitudeMin, longitudeMin, null, radius, version, limit, stringLimit, language, null, l, false);
                    	LayerHelperFactory.getHotelsCombinedUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {
                		String flex2 = null;

                        if (version <= 2) {
                            flex2 = Double.toString(latitudeMax) + "_" + Double.toString(longitudeMax);
                        }

                		outString = LayerHelperFactory.getHotelsCombinedUtils().processRequest(latitudeMin, longitudeMin, null, radius, version, limit, stringLimit, language, flex2).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "twitterProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getTwitterUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null, l, true);
                    	LayerHelperFactory.getTwitterUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {
                	   outString = LayerHelperFactory.getTwitterUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null).toString();
                	}   
                }
            } else if (StringUtils.contains(uri, "instagramProvider")) {
            	if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstagramUtils().processBinaryRequest(latitude, longitude, null, radius * 1000, version, limit, stringLimit, language, null, l, true);               	
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getInstagramUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {
                		outString = new JSONObject().put("ResultSet", landmarks).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "freebaseProvider")) {
            	if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFreebaseUtils().processBinaryRequest(latitude, longitude, null, radius, version, limit, stringLimit, language, null, l, true);               	
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getFreebaseUtils().serialize(landmarks, response.getOutputStream(), version);
                	} else {
                		outString = new JSONObject().put("ResultSet", landmarks).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "fbCheckins")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getFriendsCheckinsToLandmarks(latitude, longitude, version, limit, stringLimit, token, l);
                    	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	//outString = new JSONObject().put("ResultSet", landmarks).toString();
                    	outString = LayerHelperFactory.getFacebookUtils().getFriendsCheckinsToJSon(latitude, longitude, version, limit, stringLimit, token).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "fbPhotos")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getFriendsPhotosToLandmark(latitude, longitude, version, limit, stringLimit, token, l);
                    	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = LayerHelperFactory.getFacebookUtils().getFriendsPhotosToJSon(latitude, longitude, version, limit, stringLimit, token).toString();              
                    }
                 }
            } else if (StringUtils.contains(uri, "fsCheckins")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","radius","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","radius","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else { 
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().getFriendsCheckinsToLandmarks(latitude, longitude, limit, version, token, language, l);
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
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().exploreVenuesToLandmark(latitude, longitude, null, radius * 1000, limit, version, token, language, l);
                    	LayerHelperFactory.getFoursquareUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = LayerHelperFactory.getFoursquareUtils().exploreVenuesToJSon(latitude, longitude, null, radius * 1000, limit, version, token, language).toString();
                    }
                }    
            } else if (StringUtils.contains(uri, "twFriends")) {
            	if (HttpUtils.isEmptyAny(request, "token", "secret")) {
            		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	} else {
            		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
            		String secret = URLDecoder.decode(request.getParameter("secret"), "UTF-8");
            		List<ExtendedLandmark> landmarks = LayerHelperFactory.getTwitterUtils().getFriendsStatuses(token, secret, l);
                    if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getTwitterUtils().serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
            	}
            } else if (StringUtils.contains(uri, "qypeProvider") || StringUtils.contains(uri, "upcomingProvider") || StringUtils.contains(uri, "gowallaProvider")) {
            	logger.log(Level.INFO, "Closed api request uri: {0}", uri);
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
        InputStream stream = getServletContext().getResourceAsStream(Commons.getProperty(Property.mcopenapi_privKeyFile));
        LayerHelperFactory.getMcOpenApiUtils().setPrivateKey(stream);
        LayerHelperFactory.setCacheProvider(new GoogleCacheProvider());
        LayerHelperFactory.setThreadProvider(new GoogleThreadProvider());
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
    }// </editor-fold>
}
