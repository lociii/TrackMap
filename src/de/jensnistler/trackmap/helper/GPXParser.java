package de.jensnistler.trackmap.helper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.location.Location;

public class GPXParser {
    private File mFile;
    
    public GPXParser(File file) {
        mFile = file;
    }

    public List<List<Location>> getTracks() {
        List<List<Location>> trackList = new ArrayList<List<Location>>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            FileInputStream fis = new FileInputStream(mFile);
            Document dom = builder.parse(fis);
            Element root = dom.getDocumentElement();

            // get all tracks
            NodeList tracks = root.getElementsByTagName("trk");
            for (int i = 0; i < tracks.getLength(); i++) {
                List<Location> pointList = new ArrayList<Location>();
                Element track = (Element) tracks.item(i);

                // get all waypoints for track
                NodeList waypoints = track.getElementsByTagName("trkpt");
                for (int j = 0; j < waypoints.getLength(); j++) {
                    Node waypoint = waypoints.item(j);

                    NamedNodeMap attrs = waypoint.getAttributes();

                    Location pt = new Location("test");
                    pt.setLatitude(Double.parseDouble(attrs.getNamedItem("lat").getTextContent()));
                    pt.setLongitude(Double.parseDouble(attrs.getNamedItem("lon").getTextContent()));

                    pointList.add(pt);
                }
                trackList.add(pointList);
            }
        }
        catch (Exception e) {
            
        }

        return trackList;
    }
}
