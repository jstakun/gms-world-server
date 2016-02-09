package com.jstakun.lm.server.utils.xml;

import java.util.Iterator;
import java.util.List;

import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.persistence.Landmark;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.jstakun.lm.server.persistence.Layer;

/**
 *
 * @author jstakun
 */
public class XMLUtils {

    public static String createCustomXmlLandmarkList(List<Landmark> landmarkList, String landingPage) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<landmarks>\r\n");

        Iterator<Landmark> iter = landmarkList.iterator();

        while (iter.hasNext()) {
            Landmark landmark = iter.next();
            xml.append(" <landmark>\r\n");
            xml.append("  <name>" + landmark.getName() + "</name>\r\n");
            xml.append("  <description>" + landmark.getDescription() + "</description>\r\n");
            xml.append("  <latitude>" + landmark.getLatitude() + "</latitude>\r\n");
            xml.append("  <longitude>" + landmark.getLongitude() + "</longitude>\r\n");
            xml.append("  <key>" + landmark.getId() + "</key>\r\n");
            xml.append(" </landmark>\r\n");
        }

        xml.append("</landmarks>\r\n");

        return xml.toString();
    }

    public static String createKmlLandmarkList(List<Landmark> landmarkList, String landingPage) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n");
        xml.append(" <Document>\r\n");
        xml.append("  <name>Landmark Manager Search Results</name>\r\n");
        xml.append("  <Folder>\r\n");
        xml.append("   <name>Landmark Manager Public Landmarks</name>\r\n");

        Iterator<Landmark> iter = landmarkList.iterator();

        while (iter.hasNext()) {
            Landmark landmark = iter.next();

            xml.append("   <Placemark>\r\n");
            xml.append("    <name>" + landmark.getName() + "</name>\r\n");
            //xml.append("   <description>" + landmark.getDescription() + "</description>\r\n");
            xml.append("    <description>" + landingPage + landmark.getId() + "</description>\r\n");
            //<IconStyle>
            //<Icon>
            //<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>
            //</Icon>
            //</IconStyle>
            xml.append("   <Point>\r\n");
            xml.append("    <coordinates>" + landmark.getLongitude() + "," + landmark.getLatitude() + ",0</coordinates>\r\n");
            xml.append("   </Point>\r\n");
            xml.append("  </Placemark>\r\n");

        }
        xml.append("  </Folder>\r\n");
        xml.append("</Document>\r\n");
        xml.append("</kml>");

        return xml.toString();
    }

    public static String createCustomXmlLayersList(List<Layer> layerList) {
        StringBuffer xml = new StringBuffer();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<layers>\r\n");

        Iterator<Layer> iter = layerList.iterator();

        while (iter.hasNext()) {
            Layer layer = iter.next();
            xml.append(" <layer>\r\n");
            xml.append("  <name>" + layer.getName() + "</name>\r\n");
            xml.append("  <iconURI>http://gms-world.appspot.com/images/" + layer.getName() + ".png</iconURI>\r\n");
            xml.append("  <manageable>" + layer.isManageable() + "</manageable>\r\n");
            xml.append("  <enabled>" + layer.isEnabled() + "</enabled>\r\n");
            xml.append("  <checkinable>" + layer.isCheckinable() + "</checkinable>\r\n");
            xml.append(" </layer>\r\n");

        }

        xml.append("</layers>");

        return xml.toString();
    }

    public static String createCustomXmlPhotoList(PhotoList<Photo> photos) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<landmarks>\r\n");

        for (Photo p : photos) {
            xml.append(" <landmark>\r\n");
            xml.append("  <name>" + p.getTitle() + "</name>\r\n");
            xml.append("  <description>" + p.getUrl() + "</description>\r\n");
            xml.append("  <latitude>" + p.getGeoData().getLatitude() + "</latitude>\r\n");
            xml.append("  <longitude>" + p.getGeoData().getLongitude() + "</longitude>\r\n");
            xml.append("  <key>" + p.getId() + "</key>\r\n");
            xml.append(" </landmark>\r\n");
        }

        xml.append("</landmarks>\r\n");

        return xml.toString();
    }

    public static String createKmlPhotoList(PhotoList<Photo> photos) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n");
        xml.append(" <Document>\r\n");
        xml.append("  <name>Landmark Manager Search Results</name>\r\n");
        xml.append("  <Folder>\r\n");
        xml.append("   <name>Landmark Manager Public Landmarks</name>\r\n");


        for (Photo p : photos) {
            xml.append("   <Placemark>\r\n");
            xml.append("    <name>" + p.getTitle() + "</name>\r\n");
            xml.append("    <description>" + p.getUrl() + "</description>\r\n");
            //<IconStyle>
            //<Icon>
            //<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>
            //</Icon>
            //</IconStyle>
            xml.append("   <Point>\r\n");
            xml.append("    <coordinates>" + p.getGeoData().getLongitude() + "," + p.getGeoData().getLatitude() + ",0</coordinates>\r\n");
            xml.append("   </Point>\r\n");
            xml.append("  </Placemark>\r\n");

        }
        xml.append("  </Folder>\r\n");
        xml.append("</Document>\r\n");
        xml.append("</kml>");

        return xml.toString();
    }

    public static String createKmlVideoList(List<VideoEntry> vel) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n");
        xml.append(" <Document>\r\n");
        xml.append("  <name>Landmark Manager Search Results</name>\r\n");
        xml.append("  <Folder>\r\n");
        xml.append("   <name>Landmark Manager Public Landmarks</name>\r\n");

        for (VideoEntry ve : vel) {

            YouTubeMediaGroup mediaGroup = ve.getMediaGroup();
            if (mediaGroup != null) {

                GeoRssWhere geo = ve.getGeoCoordinates();
                MediaPlayer mediaPlayer = mediaGroup.getPlayer();

                xml.append("   <Placemark>\r\n");
                xml.append("    <name>" + (ve.getTitle() != null ? ve.getTitle().getPlainText() : "") + "</name>\r\n");
                xml.append("    <description>" + UrlUtils.forXML(mediaPlayer.getUrl()) + "</description>\r\n");
                //<IconStyle>
                //<Icon>
                //<href>http://maps.google.com/mapfiles/kml/pal3/icon61.png</href>
                //</Icon>
                //</IconStyle>
                xml.append("   <Point>\r\n");
                xml.append("    <coordinates>" + geo.getLongitude() + "," + geo.getLatitude() + ",0</coordinates>\r\n");
                xml.append("   </Point>\r\n");
                xml.append("  </Placemark>\r\n");

            }
        }
        xml.append("  </Folder>\r\n");
        xml.append("</Document>\r\n");
        xml.append("</kml>");

        return xml.toString();
    }

    public static String createCustomXmlVideoList(List<VideoEntry> vel) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<landmarks>\r\n");

        for (VideoEntry ve : vel) {

            YouTubeMediaGroup mediaGroup = ve.getMediaGroup();
            if (mediaGroup != null) {

                GeoRssWhere geo = ve.getGeoCoordinates();
                MediaPlayer mediaPlayer = mediaGroup.getPlayer();

                xml.append(" <landmark>\r\n");
                xml.append("  <name>" + (ve.getTitle() != null ? ve.getTitle().getPlainText() : "") + "</name>\r\n");
                xml.append("  <description>" + UrlUtils.forXML(mediaPlayer.getUrl()) + "</description>\r\n");
                xml.append("  <latitude>" + geo.getLatitude() + "</latitude>\r\n");
                xml.append("  <longitude>" + geo.getLongitude() + "</longitude>\r\n");
                xml.append("  <key></key>\r\n");
                xml.append(" </landmark>\r\n");
            }
        }

        xml.append("</landmarks>\r\n");

        return xml.toString();
    }

    

}
