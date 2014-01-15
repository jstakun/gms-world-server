/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.xml;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jstakun
 */
public class ParserManager {

    private DefaultHandler handler;
    private SAXParser saxParser;
    private static final Logger logger = Logger.getLogger(ParserManager.class.getName());

    /**
     * Constructor
     * @param handler - DefaultHandler for the SAX parser
     */
    public ParserManager(DefaultHandler handler) {
        this.handler = handler;
        create();
    }

    /**
     * Create the SAX parser
     */
    private void create() {
        try {
            // Obtain a new instance of a SAXParserFactory.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // Specifies that the parser produced by this code will provide support for XML namespaces.
            factory.setNamespaceAware(true);
            // Specifies that the parser produced by this code will validate documents as they are parsed.
            factory.setValidating(true);
            //
            factory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            // Creates a new instance of a SAXParser using the currently configured factory parameters.
            saxParser = factory.newSAXParser();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
            //t.printStackTrace();
        }
    }

    /**
     * Parse a File
     * @param file - File
     */
    public void parseFile(File file) {
        try {
            saxParser.parse(file, handler);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
            //t.printStackTrace();
        }
    }

    /**
     * Parse a URI
     * @param uri - String
     */
    public void parseUri(String uri) {
        try {
            saxParser.parse(uri, handler);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
            //t.printStackTrace();
        }
    }

    /**
     * Parse a Stream
     * @param stream - InputStream
     */
    public void parseInputStream(InputStream stream) {
        try {
            saxParser.parse(stream, handler);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
            //t.printStackTrace();
        }
    }

    /**
     * Parse a Stream
     * @param stream - InputStream
     */
    public void parseInputSource(InputSource stream) {
        try {
            saxParser.parse(stream, handler); 
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
    }
}
