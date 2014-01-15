package osm.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import osm.OSMFile;
import osm.primitive.Primitive;
import osm.primitive.Tag;
import osm.primitive.User;
import osm.primitive.node.Node;
import osm.primitive.node.NodeByRef;
import osm.primitive.relation.Member;
import osm.primitive.relation.Relation;
import osm.primitive.relation.RelationByRef;
import osm.primitive.way.Way;
import osm.primitive.way.WayByRef;

public class OSMSaxParser extends DefaultHandler {

	private static final Logger logger = Logger.getLogger(OSMSaxParser.class.getName());
    private OSMFile file = new OSMFile();
    private Primitive currentPrimitive;
    private Node currentNode;
    private Way currentWay;
    private Relation currentRelation;

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("tag".equals(qName)) {
            String key = attributes.getValue("k");
            String value = attributes.getValue("v");
            Tag t = new Tag(key, value);
            currentPrimitive.addTag(t);
        } else if ("node".equals(qName)) {
            double lat = Double.parseDouble(attributes.getValue("lat"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            currentNode = new Node(lat, lon);
            currentPrimitive = currentNode;
            handlePrimitiveAttributes(attributes);
        } else if ("nd".equals(qName)) {
            int id = Integer.parseInt(attributes.getValue("ref"));
            Node ref = file.findNodeById(id);
            if (ref == null) {
                ref = new NodeByRef(id);
            }
            currentWay.addNode(ref);
        } else if ("way".equals(qName)) {
            currentWay = new Way();
            currentPrimitive = currentWay;
            handlePrimitiveAttributes(attributes);
        } else if ("relation".equals(qName)) {
            currentRelation = new Relation();
            currentPrimitive = currentRelation;
            handlePrimitiveAttributes(attributes);
        } else if ("member".equals(qName)) {
            String type = attributes.getValue("type");
            int refId = Integer.parseInt(attributes.getValue("ref"));
            Primitive ref = null;
            if("way".equals(type)) {
                ref = file.findWayById(refId);
                if(ref == null) {
                    ref = new WayByRef(refId);
                }
            } else if("node".equals(type)) {
                ref = file.findNodeById(refId);
                if(ref == null) {
                    ref = new NodeByRef(refId);
                }
            } else if("relation".equals(type)) {
                ref = file.findRelationById(refId);
                if(ref == null) {
                    ref = new RelationByRef(refId);
                }
            }
            String role = attributes.getValue("role");
            Member member = new Member(ref, role);
            currentRelation.addMember(member);
        } else {

        }
    }

    /**
     * @param attributes
     */
    private void handlePrimitiveAttributes(Attributes attributes) {
        int id = Integer.parseInt(attributes.getValue("id"));
//        String date = attributes.getValue("timestamp");
        int ver = Integer.parseInt(attributes.getValue("version"));
        int uid = Integer.parseInt(attributes.getValue("uid"));
        String username = attributes.getValue("user");
        User user = new User(uid, username);
        currentPrimitive.setUser(user);
        currentPrimitive.setID(id);
        currentPrimitive.setVersion(ver);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("node".equals(qName)) {
            file.addNode(currentNode);
        } else if ("way".equals(qName)) {
            file.addWay(currentWay);
        } else if ("relation".equals(qName)) {
            file.addRelation(currentRelation);
        } else {

        }
    }

    /**
     * @return The {@link OSMFile} that was built up when reading using this
     *         parser. Guaranteed to not be null.
     */
    public OSMFile getOSMFile() {
        return file;
    }
    
	public void error(SAXParseException exception) {
		logger.log(Level.SEVERE, "OsmHandler.error():", exception);
	}
	
	public void fatalError(SAXParseException exception) {
		logger.log(Level.SEVERE, "OsmHandler.fatalError():", exception);
	}
	
	public void warning(SAXParseException exception)  {
		logger.log(Level.WARNING, "OsmHandler.warning():", exception);
	}

}
