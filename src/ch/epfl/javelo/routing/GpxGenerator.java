package ch.epfl.javelo.routing;

import java.io.IOException;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generator for GPX document representing a route.
 * <p>
 * Non-instantiable.
 */
public class GpxGenerator {

    private GpxGenerator() {}

    /**
     * Generates the GPX document corresponding to the given route and profile.
     * 
     * @param route   route to represents in the GPX format
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


        // TODO: complete with rte and rtept elements
    }

    /**
     * Writes the GPX document corresponding to the given route and profile in a file.
     * 
     * @param filename name of the file
     * @param route    route to represent in the GPX format
     * @param profile  profile of the route
     * @throws IOException if any input/output error is thrown during file related operations
     */
    public static void writeGpx(String filename, Route route, ElevationProfile profile)
            throws IOException {
        Document doc = createGpx(route, profile);
        Writer w = null; // TODO: correct writer

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
            throw new Error(e); // Should never happen
        }
    }

}
