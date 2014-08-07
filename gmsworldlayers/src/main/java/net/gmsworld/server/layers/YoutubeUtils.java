/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Person;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.gdata.util.ServiceException;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class YoutubeUtils extends LayerHelper {

    private static final String YOUTUBE_GDATA_SERVER = "http://gdata.youtube.com";
    private static final String VIDEOS_FEED = YOUTUBE_GDATA_SERVER + "/feeds/api/videos";
    
    private static VideoFeed getVideoFeed(double latitude, double longitude, String query, int radius, int limit) throws MalformedURLException, IOException, ServiceException {
        YouTubeService ytservice = new YouTubeService("GMS World", Commons.getProperty(Property.YOUTUBE_API_KEY));

        YouTubeQuery vquery = new YouTubeQuery(new URL(VIDEOS_FEED));

        GeoRssWhere geo = new GeoRssWhere(latitude, longitude);
        vquery.setLocation(geo);

        int r = NumberUtils.normalizeNumber(radius, 1, 999);
        
        vquery.setLocationRadius(r + "km");
        vquery.setRestrictLocation(true);
        if (StringUtils.isNotEmpty(query)) {
            vquery.setFullTextQuery(query);
        }

        //vquery.setPublishedMin(new DateTime(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 365))); //unsupported by youtube
        //vquery.setOrderBy(YouTubeQuery.OrderBy.PUBLISHED);
        vquery.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);

        //vquery.setTime(YouTubeQuery.Time.THIS_MONTH);

        int l = NumberUtils.normalizeNumber(limit, 1, 50);

        vquery.setMaxResults(l);

        return ytservice.query(vquery, VideoFeed.class);
    }

    @Override
    protected JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flex, String flexString2) throws MalformedURLException, IOException, ServiceException, JSONException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flex, flexString2);

        String output = cacheProvider.getString(key);
        JSONObject json = null;

        if (output == null) {
            VideoFeed videoFeed = getVideoFeed(latitude, longitude, query, radius, limit);
            json = createCustomJSonVideoList(videoFeed.getEntries(), version, stringLimit);
            if (!videoFeed.getEntries().isEmpty()) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding YT landmark list to cache with key {0}", key);
            }
        } else {
            json = new JSONObject(output);
            logger.log(Level.INFO, "Reading YT landmark list from cache with key {0}", key);
        }

        return json;
    }

    private static List<ExtendedLandmark> createCustomLandmarkVideoList(List<VideoEntry> vel, int version, int stringLimit, Locale locale) {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	
    	logger.info("Found " + vel.size() + " video entries...");

        for (VideoEntry ve : vel) {
            YouTubeMediaGroup mediaGroup = ve.getMediaGroup();
            if (mediaGroup != null) {
            	GeoRssWhere geo = ve.getGeoCoordinates();
                MediaPlayer mediaPlayer = mediaGroup.getPlayer();
                
                String title = "";
                if (mediaGroup.getTitle() != null) {
                    title = mediaGroup.getTitle().getPlainTextContent();
                }
                
                long creationDate = -1;
                if (ve.getPublished() != null) {
                    creationDate = ve.getPublished().getValue();
                }
                
                Map<String, String> tokens = new HashMap<String, String>();

                if (mediaGroup.getDescription() != null) {
                    JSONUtils.putOptValue(tokens, "description", mediaGroup.getDescription().getPlainTextContent(), stringLimit, false);
                }

                Set<com.google.gdata.data.Category> categories = ve.getCategories();
                String categoryStr = "";
                if (categories.size() > 0) {
                    Iterator<com.google.gdata.data.Category> iter = categories.iterator();
                    com.google.gdata.data.Category category = (com.google.gdata.data.Category) iter.next();
                    if (StringUtils.isNotEmpty(category.getLabel())) {
                        if (categoryStr.length() > 0) {
                            categoryStr += ", ";
                        }
                        categoryStr += category.getLabel();
                    }
                }

                if (StringUtils.isNotEmpty(categoryStr)) {
                    tokens.put("category", categoryStr);
                }

                List<Person> authors = ve.getAuthors();
                String artist = "";
                for (Person p : authors) {
                   if (artist.length() > 0) {
                       artist += ", ";
                   }
                   artist += p.getName();
                }
                JSONUtils.putOptValue(tokens, "artist", artist, stringLimit, false);

                QualifiedCoordinates qc = new QualifiedCoordinates(geo.getLatitude(), geo.getLongitude(), 0f, 0f, 0f);
            	ExtendedLandmark landmark = LandmarkFactory.getLandmark(title, null, qc, Commons.YOUTUBE_LAYER, new AddressInfo(), creationDate, null);
            	landmark.setUrl(UrlUtils.forXML(mediaPlayer.getUrl()));
            	
            	List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
                if (thumbnails.size() > 0) {
                    MediaThumbnail thumbnail = thumbnails.get(0);
                    landmark.setThumbnail(thumbnail.getUrl());
                }
                
                Rating rating = ve.getRating();
                if (rating != null) {
                   landmark.setRating(rating.getAverage());
                }
                
                YtStatistics stats = ve.getStatistics();
                if (stats != null) {
                   landmark.setNumberOfReviews((int)stats.getViewCount());
                }
            	
            	String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            	landmark.setDescription(description);
            	
            	landmarks.add(landmark);
            }
            
        }    
    	
    	return landmarks;
    }

    private static JSONObject createCustomJSonVideoList(List<VideoEntry> vel, int version, int stringLimit) throws JSONException, UnsupportedEncodingException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        
        logger.info("Found " + vel.size() + " video entries...");

        for (VideoEntry ve : vel) {
            try {
                YouTubeMediaGroup mediaGroup = ve.getMediaGroup();

                if (mediaGroup != null) {

                    GeoRssWhere geo = ve.getGeoCoordinates();
                    MediaPlayer mediaPlayer = mediaGroup.getPlayer();

                    Map<String, Object> jsonObject = new HashMap<String, Object>();

                    String title = "";
                    if (mediaGroup.getTitle() != null) {
                        title = mediaGroup.getTitle().getPlainTextContent();
                    }
                    //if (ve.getTitle() != null && !ve.getTitle().isEmpty()) {
                    //    title = ve.getTitle().getPlainText();
                    //}
                    jsonObject.put("name", title);

                    jsonObject.put("lat", MathUtils.normalizeE6(geo.getLatitude()));
                    jsonObject.put("lng", MathUtils.normalizeE6(geo.getLongitude()));

                    if (version >= 2) {
                        jsonObject.put("url", UrlUtils.forXML(mediaPlayer.getUrl()));
                        Map<String, String> desc = new HashMap<String, String>();

                        //if (ve.getSummary() != null) {
                        //    JSONUtils.putOptValue(desc, "description", ve.getSummary().getPlainText());
                        //}

                        if (mediaGroup.getDescription() != null) {
                            JSONUtils.putOptValue(desc, "description", mediaGroup.getDescription().getPlainTextContent(), stringLimit, false);
                        }

                        Set<com.google.gdata.data.Category> categories = ve.getCategories();
                        String categoryStr = "";
                        if (categories.size() > 0) {
                            Iterator<com.google.gdata.data.Category> iter = categories.iterator();
                            com.google.gdata.data.Category category = (com.google.gdata.data.Category) iter.next();
                            if (StringUtils.isNotEmpty(category.getLabel())) {
                                if (categoryStr.length() > 0) {
                                    categoryStr += ", ";
                                }
                                categoryStr += category.getLabel();
                            }
                        }

                        if (StringUtils.isNotEmpty(categoryStr)) {
                            desc.put("category", categoryStr);
                        }

                        if (version >= 3) {
                            if (ve.getPublished() != null) {
                                desc.put("creationDate", Long.toString(ve.getPublished().getValue()));
                            }
                            List<Person> authors = ve.getAuthors();
                            String artist = "";
                            for (Person p : authors) {
                                if (artist.length() > 0) {
                                    artist += ", ";
                                }
                                artist += p.getName();
                            }
                            JSONUtils.putOptValue(desc, "artist", artist, stringLimit, false);

                            Rating rating = ve.getRating();
                            if (rating != null) {
                                desc.put("rating", rating.getAverage().toString());
                            }

                            YtStatistics stats = ve.getStatistics();
                            if (stats != null) {
                                desc.put("numberOfReviews", Long.toString(stats.getViewCount()));
                            }

                            if (version >= 4) {
                                List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
                                if (thumbnails.size() > 0) {
                                    MediaThumbnail thumbnail = thumbnails.get(0);
                                    desc.put("icon", thumbnail.getUrl());
                                }
                            }
                        }

                        if (!desc.isEmpty()) {
                            jsonObject.put("desc", desc);
                        }
                    } else {
                        jsonObject.put("desc", UrlUtils.forXML(mediaPlayer.getUrl()));
                    }

                    jsonArray.add(jsonObject);
                }
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);

        return json;
    }

	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale)	throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", latitude, longitude, query, radius, version, limit, stringLimit, flexString, flexString2);
		List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)cacheProvider.getObject(key);
        if (landmarks == null) {
        	VideoFeed videoFeed = getVideoFeed(latitude, longitude, query, radius, limit);
        	if (!videoFeed.getEntries().isEmpty()) {
        		landmarks = createCustomLandmarkVideoList(videoFeed.getEntries(), version, stringLimit, locale);
                cacheProvider.put(key, landmarks);
                logger.log(Level.INFO, "Adding YT landmark list to cache with key {0}", key);
            } else {
            	landmarks = new ArrayList<ExtendedLandmark>();
            }
        } else {
        	logger.log(Level.INFO, "Reading YT landmark list from cache with key {0}", key);
        }
		return landmarks;
	}
}
