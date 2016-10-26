package net.gmsworld.server.etl;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.sf.juffrou.reflect.BeanWrapperContext;
import net.sf.juffrou.reflect.JuffrouBeanWrapper;

import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.json.JSONArray;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Processor {
	
	private static final String HOTELS_POST_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/multi/hotels"; 
	private static final String HOTELS_GET_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/hotels/_id/"; 
	private static URL cachePostUrl;
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String HOTELS_FILE = "/tmp/hotels.zip";
	
	//last count 1038551
	private static final int BATCH_SIZE = 2000; 
	
	private static final String AID = "864525";
	
	private static int count = 0;
	private static int errors = 0;
   
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.print("Please provide tsv or zip file name!");
			System.exit(10);
		}
		
		boolean dryrun = true;
		boolean compare = false;
		
		if (args.length >= 3) {
			try {
				dryrun = Boolean.valueOf(args[2]).booleanValue();
				if (args.length >= 4) {
					compare = Boolean.valueOf(args[3]).booleanValue();
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		
		System.out.println("Dry run is set to: " + dryrun);
		System.out.println("Records will be compared with database: " + compare);
		
		CsvBeanReader beanReader = null;
		ZipFile zf = null;
		
		try {
			Reader reader = null; 
			cachePostUrl = new URL(HOTELS_POST_URL);
			
			if (args[0].equals("zip")) {
				zf = new ZipFile(args[1]);
				List<? extends ZipEntry> zipFiles = Collections.list(zf.entries());
				for(ZipEntry ze : zipFiles) {
					if (ze != null) {
						reader = new InputStreamReader(zf.getInputStream(ze));
						beanReader = new CsvBeanReader(reader, new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).surroundingSpacesNeedQuotes(true).build());
						processFile(beanReader, dryrun, compare);
					}
				}	
			} else if (args[0].equals("tsv")) {
				reader = new FileReader(args[1]);
				beanReader = new CsvBeanReader(reader, new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).surroundingSpacesNeedQuotes(true).build());
				processFile(beanReader, dryrun, compare);
			} else if (args[0].equals("web")) {
				try {
					downloadHotelsZip(args[1]);
					zf = new ZipFile(HOTELS_FILE);
					List<? extends ZipEntry> zipFiles = Collections.list(zf.entries());
					for(ZipEntry ze : zipFiles) {
						if (ze != null) {
							reader = new InputStreamReader(zf.getInputStream(ze));
							beanReader = new CsvBeanReader(reader, new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).surroundingSpacesNeedQuotes(true).build());
							processFile(beanReader, dryrun, compare);
						}
					}	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (zf != null) {
				zf.close();
			}
		}
	}
	
	private static void processFile(CsvBeanReader beanReader, boolean dryrun, boolean compare) throws IOException {
		try {
			final String[] header = beanReader.getHeader(true);
		    
		    //filter columns
		    List<String> columnsToMap = Arrays.asList("header_to_exclude");
		    for (int i = 0; i < header.length; i++) {
		    	//System.out.print(header[i] + " ");
		    	if (header[i] != null && header[i].startsWith("desc_")) {
		    		header[i] = null;
		    	}
		    	if (columnsToMap.contains(header[i])) {
		    		header[i] = null; 
		        }
		    	if (header[i] != null && header[i].equals("class")) {
		    		header[i] = "stars";
		    	}
		    }

		    int batchSize = 0;
		    FeatureCollection featureCollection = new FeatureCollection();
		    JuffrouBeanWrapper beanWrapper = new JuffrouBeanWrapper(BeanWrapperContext.create(HotelBean.class));
		    
		    while (true) {
		    	try {
		    		count++;
		    		HotelBean h = beanReader.read(HotelBean.class, header, processors);
		    		if (h == null) {
						break;
					}
		    		h.setHotel_url(h.getHotel_url() + "?aid=" + AID);
		    		h.setPhoto_url(h.getPhoto_url().replace("max500", "max200"));
		    		
		    		Feature f = new Feature();
	    			Point p = new Point();
	    			LngLatAlt coords = new LngLatAlt(h.getLongitude(), h.getLatitude());
	    			p.setCoordinates(coords);
	    			h.setLatitude(null);
	    			h.setLongitude(null);
	    			f.setGeometry(p);	    			
		    		f.setId(Long.toString(h.getId()));
		    		Map<String, Object> properties = getBeanMap(h, beanWrapper);
		    		
		    		//compare with current version
		    		boolean equal = false; 
		    		if (compare) {
		    			equal = compareHotelBean(properties, beanWrapper, count);
		    		}
		    			    		
	    			if (!equal) {	
	    				System.out.println(count + ". Hotel " + h.getId() + " added to batch.");
	    				batchSize++;
	    				f.setProperties(properties);
	    				featureCollection.add(f);
	    			} else {
	    				System.out.println(count + ". Hotel " + h.getId() + " has not changed.");
	    			}
	    			
		    		if (batchSize == BATCH_SIZE) {
		    			saveBatchToDb(featureCollection, dryrun);
		    			featureCollection.setFeatures(new ArrayList<Feature>());
		    			batchSize = 0;
		    			System.out.println("Processed " + count + " records ...");
		    		}
		    		
		    	} catch (Exception e) {
		    		errors++;
		    		System.err.println(e.getMessage());
		    		//e.printStackTrace();
		    	}		        
		    }
		   
		    if (batchSize > 0) {
		    	saveBatchToDb(featureCollection, dryrun);
    		}
		    
		    System.out.println("Processed " + count + " records with " + errors + " errors.");

		} finally {
			if (beanReader != null) {
				beanReader.close();
			}	
		}
	}
	
	private static final CellProcessor[] processors = new CellProcessor[] { 
            new ParseLong(), //id
            new NotNull(),   //name           
            new Optional(),   //address            
            new Optional(),  //zip            
            new Optional(),  //city_hotel           
            new Optional(),  //cc1            
            new Optional(),  //ufi            
            new Optional(new ParseDouble()),  //class
            new Optional(),  //currencycode
            new Optional(new ParseDouble()), //minrate
            new Optional(new ParseDouble()), //maxrate
            new Optional(new ParseInt()),  //preffered
            new Optional(new ParseInt()), //nr_rooms
            new ParseDouble(), //longitude 
            new ParseDouble(), //latitude
            new ParseInt(), //public_ranking         
            new Optional(), //hotel_url             
            new Optional(), //photo_url             
            new Optional(), //desc_en             
            new Optional(), //desc_fr            
            new Optional(), //desc_es 
            new Optional(), //desc_de
            new Optional(), //desc_nl            
            new Optional(), //desc_it 
            new Optional(), //desc_pt              
            new Optional(), //desc_ja           
            new Optional(), //desc_zh            
            new Optional(), //desc_pl            
            new Optional(), //desc_ru             
            new Optional(), //desc_sv           
            new Optional(), //desc_ar 
            new Optional(), //desc_el 
            new Optional(), //desc_no            
            new Optional(), //city_unique            
            new Optional(), //city_preferred            
            new ParseInt(), //continent_id 
            new ParseInt(),//review_score 
            new ParseInt(),//review_nr
            new Optional(),
    };

	private static Map<String, Object> getBeanMap(Object bean, JuffrouBeanWrapper beanWrapper) {
	    Map<String, Object> beanMap = new HashMap<String, Object>();
	    beanWrapper.setBean(bean);
	    for(String propertyName : beanWrapper.getPropertyNames()) {
	    	//Type type = beanWrapper.getType(propertyName);
	    	//System.out.println("Setting " + type + " " + propertyName + ": " + beanWrapper.getValue(propertyName));
	        beanMap.put(propertyName, beanWrapper.getValue(propertyName));
		}
	    return beanMap;
	}
	
	private static void saveBatchToDb(FeatureCollection featureCollection, boolean dryrun) throws JsonProcessingException {
		long start = System.currentTimeMillis();
		System.out.println("Saving to db batch of " + featureCollection.getFeatures().size() + "...");
		
		String json = mapper.writeValueAsString(featureCollection).replace("\"id\"", "\"_id\"");
		//load to db 		    	
		if (!dryrun) {
			try {
	    		String resp = HttpUtils.processFileRequestWithBasicAuthn(cachePostUrl, "POST", null, json, "application/json", Commons.getProperty(Property.RH_GMS_USER));
				System.out.println("Cache response: " + resp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println(json);
	    System.out.println("Done in " + (System.currentTimeMillis()-start) + " milliseconds.");
	}
	
	private static HotelBean jsonToHotelBean(Long id) throws JsonParseException, JsonMappingException, IOException {
		URL cacheGetUrl = new URL(HOTELS_GET_URL + id);
		String resp = HttpUtils.processFileRequestWithBasicAuthn(cacheGetUrl, "GET", null, null, "application/json", Commons.getProperty(Property.RH_GMS_USER));
		if (StringUtils.startsWith(resp, "[")) {
			JSONArray root = new JSONArray(resp);
			if (root.length() > 0) {
				String json = root.getJSONObject(0).getJSONObject("properties").toString();
				return mapper.readValue(json.replace("_id", "id"), HotelBean.class);
			} else {
				//System.out.println("Received following server response for id " + id + ": " + resp);
				return null;
			}
		} else {
			//System.out.println("Received following server response for id " + id + ": " + resp);
			return null;
		}
	}
	
	private static boolean compareHotelBean(Map<String, Object> properties, JuffrouBeanWrapper beanWrapper, int count) throws JsonParseException, JsonMappingException, IOException {
		Long id = (Long)properties.get("id");
		HotelBean old = jsonToHotelBean(id);
		boolean equal = true;
		if (old != null) {
			Map<String, Object> oldproperties = getBeanMap(old, beanWrapper);
			for (String key : properties.keySet()) {
				//System.out.println("Comparing: " + key);
				if (!key.equals("creationDate") && oldproperties.containsKey(key)) {
					Object oldval = oldproperties.get(key);
					Object val = properties.get(key);
					if (val != null && oldval != null &&  !val.equals(oldval)) {
						equal = false;
						System.out.println("Object: " + id + " -> " + key + " new: " + val + ", old: " + oldval);
					} 
				} else if (!key.equals("creationDate")) {
					System.out.println("Missing property " + key);
					equal = false;
				}
			}
		} else {
			System.out.println(count + ". Hotel " + id + ": " + properties.get("name") + " not found.");
			equal = false;
		}
		return equal;
	}
	
	private static void downloadHotelsZip(String zipurl) throws IOException {
	    URL url = new URL(zipurl);
	    HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    System.out.println("Starting downloading file " + zipurl);
	    // Check for errors
	    int responseCode = con.getResponseCode();
	    InputStream inputStream = null;
	    if (responseCode == HttpURLConnection.HTTP_OK) {
	    	System.out.println("File found");
	        inputStream = con.getInputStream();
	    } else {
	    	System.out.println("File download failed with response code " + responseCode);
	    }
	    
	    if (inputStream != null) {
	    	OutputStream output = new FileOutputStream(HOTELS_FILE);

	    	byte[] buffer = new byte[8 * 1024]; // 8 KB
	    	int bytesRead;
	    	int count = 0;
	    	//expected ~ 646 000 000
	    	while ((bytesRead = inputStream.read(buffer)) > 0) {
	    		output.write(buffer, 0, bytesRead);
	    		count += bytesRead;
	    		System.out.println(count);
	    	}

	    	output.close();
	    	inputStream.close();
	    }
	}
}
