/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.xml;

import com.jstakun.lm.server.utils.persistence.HotelPersistenceUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jstakun
 */
public class HotelParser extends DefaultHandler {

    public static final int CREATE = 0;
    public static final int UPDATE = 1;
    public static final int NONE = 2;
    public static final int TOTAL = 3;
    
    private static final String ROOT = "hotel";
    private String currentElement;
    private Map<String, String> hotelMap;
    private int[] counters;
    private int mode = UPDATE;
    private static final Logger logger = Logger.getLogger(HotelParser.class.getName());
    private long startTime;

    public HotelParser(int mode) {
        super();
        this.mode = mode;
        counters = new int[4];
        for(int i=0;i<4;i++){
           counters[i] = 0;
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (ROOT.equals(localName)) {
            hotelMap = new HashMap<String, String>();
        } else {
            currentElement = localName;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (length > 0) {

            String s = new String(ch, start, length);

            if (StringUtils.isNotEmpty(currentElement) && hotelMap != null) { 
                String tmp = "";
                String propertyName = StringUtils.uncapitalize(currentElement);
                if (hotelMap.containsKey(propertyName)) {
                    tmp = hotelMap.get(propertyName);
                }
                tmp += s;
                hotelMap.put(propertyName, tmp);
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (ROOT.equals(localName)) {
            if (mode == CREATE) {
                HotelPersistenceUtils.persistHotel(hotelMap);
                counters[CREATE]++;
            } else {
                int response = HotelPersistenceUtils.updateHotel(hotelMap);
                counters[response]++;
            }
            counters[TOTAL]++;
            if ((counters[TOTAL] % 1000) == 0) {
                logStatus();
            }
            hotelMap.clear();
            hotelMap = null;
        }
        currentElement = "";
    }

    @Override
    public void endDocument() {
        logStatus();
    }

    private void logStatus() {
        long interval = (System.currentTimeMillis() - startTime);
        logger.log(Level.INFO, "Processed {0} records in {1}ms.", new Object[]{counters[TOTAL], interval});
    }

    public int[] getCounters() {
        return counters;
    }
}
