package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.Query;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.Category;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.ServiceException;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class PicasaUtils extends LayerHelper {

    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String bbox, String flexString2) throws MalformedURLException, IOException, ServiceException, JSONException {

        double lat = 0d, lng = 0d;
        String[] coords = StringUtils.split(bbox, ",");
        if (coords.length == 4) {
            lat = (Double.parseDouble(coords[2]) + Double.parseDouble(coords[0])) / 2;
            lng = (Double.parseDouble(coords[3]) + Double.parseDouble(coords[1])) / 2;
        }

        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, bbox, flexString2);

        String cachedResponse = cacheProvider.getString(key);

        if (cachedResponse == null) {
            PicasawebService myService = new PicasawebService("Landmark Manager");
            URL baseSearchUrl = new URL("https://picasaweb.google.com/data/feed/api/all");
            Query myQuery = new Query(baseSearchUrl);
            myQuery.setStringCustomParameter("kind", "photo");
            myQuery.setStringCustomParameter("bbox", bbox); //west, south, east, north i.e. "50.0,20.0,53.0,23.0"
            myQuery.setPublishedMin(new DateTime(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 365)));
            myQuery.setMaxResults(limit);
            if (StringUtils.isNotEmpty(query)) {
                myQuery.setFullTextQuery(query);
            }

            AlbumFeed searchResultsFeed = myService.query(myQuery, AlbumFeed.class);
            JSONObject output = createCustomJSonPicasaPhotoList(searchResultsFeed.getPhotoEntries(), version, stringLimit);

            if (!searchResultsFeed.getPhotoEntries().isEmpty()) {
                cacheProvider.put(key, output.toString());
                logger.log(Level.INFO, "Adding PC landmark list to cache with key {0}", key);
            }

            return output;

        } else {
            logger.log(Level.INFO, "Reading PC landmark list from cache with key {0}", key);
            return new JSONObject(cachedResponse);
        }
    }

    private static JSONObject createCustomJSonPicasaPhotoList(List<PhotoEntry> pel, int version, int stringLimit) throws JSONException, ServiceException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < pel.size(); i++) {
            PhotoEntry photo = pel.get(i);

            if (photo.getGeoLocation() != null) {

                Map<String, Object> jsonObject = new HashMap<String, Object>();

                jsonObject.put("name", photo.getTitle() != null ? photo.getTitle().getPlainText() : "");
                jsonObject.put("lat", photo.getGeoLocation().getLatitude());
                jsonObject.put("lng", photo.getGeoLocation().getLongitude());

                if (version >= 2) {
                    Map<String, String> desc = new HashMap<String, String>();

                    if (version >= 3) {
                        if (photo.getTimestamp() != null) {
                        	SimpleDateFormat formatter = DateUtils.getSimpleDateFormat(dateFormat);
                            JSONUtils.putOptDate(desc, "taken_date", formatter.format(photo.getTimestamp()), formatter);
                        }
                        List<Person> authors = photo.getAuthors();
                        String artist = "";
                        for (Person p : authors) {
                            if (artist.length() > 0) {
                                artist += ", ";
                            }
                            artist += p.getName();
                        }
                        JSONUtils.putOptValue(desc, "artist", artist, stringLimit, false);

                        Long viewCount = photo.getViewCount();
                        if (viewCount != null) {
                            desc.put("viewCount", viewCount.toString());
                        }
                        Integer totalStars = photo.getTotalStars();
                        if (totalStars != null) {
                            desc.put("totalStars", totalStars.toString());
                        }

                        if (version >= 4) {
                            List<MediaThumbnail> thumbnails = photo.getMediaThumbnails();
                            if (thumbnails.size() > 0) {
                                MediaThumbnail thumbnail = thumbnails.get(0);
                                desc.put("icon", thumbnail.getUrl());
                            }
                        }
                    }

                    Set<Category> categories = photo.getCategories();
                    if (categories.size() > 0) {
                        Iterator<Category> iter = categories.iterator();
                        Category category = iter.next();
                        if (category.getLabel() != null && category.getLabel().length() > 0) {
                            desc.put("category", category.getLabel());
                        }
                    }

                    if (photo.getSummary() != null) {
                        JSONUtils.putOptValue(desc, "description", photo.getSummary().getPlainText(), stringLimit, false);
                    }

                    if (!desc.isEmpty()) {
                        jsonObject.put("desc", desc);
                    }

                    jsonObject.put("url", photo.getHtmlLink().getHref());
                } else {
                    jsonObject.put("desc", photo.getHtmlLink().getHref());
                }


                jsonArray.add(jsonObject);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String bbox, String flexString2, Locale locale, boolean useCache) throws Exception {
		double lat = 0d, lng = 0d;
        String[] coords = StringUtils.split(bbox, ",");
        //set new bbox
        
        double[] dcoords = new double[]{0d, 0d, 0d, 0d};
        if (coords.length == 4) {
        	dcoords[0] = Double.parseDouble(coords[0]);
        	dcoords[1] = Double.parseDouble(coords[1]);
        	dcoords[2] = Double.parseDouble(coords[2]);
        	dcoords[3] = Double.parseDouble(coords[3]);
        	
            lat = (dcoords[2] + dcoords[0]) / 2;
            lng = (dcoords[3] + dcoords[1]) / 2;
        }

        if ((dcoords[2] - dcoords[0]) < 0.1) {
        	dcoords[2] += 0.05;
        	dcoords[0] -= 0.05;
        }
        
        if ((dcoords[3] - dcoords[1]) < 0.1) {
        	dcoords[3] += 0.05;
        	dcoords[1] -= 0.05;
        }
        
        String normalizedBbox = MathUtils.normalizeE2(dcoords[0]) + "," +
        		MathUtils.normalizeE2(dcoords[1]) + "," +
        		MathUtils.normalizeE2(dcoords[2]) + "," +
        		MathUtils.normalizeE2(dcoords[3]);    
        //
        
        logger.log(Level.INFO, "Searching for pictures in " + bbox);
        
        String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, normalizedBbox, flexString2);

        List<ExtendedLandmark> output = (List<ExtendedLandmark>)cacheProvider.getObject(key);

        if (output == null) {
            PicasawebService myService = new PicasawebService("Landmark Manager");
            URL baseSearchUrl = new URL("https://picasaweb.google.com/data/feed/api/all");
            Query myQuery = new Query(baseSearchUrl);
            myQuery.setStringCustomParameter("kind", "photo");
            myQuery.setStringCustomParameter("bbox", normalizedBbox); //west, south, east, north i.e. "50.0,20.0,53.0,23.0"
            myQuery.setPublishedMin(new DateTime(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 365)));
            myQuery.setStringCustomParameter("thumbsize", "104u"); //32, 48, 64, 72, 104, 144, 150, 160
            myQuery.setMaxResults(limit);
            if (StringUtils.isNotEmpty(query)) {
                myQuery.setFullTextQuery(query);
            }

            //System.out.println("Calling: " + myQuery.getFeedUrl().toExternalForm() + myQuery.getQueryUri());
            
            AlbumFeed searchResultsFeed = myService.query(myQuery, AlbumFeed.class);
            
            //System.out.println("Found: " +  searchResultsFeed.getTotalResults() + " " + 
            //		searchResultsFeed.getItemsPerPage() + " " + searchResultsFeed.getStartIndex());
            
            //photos-meta.jar
            
            //List<GphotoEntry> entries = searchResultsFeed.getEntries();
  
            //List<PhotoEntry> photos = new ArrayList<PhotoEntry>(entries.size());
            
            logger.log(Level.INFO, "Found: " + searchResultsFeed.getTotalResults() + "-" + searchResultsFeed.getPhotoEntries().size() + " entries in normalized bbox: " + normalizedBbox);
            
            //for (GphotoEntry<PhotoEntry> entry : entries) {
            //	PhotoEntry p = new PhotoEntry(entry);
            //	photos.add(p);
            //}
            
            output = createLandmarksPicasaPhotoList(searchResultsFeed.getPhotoEntries(), stringLimit, locale);
            
            if (!output.isEmpty()) {
                cacheProvider.put(key, output);
                logger.log(Level.INFO, "Adding PC landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading PC landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Found {0} landmarks", output.size()); 
        
        return output;
	}
	
	private static List<ExtendedLandmark> createLandmarksPicasaPhotoList(List<PhotoEntry> pel, int stringLimit, Locale locale) throws JSONException, ServiceException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        for (int i = 0; i < pel.size(); i++) {
            PhotoEntry photo = pel.get(i);

            if (photo.getGeoLocation() != null) {

                String name = photo.getTitle() != null ? photo.getTitle().getPlainText() : "No name";
                double lat = photo.getGeoLocation().getLatitude();
                double lng = photo.getGeoLocation().getLongitude();
                String url = photo.getHtmlLink().getHref();
               
                Map<String, String> tokens = new HashMap<String, String>();
 
                if (photo.getTimestamp() != null) {
                	SimpleDateFormat formatter = DateUtils.getSimpleDateFormat(dateFormat);
                    JSONUtils.putOptDate(tokens, "taken_date", formatter.format(photo.getTimestamp()), formatter);
                }
                        
                List<Person> authors = photo.getAuthors();
                String artist = "";
                for (Person p : authors) {
                    if (artist.length() > 0) {
                         artist += ", ";
                    }
                    artist += p.getName();
                }
                JSONUtils.putOptValue(tokens, "artist", artist, stringLimit, false);

                Long viewCount = photo.getViewCount();
                if (viewCount != null) {
                    tokens.put("viewCount", viewCount.toString());
                }
                Integer totalStars = photo.getTotalStars();
                if (totalStars != null) {
                    tokens.put("totalStars", totalStars.toString());
                }

                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.PICASA_LAYER, new AddressInfo(), -1, null);
                landmark.setUrl(url); 
                        
                List<MediaThumbnail> thumbnails = photo.getMediaThumbnails();
                if (!thumbnails.isEmpty()) {
                    MediaThumbnail thumbnail = thumbnails.get(0);
                    landmark.setThumbnail(thumbnail.getUrl());
                }                       

                Set<Category> categories = photo.getCategories();
                if (!categories.isEmpty()) {
                    Iterator<Category> iter = categories.iterator();
                    Category category = iter.next();
                    if (category.getLabel() != null && category.getLabel().length() > 0) {
                       tokens.put("category", category.getLabel());
                    }
                }

                if (photo.getSummary() != null) {
                    JSONUtils.putOptValue(tokens, "description", photo.getSummary().getPlainText(), stringLimit, false);
                }
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
				
                landmarks.add(landmark);
            }
        }

        return landmarks;
    }
	
    public String getLayerName() {
    	return Commons.PICASA_LAYER;
    }
}
