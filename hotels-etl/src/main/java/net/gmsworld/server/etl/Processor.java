package net.gmsworld.server.etl;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class Processor {

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
		    //List<String> columnsToMap = Arrays.asList("name", "address", "id");
		    for (int i = 0; i < header.length; i++) {
		    	System.out.print(header[i] + " ");
		        //if (!columnsToMap.contains(header[i])) {
		        //    header[i] = null;
		        //}
		    	if (header[i] != null && header[i].equals("class")) {
		    		header[i] = "stars";
		    	} 
		    }

		    Hotel h;
		    int count = 0;
		    int errors = 0;
		    while (true) {
		    	try {
		    		h = beanReader.read(Hotel.class, header, processors);
		    		if (h == null) {
		    			break;
		    		} else {
		    			count++;
		    	    }	
		    		System.out.println(h);
		    	} catch (Exception e) {
		    		errors++;
		    		//System.err.println(e.getMessage());
		    		e.printStackTrace();
		    	}		        
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
            new NotNull(),   //address            
            new Optional(),  //zip            
            new Optional(),  //city_hotel           
            new Optional(),  //cc1            
            new Optional(),  //ufi            
            new Optional(new ParseDouble()),  //class
            new Optional(),  //currencycode
            new Optional(new ParseDouble()), //minrate
            new Optional(new ParseDouble()), //maxrate
            new Optional(new ParseInt()),  //preffered
            new ParseInt(), //nr_rooms
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

}
