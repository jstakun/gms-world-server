/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.xml;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jstakun
 */
public class DefaultParser extends DefaultHandler {

    private String root;
    private Hashtable elements;
    private String currentElement;
    private int level = 0, eventLevel = -1;
    private ArrayList jsonArray = new ArrayList();
    private Map jsonObject;

    public DefaultParser(String root, Hashtable elements) {
        this.root = root;
        this.elements = elements;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        level++;
        if (root.equals(localName)) {
            jsonObject = new HashMap();
            eventLevel = level;
            //System.out.println("New " + localName);
        } else if (inElementArray(localName)) {
            currentElement = localName;
            //System.out.println("startElement: " + localName);
        } else {
            //System.out.println("startElement: " + localName);
            currentElement = "";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (level == eventLevel + 1 && inElementArray(currentElement) && !jsonObject.containsKey(elements.get(currentElement))) {
            String s = null;
            if (length > 256) {
                s = new String(ch, start, 255) + "...";
            } else {
                s = new String(ch, start, length);
            }
            
            //if (StringUtils.isNotBlank(s)) {
                //System.out.println("Adding: " + elements.get(currentElement) + ", ---" + s + "---");
                jsonObject.put(elements.get(currentElement), s);
            //}
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        level--;
        //System.out.println("endElement: " + localName);
        if (root.equals(localName)) {
            if (jsonObject != null && containsAll()) {
                jsonArray.add(jsonObject);
            }
        } else if (inElementArray(localName)) {
            currentElement = "";
        }

    }

    private boolean inElementArray(String name) {
        Enumeration keys = elements.keys();
        while (keys.hasMoreElements()) {
            String l = (String) keys.nextElement();
            if (l.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAll() {
        Enumeration values = elements.elements();
        while (values.hasMoreElements()) {
            String l = (String) values.nextElement();
            if (! (l.equals("desc") || jsonObject.containsKey(l)) ) {
                return false;
            }
        }
        return true;
    }

    public List getJSonArray() {
        return jsonArray;
    }
}
