/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jstakun
 */
public class KMLParser extends DefaultHandler {

    private final String PLACEMARK = "Placemark";
    private final String NAME = "name";
    private final String DESCRIPTION = "description";
    private final String LATITUDE = "lat";
    private final String LONGITUDE = "lng";
    private final String COORDINATES = "coordinates";
    private String currentElement;
    private int level = 0, elementLevel = -1;
    private ArrayList jsonArray = new ArrayList();
    private Map jsonObject;
    private String urlAttr;

    public KMLParser(int apiVersion) {
        if (apiVersion == 2) {
            urlAttr = "url";
        } else {
            urlAttr = "desc";
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        level++;
        if (PLACEMARK.equals(localName)) {
            jsonObject = new HashMap();
            elementLevel = level;
//            System.out.println("New " + localName);
        } else if (DESCRIPTION.equals(localName) || NAME.equals(localName) || COORDINATES.equals(localName)) {
            currentElement = localName;
 //           System.out.println("startElement: " + localName);
        } else {
            //System.out.println("startElement: " + localName);
            currentElement = "";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (level == elementLevel + 1) {
            if (DESCRIPTION.equals(currentElement) && !jsonObject.containsKey(urlAttr)) {
                jsonObject.put(urlAttr, getLandmarkPageURL(new String(ch, start, length)));
            } else if (NAME.equals(currentElement) && !jsonObject.containsKey(NAME)) {
                jsonObject.put(NAME, new String(ch, start, length));
            }
        } else if (level == elementLevel + 2) {
            if (COORDINATES.equals(currentElement) && !jsonObject.containsKey(LATITUDE) && !jsonObject.containsKey(LONGITUDE)) {
                parseCoordinates(new String(ch, start, length));
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        level--;
        if (PLACEMARK.equals(localName)) {
            if (jsonObject != null && jsonObject.containsKey(urlAttr) && jsonObject.containsKey(NAME) && jsonObject.containsKey(LATITUDE) && jsonObject.containsKey(LONGITUDE)) {
                jsonArray.add(jsonObject);
            }
        } else if (DESCRIPTION.equals(localName) || NAME.equals(localName) || COORDINATES.equals(localName)) {
            currentElement = "";
        }

    }

    public List getJSonArray() {
        return jsonArray;
    }

    private String getLandmarkPageURL(String description) {
        String url = "";

        int start = description.indexOf("<a href=\"http://www.panoramio.com/photo/");
        int end = description.indexOf('"', start + 15);
        url = description.substring(start + 9, end);
        url = url.replace(".com", ".com/m");

        return url;
    }

    private void parseCoordinates(String content) {
        int subStart = 0;
        int p = 0;
        int length = content.length();
        // EclipseME won't allow a double [] array
        double[] parts = new double[3];

        for (int i = 0; i < length; i++) {
            char character = content.charAt(i);
            if ((i != 0 && ((character == ',') || (character == ' ') || (character == '\n'))) || (i == length - 1)) {
                // end of segment reached
                int end = i;
                if (i == length - 1) {
                    end++;
                }
                String part = content.substring(subStart, end);
                //System.out.println("-" + part + "- -" + p + "- " + i + " " + length + " " + subStart + " " + end);
                try {
                    parts[p] = Double.parseDouble(part);
                } catch (NumberFormatException e) {
                    if (p == 2) {
                        parts[p] = 0.0d;
                    } else {
                        throw e;
                    }
                }
                p++;
                subStart = i + 1;

                if (p == 2 && i == length - 1) {
                    parts[p] = 0.0d;
                    p++;
                }

                if (p == 3) {
                    //System.out.println("Adding new coords");
                    jsonObject.put(LATITUDE, parts[1]);
                    jsonObject.put(LONGITUDE, parts[0]);
                }
            }
        }
    }
}
