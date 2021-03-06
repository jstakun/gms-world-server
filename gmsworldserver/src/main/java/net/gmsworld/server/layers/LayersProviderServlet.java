package net.gmsworld.server.layers;

import java.io.IOException;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            Double latitude;
            if (request.getParameter("lat") != null) {
                latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
            } else {
                latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
            }

            Double longitude;
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

            Double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
            Double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
            Double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
            Double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));

            String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");
            int radiusInKm = NumberUtils.getRadius(request.getParameter("radius"), 3, 100);
            int radiusInMeters = radiusInKm * 1000;
            int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
            int dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
            int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
            
            String uri = request.getRequestURI();

            if (StringUtils.contains(uri, "facebookProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "distance") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (request.getParameter("distance") != null) {
                		radiusInMeters = NumberUtils.getRadius(request.getParameter("distance"), 1, 6371) * 1000;
                	}
                	String query = request.getParameter("q");
                    String token = null;
                    if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                        token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    }
                    if (outFormat.equals(Format.BIN)) {
                        List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processBinaryRequest(latitude, longitude, query, radiusInMeters, version, limit, stringLimit, token, null, l, true);
                        LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).serialize(landmarks, response.getOutputStream(), version);
                        LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.FACEBOOK_LAYER, l, null);
                    } else {
                    	outString = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processRequest(latitude, longitude, query, radiusInMeters, version, limit, stringLimit, token, null).toString();
                    }
                }    
            } else if (StringUtils.contains(uri, "foursquareProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).processBinaryRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, "checkin", language, l, true);               	
                		LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.FOURSQUARE_LAYER, l, null);
                	} else {
                		outString = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).processRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, "checkin", language).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "yelpProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    radiusInMeters = NumberUtils.getRadius(request.getParameter("radius"), 1000, 40000);
                    int deals = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), 0);
                    String hasDeals = "false";
                    if (deals == 1) {
                        hasDeals = "true";
                    }
                    if (outFormat.equals(Format.BIN)) {
                        List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).processBinaryRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, hasDeals, language, l, true);
                    	LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.YELP_LAYER, l, null);
                    } else {
                    	outString = LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).processRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, hasDeals, language).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "googlePlacesProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).processBinaryRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, language, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.GOOGLE_PLACES_LAYER, l, null);
                	} else {			
                        outString = LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).processRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, language, null).toString();
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
                    		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, dealLimit, stringLimit, categoryid, language, l, true);
                    		LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    		LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.COUPONS_LAYER, l, null);
                    	} else {
                    		outString = LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, dealLimit, stringLimit, categoryid, language).toString();
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
                        	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, dealLimit, stringLimit, categoryid, null, l, true);
                    		LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    		LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.GROUPON_LAYER, l, null);
                    	} else {
                    	    outString = LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, dealLimit, stringLimit, categoryid, null).toString();
                    	}	
                    }
                }
            } else if (StringUtils.contains(uri, "atmProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.MC_ATM_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, 1, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.MC_ATM_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.MC_ATM_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.MC_ATM_LAYER, l, null);
                    } else {	
                		outString = LayerHelperFactory.getInstance().getByName(Commons.MC_ATM_LAYER).processRequest(latitude, longitude, null, radiusInKm, 1, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "flickrProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
            		if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).processBinaryRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.FLICKR_LAYER, l, null);
            		} else { 
            			if (version > 4) {
                        	outString = LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).processRequest(latitudeMin, longitudeMin, null, radiusInMeters, version, limit, stringLimit, null, null).toString();
                    	} else {
                        	outString = ((FlickrUtils)LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER)).processRequest(latitudeMin, latitudeMax, longitudeMin, longitudeMax, null, version, limit, stringLimit, formatParam);
                    	}
            		} 
                }
            } else if (StringUtils.contains(uri, "downloadLandmark")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                	 	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).processBinaryRequest(latitudeMin, longitudeMin, null, radiusInMeters, version, limit, stringLimit, layer, null, l, true);
                	 	LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).serialize(landmarks, response.getOutputStream(), version);
                	 	LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).cacheGeoJson(landmarks, latitude, longitude, layer, l, null);
                	} else {
                		if (version > 4) {
                			outString = LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).processRequest(latitudeMin, longitudeMin, null, radiusInMeters, version, limit, stringLimit, layer, null).toString();
                		} else {
                			outString = ((GMSUtils)LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER)).processRequest(latitudeMin, longitudeMin, latitudeMax, longitudeMax, version, limit, stringLimit, layer, formatParam);
                		}
                	}	
                }
            } else if (StringUtils.contains(uri, "meetupProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.MEETUP_LAYER, l, null);                      
                	} else {	
                		outString = LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "youTubeProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.YOUTUBE_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.YOUTUBE_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.YOUTUBE_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.YOUTUBE_LAYER, l, null);                      
                    } else {
                		outString = LayerHelperFactory.getInstance().getByName(Commons.YOUTUBE_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, formatParam, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "eventfulProvider")) {
            	if (outFormat.equals(Format.BIN)) {
            		if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
            			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            		} else {
            			List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null, l, true);
            			LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER).serialize(landmarks, response.getOutputStream(), version);
            			LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.EVENTFUL_LAYER, l, null);                      
                    } 
            	} else {
            		if (HttpUtils.isEmptyAny(request, "location", "within", "date", "page_size")) {
            			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            		} else {
            			if (outFormat.equals(Format.JSON)) {
            				outString = ((EventfulUtils)LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER)).processRequest(null, version, stringLimit, request.getQueryString());
            			} else {
            				outString = ((EventfulUtils)LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER)).processRequest(request.getQueryString());
            			}
            		}
            	}
            } else if (StringUtils.contains(uri, "osmProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin", "latitudeMax", "longitudeMax")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String bbox = null;	
                	String amenity = StringUtil.getStringParam(request.getParameter("amenity"), "atm");
            		if ((latitudeMin == 0d && latitudeMax == 0d && longitudeMin == 0d && longitudeMax == 0d) ||
            		    (latitudeMin == 85.05d && latitudeMax == 85.05d && longitudeMin == -180d && longitudeMax == -180d)) {
                		logger.log(Level.WARNING, "Bounding box is zero!");
                	} else {
                		bbox = StringUtil.formatCoordE6(latitudeMin) + "," + StringUtil.formatCoordE6(longitudeMin) + "," + 
                			   StringUtil.formatCoordE6(latitudeMax) + "," + StringUtil.formatCoordE6(longitudeMax);
                	}	
                	LayerHelper layerHelper = null;
            		if (StringUtils.equals(amenity, "parking")) {
            			layerHelper = LayerHelperFactory.getInstance().getByName(Commons.OSM_PARKING_LAYER);
            		} else if (StringUtils.equals(amenity, "taxi")) {
            			layerHelper = LayerHelperFactory.getInstance().getByName(Commons.OSM_TAXI_LAYER);
            		} else {
            			layerHelper = LayerHelperFactory.getInstance().getByName(Commons.OSM_ATM_LAYER);
            		}
            		//logger.log(Level.INFO, "bbox: " + bbox + ", latitude: " + latitude + ", longitude: " + longitude + ", amenity: " + amenity);
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(latitude, longitude, null, -1, 1, limit, stringLimit, amenity, bbox, l, true);
                    	layerHelper.serialize(landmarks, response.getOutputStream(), version);
                    	layerHelper.cacheGeoJson(landmarks, latitude, longitude, amenity, l, null);                      
                    } else {	
                    	outString = layerHelper.processRequest(latitude, longitude, null, -1, 1, limit, stringLimit, amenity, bbox).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "geonamesProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.WIKIPEDIA_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, language, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.WIKIPEDIA_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.WIKIPEDIA_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.WIKIPEDIA_LAYER, l, null);
                	} else {
                		outString = LayerHelperFactory.getInstance().getByName(Commons.WIKIPEDIA_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, language, null).toString();
                	}	
                }
            } else if (StringUtils.contains(uri, "webcamProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.WEBCAM_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null, l, true);
                		LayerHelperFactory.getInstance().getByName(Commons.WEBCAM_LAYER).serialize(landmarks, response.getOutputStream(), version);
                		LayerHelperFactory.getInstance().getByName(Commons.WEBCAM_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.WEBCAM_LAYER, l, null);    
                	} else {
                		outString = LayerHelperFactory.getInstance().getByName(Commons.WEBCAM_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, null, null).toString();
                	}	
                }
            /*} else if (StringUtils.contains(uri, "panoramio2Provider")) {
                if (HttpUtils.isEmptyAny(request, "minx", "miny", "maxx", "maxy")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    double miny = GeocodeUtils.getLatitude(request.getParameter("miny"));
                    double minx = GeocodeUtils.getLongitude(request.getParameter("minx"));
                    double maxy = GeocodeUtils.getLatitude(request.getParameter("maxy"));
                    double maxx = GeocodeUtils.getLongitude(request.getParameter("maxx"));
                    
                    //minx, miny, maxx, maxy -> minimum longitude, latitude, maximum longitude and latitude,
                    
                    if ((minx == 0d && maxx == 0d && miny == 0d && maxy == 0d) ||
                    	(miny == 85.05d && maxy == 85.05d && minx == -180d && maxx == -180d)) {
            			logger.log(Level.WARNING, "Bounding box is zero. Changing to latitude and longitude");
            			minx = maxx = longitude;
            			miny = maxy = latitude;
            		}
                    
                    if (Math.abs(maxx - minx) < 0.1) {
            			if (minx > -180d) {
            				minx -= 0.1;
            			}
            			if (maxx < 180d) {
            				maxx += 0.1;
            			}
            		}
            		if (Math.abs(maxy - miny) < 0.1) {
            			if (miny > -90d) {
            				miny -= 0.1;
            			}
            			if (maxy < 90d) {
            				maxy += 0.1;
            			}
            		}

                    String bbox = "minx=" + minx + "&miny=" + miny + "&maxx=" + maxx + "&maxy=" + maxy;
                    
                    //remove
                    //logger.log(Level.INFO, "bbox: " + bbox + ", latitude: " + latitude + ", longitude: " + longitude);
                    
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.PANORAMIO_LAYER).processBinaryRequest(latitude, longitude, null, 0, version, limit, stringLimit, bbox, null, l, true);
                    	LayerHelperFactory.getInstance().getByName(Commons.PANORAMIO_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.PANORAMIO_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.PANORAMIO_LAYER, l, null);    
                    } else {	
                    	outString = LayerHelperFactory.getInstance().getByName(Commons.PANORAMIO_LAYER).processRequest(latitude, longitude, null, 0, version, limit, stringLimit, bbox, null).toString();
                    }	
                }*/
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
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_MERCHANT_LAYER).processBinaryRequest(latitude, longitude, null, radiusInMeters, version, limit, stringLimit, token, categoryid, l, true);
                    	LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_MERCHANT_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_MERCHANT_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.FOURSQUARE_MERCHANT_LAYER, l, null);    
                    } else {
                        outString = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_MERCHANT_LAYER).processRequest(latitude, longitude, categoryid, radiusInMeters, version, limit, stringLimit, token, language).toString();
                    }                                  
                }
            } else if (StringUtils.contains(uri, "expediaProvider")) { //remove?
                if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.EXPEDIA_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, locale, null, l, true);
                    	LayerHelperFactory.getInstance().getByName(Commons.EXPEDIA_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.EXPEDIA_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.EXPEDIA_LAYER, l, null);    
                    } else {	
                		outString = LayerHelperFactory.getInstance().getByName(Commons.EXPEDIA_LAYER).processRequest(latitude, longitude, null, radiusInKm, 1, limit, stringLimit, locale, null).toString();
                	}
                }
            } else if (StringUtils.contains(uri, "hotelsProvider")) {
                if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) { //, "latitudeMax", "longitudeMax")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER).processBinaryRequest(latitudeMin, longitudeMin, null, radiusInMeters, version, limit, stringLimit, language, null, l, false);
                    	LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.HOTELS_LAYER, l, "distance");                          
                    } else {
                		outString = LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER).processRequest(latitudeMin, longitudeMin, null, radiusInMeters, version, limit, stringLimit, language, null).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "twitterProvider")) {
                if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius") && HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	if (outFormat.equals(Format.BIN)) {
                		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).processBinaryRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, language, null, l, true);
                    	LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    	LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).cacheGeoJson(landmarks, latitude, longitude, Commons.TWITTER_LAYER, l, null);                          
                    } else {
                	   outString = LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).processRequest(latitude, longitude, null, radiusInKm, version, limit, stringLimit, language, null).toString();
                	}   
                }
            } else if (StringUtils.contains(uri, "fbCheckins")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                  	List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPlaces(version, dealLimit, stringLimit, token, l, false);
                    if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                }
            } else if (StringUtils.contains(uri, "fbPhotos")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPhotos(version, limit, stringLimit, token, l, false);
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
                 }
            } else if (StringUtils.contains(uri, "fbTagged")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                	String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                	List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyTaggedPlaces(version, dealLimit, stringLimit, token, l, false);
                	if (outFormat.equals(Format.BIN)) {
                		LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).serialize(landmarks, response.getOutputStream(), version);
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
                    	List<ExtendedLandmark> landmarks = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).getFriendsCheckinsToLandmarks(latitude, longitude, limit, stringLimit, version, token, l, false);
                    	LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).getFriendsCheckinsToJSon(latitude, longitude, limit, version, token, language).toString();
                    }  
                }
            } else if (StringUtils.contains(uri, "fsRecommended")) {
            	if (HttpUtils.isEmptyAny(request,"lat","lng","radius","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","radius","token")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                    if (outFormat.equals(Format.BIN)) {
                    	List<ExtendedLandmark> landmarks = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).exploreVenuesToLandmark(latitude, longitude, null, radiusInMeters, limit, stringLimit, version, token, l, false);
                    	LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).exploreVenuesToJSon(latitude, longitude, null, radiusInMeters, limit, stringLimit, version, token, language).toString();
                    }
                }    
            } else if (StringUtils.contains(uri, "twFriends")) {
            	if (HttpUtils.isEmptyAny(request, "token", "secret")) {
            		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	} else {
            		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
            		String secret = URLDecoder.decode(request.getParameter("secret"), "UTF-8");
            		List<ExtendedLandmark> landmarks = ((TwitterUtils)LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER)).getFriendsStatuses(token, secret, l, false);
                    if (outFormat.equals(Format.BIN)) {
                    	LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).serialize(landmarks, response.getOutputStream(), version);
                    } else {
                    	outString = new JSONObject().put("ResultSet", landmarks).toString();
                    }
            	}
            } else if (StringUtils.endsWithAny(uri, new String[]{"qypeProvider", "upcomingProvider", "gowallaProvider", 
            		"picasaProvider", "freebaseProvider", "lastfmProvider", "instagramProvider", "panoramioProvider", "panoramio2Provider", "hotwireProvider"})) {
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
        		if (outString == null && outFormat.equals(Format.JSON)) {
        			 outString = "{ResultSet:[]}";
        		}
        		if (outPrinter != null) {
        			if (outString != null) {
        				outPrinter.print(outString);
        			}
        			outPrinter.close();
        		}
        	} 
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
        LayerHelperFactory.getInstance().setThreadProvider(new GoogleThreadProvider());
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
