package ch.epfl.javelo.routing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ch.epfl.javelo.projection.PointCh;

/**
 * Generator for GPX document representing a route.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public class GpxGenerator {

    private static final String DATA_FORMAT = "%.5f";

    private GpxGenerator() {}

    /**
     * Generates the GPX document corresponding to the given route and profile.
     *
     * @param route   route to represent in the GPX format
     * @param profile profile of the route
     * @return the GPX document representing the route and its profile
     */
    public static Document createGpx(Route route, ElevationProfile profile) {
        Document doc = newDocument();

        Element root = doc.createElementNS("http://www.topografix.com/GPX/1/1", "gpx");
        doc.appendChild(root);
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 " + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement("rte");
        double runningLength = 0;
        PointCh previous = route.pointAt(0);
        for (PointCh point : route.points()) {
            // FIXME recalculate distance to -> other way to iterate ? (can't do over the edges:
            // won't treat last point)
            runningLength += previous.distanceTo(point);
            Element rtept = doc.createElement("rtept");
            rtept.setAttribute("lat", String.format(DATA_FORMAT, Math.toDegrees(point.lat())));
            rtept.setAttribute("lon", String.format(DATA_FORMAT, Math.toDegrees(point.lon())));
            Element ele = doc.createElement("ele");
            ele.setTextContent(String.format(DATA_FORMAT, profile.elevationAt(runningLength)));
            rtept.appendChild(ele);
            rte.appendChild(rtept);
            previous = point;
        }
        root.appendChild(rte);

        return doc;
    }

    /**
     * Saves the GPX document, corresponding to a given route and its profile, in a file.
     *
     * @param fileName name of the file
     * @param route    route to represent in the GPX format
     * @param profile  profile of the route
     * @throws IOException if any input/output error is thrown during file related operations
     */
    public static void writeGpx(String fileName, Route route, ElevationProfile profile)
            throws IOException {
        Document doc = createGpx(route, profile);
        Writer w = new FileWriter(fileName);

        try {
            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(w));
        } catch (TransformerException e) {
            throw new Error(e); // should never happen
        }
    }

    /**
     * Generates a new XML document.
     *
     * @return the generated document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // should never happen
        }
    }

}
