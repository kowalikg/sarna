package pl.edu.agh.sarna.utils.java;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import pl.edu.agh.sarna.model.NetworkEntry;

public class XMLParser {

    private static final String TAG_NAME = "WifiConfiguration";

    private static final String ATTRIB = "name";

    private static final String ATTRIB_SSID = "SSID";

    private static final String ATTRIB_PASSWORD = "PreSharedKey";

    private static final String QUOTE = "\"";

    private DocumentBuilder builder;

    private Document document;

    private Collection<NetworkEntry> parsedEntries = new ArrayList<>();

    public XMLParser() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void parse(InputStream in) throws IOException, SAXException {
        document = builder.parse(in);
        fillNetworkEntriesList();
    }

    private void fillNetworkEntriesList() {
        NodeList list = document.getElementsByTagName(TAG_NAME);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            addEntryToList(node);
        }
    }

    private void addEntryToList(Node node) {
        String ssid = "";
        String password = "";
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.hasAttributes() && child.getAttributes().getNamedItem(ATTRIB) != null) {
                String attrib = child.getAttributes().getNamedItem(ATTRIB).getTextContent();
                if (attrib.equals(ATTRIB_SSID)) {
                    ssid = child.getTextContent();
                } else if (attrib.equals(ATTRIB_PASSWORD)) {
                    password = child.getTextContent();
                }
            }
        }
        parsedEntries.add(new NetworkEntry(trimQuotes(ssid), trimQuotes(password)));
    }

    private static String trimQuotes(String str) {
        if (str.length() > 1 && str.startsWith(QUOTE) && str.endsWith(QUOTE)) {
            return str.substring(1, str.length()-1);
        } else {
            return str;
        }
    }

    public Collection<NetworkEntry> getParsedEntries() {
        return parsedEntries;
    }
}
