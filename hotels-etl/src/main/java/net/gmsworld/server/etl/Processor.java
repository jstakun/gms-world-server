package net.gmsworld.server.etl;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.sf.juffrou.reflect.BeanWrapperContext;
import net.sf.juffrou.reflect.JuffrouBeanWrapper;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Processor {
	
	private static final int BATCH_SIZE = 1000;

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.print("Please provide tsv or zip file name!");
			System.exit(10);
		}
		
		ICsvBeanReader beanReader = null;
		ZipFile zf = null;
		
		try {
			Reader reader = null; 
			
			if (args[0].equals("zip")) {
				zf = new ZipFile(args[1]);
				ZipEntry ze = zf.entries().nextElement();
				if (ze != null) {
					reader = new InputStreamReader(zf.getInputStream(ze));
				}
			} else if (args[0].equals("tsv")) {
				reader = new FileReader(args[1]);
			}
			
		    beanReader = new CsvBeanReader(
		            reader,
		            new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE)
		                    .surroundingSpacesNeedQuotes(true).build());

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

		    int count = 0;
		    int errors = 0;
		    int batchSize = 0;
		    FeatureCollection featureCollection = new FeatureCollection();
		    JuffrouBeanWrapper beanWrapper = new JuffrouBeanWrapper(BeanWrapperContext.create(HotelBean.class));
		    
		    //max 400000, total 367885
		    //for (int i=0;i<100000;i++) {
		    while (true) {
		    	try {
		    		HotelBean h = beanReader.read(HotelBean.class, header, processors);
		    		if (h == null) {
						break;
					}
		    		count++;
		    		batchSize++;
		    		h.setHotel_url(h.getHotel_url() + "?aid=864525");
		    		h.setPhoto_url(h.getPhoto_url().replace("max500", "max200"));
		    		
		    		Feature f = new Feature();
	    			Point p = new Point();
	    			p.setCoordinates(new LngLatAlt(h.getLongitude(), h.getLatitude()));
	    			h.setLatitude(null);
	    			h.setLongitude(null);
	    			f.setGeometry(p);	    			
		    		f.setId(Long.toString(h.getId()));
		    		
		    		Map<String, Object> properties = getBeanMap(h, beanWrapper);
		    		f.setProperties(properties);
		    		
		    		featureCollection.add(f);
		    		
		    		if (batchSize == BATCH_SIZE) {
		    			saveBatchToDb(featureCollection);
		    			featureCollection.setFeatures(new ArrayList<Feature>());
		    			batchSize = 0;
		    		}
		    		
		    	} catch (Exception e) {
		    		errors++;
		    		System.err.println(e.getMessage());
		    		//e.printStackTrace();
		    	}		        
		    }
		   
		    if (batchSize > 0) {
    			saveBatchToDb(featureCollection);
    		}
		    
		    System.out.println("Processed " + count + " records with " + errors + " errors.");

		} finally {
			if (beanReader != null) {
				beanReader.close();
			}
			if (zf != null) {
				zf.close();
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
	    	//System.out.println("Setting " + type + ": " + beanWrapper.getValue(propertyName));
	        beanMap.put(propertyName, beanWrapper.getValue(propertyName));
		}
	    return beanMap;
	}
	
	private static void saveBatchToDb(FeatureCollection featureCollection) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
	    String json = mapper.writeValueAsString(featureCollection).replace("id", "_id");
		
	    System.out.println("Saving to db batch of " + featureCollection.getFeatures().size() + "...");
		
		//load to db 		    		
	    try {
	    	URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/camel/v1/cache/multi/hotels");
			String resp = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "POST", null, json, "application/json", Commons.getProperty(Property.RH_GMS_USER));
			System.out.println("Cache response: " + resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    System.out.println("Done.");
	}
}
