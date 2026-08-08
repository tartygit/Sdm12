package com.cth.sdm.handler;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class XmlDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "xml".equalsIgnoreCase(fileExtension);
    }

    @Override
    public Map<String, String> handle(File file) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // Secure XML Parsing against XXE
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        String rootName = doc.getDocumentElement().getNodeName();
        StringBuilder childNodesBuilder = new StringBuilder();

        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        int itemNum = 1;
        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                if (itemNum++ > 50) {
                    childNodesBuilder.append("[Truncated due to child node limit]\n");
                    break;
                }
                String childName = nNode.getNodeName();
                String childValue = nNode.getTextContent().trim();
                if (!childValue.isEmpty()) {
                    childNodesBuilder.append(childName).append(": ").append(childValue).append("\n");
                }
            }
        }

        String listResult = childNodesBuilder.toString().trim();
        result.put("XML Node - " + rootName, listResult.isEmpty() ? "Empty XML Schema Content" : listResult);
        return result;
    }
}
