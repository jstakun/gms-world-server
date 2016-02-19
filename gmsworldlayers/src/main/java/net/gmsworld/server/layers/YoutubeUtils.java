package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.GeoPoint;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
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

    private static YouTube getYouTube() {
    	HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
    	
    	return new YouTube.Builder(httpTransport, jsonFactory, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("GMS World").build();
    }
    
    private static List<Video> getVideosV3(double latitude, double longitude, String query, int radius, int limit) throws MalformedURLException, IOException, ServiceException {
    	
    	YouTube.Search.List search = getYouTube().search().list("snippet");
    	
        search.setKey(Commons.getProperty(Property.GOOGLE_API_KEY));
        search.setType("video");

        if (StringUtils.isNotEmpty(query)) {
            search.setQ(query);
        }
        
        search.setLocation(StringUtil.formatCoordE2(latitude) + "," + StringUtil.formatCoordE2(longitude));
        
        int r = NumberUtils.normalizeNumber(radius, 1, 999);
        search.setLocationRadius(r + "mi");
        
        int l = NumberUtils.normalizeNumber(limit, 1, 50);
        search.setMaxResults(new Long(l));
        
        search.setOrder("date"); //("viewCount");
        //search.setPublishedAfter(DateTime.)
        
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();
        
        if (!searchResultList.isEmpty()) {
        
        	List<String> ids = new ArrayList<String>(searchResultList.size());
            
            for (SearchResult res : searchResultList) {
            	ids.add(res.getId().getVideoId());
            }
            
          	YouTube.Videos.List videos = getYouTube().videos().list("recordingDetails, statistics, snippet");
            videos.setKey(Commons.getProperty(Property.GOOGLE_API_KEY));
        	
        	videos.setId(StringUtils.join(ids, ","));
        
        	VideoListResponse videosResponse = videos.execute();
        	List<Video> videosList = videosResponse.getItems();
        	
        	
        	//load next batch up to 100 videos in total
        	try {
        		if (ids.size() < limit)
        		{
        			String nextPageToken = searchResponse.getNextPageToken();
        			if (StringUtils.isNotEmpty(nextPageToken)) {
        				search.setPageToken(nextPageToken);
        				l = NumberUtils.normalizeNumber(limit - ids.size(), 1, 50);
        				search.setMaxResults(new Long(l));
        				searchResponse = search.execute();
        				searchResultList = searchResponse.getItems();
        				if (!searchResultList.isEmpty()) {
        					ids.clear();
        					for (SearchResult res : searchResultList) {
        						ids.add(res.getId().getVideoId());
        					}
        					videos.setId(StringUtils.join(ids, ","));
        					videosResponse = videos.execute();
        					videosList.addAll(videosResponse.getItems());
        				}	
        			}
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        	}
            
           logger.log(Level.INFO, "Loaded " + videosList.size() + " videos.");
        	
        	return videosList;
        } else {
        	return new ArrayList<Video>();
        }
    }

    @Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flex, String flexString2) throws MalformedURLException, IOException, ServiceException, JSONException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flex, flexString2);

        String output = cacheProvider.getString(key);
        JSONObject json = null;

        if (output == null) {
            VideoFeed videoFeed = new VideoFeed(); //getVideoFeed(latitude, longitude, query, radius, limit);
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

    private static List<ExtendedLandmark> createCustomVideoV3LandmarkList(List<Video> videoList, int stringLimit, Locale locale, double lat, double lng) {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	logger.info("Found " + videoList.size() + " video entries...");
    	
    	for (Video video : videoList) {
    		VideoSnippet snippet = video.getSnippet();
    		
    		String url = "https://www.youtube.com/watch?v=" + video.getId();
    		
    		String title = snippet.getTitle();
    		long creationDate = snippet.getPublishedAt().getValue();
    		String desc = snippet.getDescription();
    		
    		String thumbnail = snippet.getThumbnails().getDefault().getUrl();
    		
    		QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);;
    		if (video.getRecordingDetails() != null) {
    			GeoPoint location = video.getRecordingDetails().getLocation();
    			if (location != null && location.getLatitude() != null && location.getLongitude() != null) {
    				qc = new QualifiedCoordinates(location.getLatitude(), location.getLongitude(), 0f, 0f, 0f);
    			} 
    		}
    		ExtendedLandmark landmark = LandmarkFactory.getLandmark(title, null, qc, Commons.YOUTUBE_LAYER, new AddressInfo(), creationDate, null);
        	landmark.setUrl(url);
        	landmark.setThumbnail(thumbnail);
        	
        	Map<String, String> tokens = new HashMap<String, String>();
        	if (desc != null) {
        		JSONUtils.putOptValue(tokens, "description", desc, stringLimit, false);
        	}
        	
        	String artist = snippet.getChannelTitle();
        	if (artist != null) {
        		JSONUtils.putOptValue(tokens, "artist", artist, stringLimit, false);
        	}   	
        	
        	VideoStatistics stats = video.getStatistics();
        	int likes = 0;
        	int dislikes = 0;
        	if (stats != null) {
        		int views = stats.getViewCount().intValue();
        		if (views > 0) {
        			tokens.put("Views", Integer.toString(views));
        		}
        		//stats.getCommentCount();
        		if (stats.getLikeCount() != null) {
        			likes = stats.getLikeCount().intValue(); //"\uD83D\uDC4D"
        		} 
        		if (stats.getDislikeCount() != null) {
        			dislikes = stats.getDislikeCount().intValue(); //"\uD83D\uDD93"
        		} 
        		//stats.getFavoriteCount();
        	}
        	
        	if (likes > 0 || dislikes > 0) {
        		tokens.put("Likes", Integer.toString(likes));
        		tokens.put("Dislikes", Integer.toString(dislikes));
        	}
        	
        	landmark.setNumberOfReviews(likes + dislikes);
        	String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        	landmark.setDescription(description);
        	
        	landmarks.add(landmark);
    	}
    	
    	return landmarks;

    }
    
    /*private static List<ExtendedLandmark> createCustomLandmarkVideoList(List<VideoEntry> vel, int version, int stringLimit, Locale locale) {
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
    }*/

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
	public List<ExtendedLandmark> loadLandmarks(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache)	throws Exception {
		List<Video> videos = getVideosV3(latitude, longitude, query, radius, limit);
        return createCustomVideoV3LandmarkList(videos, stringLimit, locale, latitude, longitude); 
	}
	
	public String getLayerName() {
		return Commons.YOUTUBE_LAYER;
	}
}
