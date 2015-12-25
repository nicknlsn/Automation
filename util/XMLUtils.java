package org.lds.cm.content.automation.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtils {
	private static DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	private static XPathFactory factory = XPathFactory.newInstance();
	private static XPath xpath = factory.newXPath();
	
	public static NodeList getNodeListFromXpath (String xml, String xpathExpression) {
		NodeList nodeList = null;

		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			docFactory.setExpandEntityReferences(false);
			Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
			nodeList = getNodeListFromXpath(doc, xpathExpression, null);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return nodeList;
	}
	
	public static NodeList getNodeListFromXpath(File xml, String xpathExpression) {

		NodeList nodeList = null;

		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			docFactory.setExpandEntityReferences(false);
			Document doc = docBuilder.parse(xml.getAbsolutePath());
			nodeList = getNodeListFromXpath(doc, xpathExpression, null);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		return nodeList;

	}
	
	public static NodeList getNodeListFromXpath(Document doc, String xpathExpression, NamespaceContext namespaceContext) {
		NodeList nodeList = null;
		
		try {
			if (null != namespaceContext) {
				xpath.setNamespaceContext(namespaceContext);
			}
			nodeList = (NodeList) xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return nodeList;
	}

	public static String getDocumentAsXmlString (Document doc) {
	    try {
	    	DOMSource domSource = new DOMSource(doc);
	    	StringWriter writer = new StringWriter();
	    	StreamResult result = new StreamResult(writer);
	    	TransformerFactory tf = TransformerFactory.newInstance();
	    	Transformer transformer = tf.newTransformer();
	    	transformer.transform(domSource, result);
   
	    	return writer.toString();
	    } catch(TransformerException ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	} 
	
	public static Document getDocumentFromFile (File input) throws ParserConfigurationException, SAXParseException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setExpandEntityReferences(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(input);
		return doc;
	}


	/**
	 * Get document information from an html5 file.
	 * @param file
	 * @return
	 * @throws SAXParseException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static DocInfo getHtml5DocInfo(File file) throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		return getHtml5DocInfo(getDocumentFromFile(file));
	}
	
	/**
	 * Get document information from an html5 document.
	 * @param document
	 * @return
	 */
	public static DocInfo getHtml5DocInfo(Document document) {
		DocInfo docInfo = new DocInfo();
		
		// get html node attributes
		NodeList htmlNodes = getNodeListFromXpath(document, "//html", null);
		if (htmlNodes.getLength() > 0) {
			Element eHtml = (Element) htmlNodes.item(0);
			docInfo.setDataAid(eHtml.getAttribute("data-aid"));
			docInfo.setDataAidVersion(eHtml.getAttribute("data-aid-version"));
			docInfo.setContentType(eHtml.getAttribute("data-content-type"));
			docInfo.setDataUri(eHtml.getAttribute("data-uri"));
			docInfo.setLang(eHtml.getAttribute("lang"));
		}
		
		// get title
		NodeList titleNodes = getNodeListFromXpath(document, "//title", null);
		if (titleNodes.getLength() > 0) {
			Element eTitle = (Element) titleNodes.item(0);
			docInfo.setTitle(eTitle.getTextContent().replaceAll("[^A-Za-z]", ""));
		}

		// get cover art reference
		NodeList metaNodes = getNodeListFromXpath(document, "//meta[@name=\"thumbnail\"]", null);
		if (metaNodes.getLength() > 0) {
			Element eMeta = (Element) metaNodes.item(0);
			docInfo.setThumbnail(eMeta.getAttribute("content"));
		}
		
		// get file ID
		NodeList ldsNodes = getNodeListFromXpath(document, "//lds:title[@type=\"file\"]", new LDSNamespace());
		if (ldsNodes.getLength() > 0) {
			Element eTitle = (Element) ldsNodes.item(0);
			docInfo.setFile(eTitle.getTextContent());
		}
	
		return docInfo;
	}
	
	/**
	 * get doc info for broadcast ldsxml files
	 * @param file
	 * @return
	 * @throws SAXParseException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static DocInfo getBroadcastXMLDocInfo(File file) throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		return getBroadcastXMLDocInfo(getDocumentFromFile(file));
	}
	
	/**
	 * get doc info for broadcast ldsxml files
	 * @param document
	 * @return
	 */
	public static DocInfo getBroadcastXMLDocInfo(Document document) {
		DocInfo docInfo = new DocInfo();
		
		// get file ID
		NodeList sectionNodes = getNodeListFromXpath(document, "//section", null);
		if (sectionNodes.getLength() > 0) {
			Element sectionElement = (Element) sectionNodes.item(0);
			docInfo.setFile(sectionElement.getAttribute("fileID"));
		}
		
		// get content type
		NodeList publicationIDNodes = getNodeListFromXpath(document, "//publicationID", null);
		if (publicationIDNodes.getLength() > 0) {
			Element publicationIDElement = (Element) publicationIDNodes.item(0);
			docInfo.setContentType(publicationIDElement.getAttribute("type"));
		}
		
		// get title, can be compared with uri
		NodeList titleNodes = getNodeListFromXpath(document, "//title", null);
		if (titleNodes.getLength() > 0) {
			Element titleElement = (Element) titleNodes.item(0);
			docInfo.setTitle(titleElement.getTextContent().replaceAll("[^A-Za-z]", ""));
		}
		
		// get language code, convert to iso3 code?
		NodeList publicationLanguage = getNodeListFromXpath(document, "//publicationID", null);
		if (publicationLanguage.getLength() > 0) {
			Element publicationLanguageElement = (Element) publicationLanguage.item(0);
			docInfo.setContentType(publicationLanguageElement.getAttribute("type"));
			docInfo.setLang(publicationLanguageElement.getTextContent());
		}
		
		return docInfo;
	}
	
	public static DocInfo getMergedConferenceDocInfo(File file) throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		return getMergedConferenceDocInfo(getDocumentFromFile(file));
	}

	private static DocInfo getMergedConferenceDocInfo(Document documentFromFile) {
		DocInfo docInfo = new DocInfo();
		
		// get uri, content type, and language
		NodeList ldswebmlNodes = getNodeListFromXpath(documentFromFile, "//ldswebml", null);
		if (ldswebmlNodes.getLength() > 0) {
			Element ldswebmlElement = (Element) ldswebmlNodes.item(0);
			docInfo.setDataUri(ldswebmlElement.getAttribute("uri"));
			docInfo.setContentType(ldswebmlElement.getAttribute("type"));
			docInfo.setLang(ldswebmlElement.getAttribute("xml:lang"));
		}
		
		NodeList sourceNodes = getNodeListFromXpath(documentFromFile, "//source", null);
		if (sourceNodes.getLength() > 0) {
			Element sourceElement = (Element) sourceNodes.item(0);
			docInfo.setFile(sourceElement.getTextContent());
		}
		
		return docInfo;
	}
	
	public static DocInfo getManualDocInfo(File file) throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		return getManualDocInfo(getDocumentFromFile(file));
	}

	private static DocInfo getManualDocInfo(Document documentFromFile) {
		DocInfo docInfo = new DocInfo();
		
		NodeList bookNodes = getNodeListFromXpath(documentFromFile, "//book", null);
		if (bookNodes.getLength() > 0) {
			Element bookElement = (Element) bookNodes.item(0);
			docInfo.setFile(bookElement.getAttribute("fileID"));
		}
		
		NodeList publicationIDNodes = getNodeListFromXpath(documentFromFile, "//publicationID", null);
		if (publicationIDNodes.getLength() > 0) {
			Element publicationIDElement = (Element) publicationIDNodes.item(0);
			docInfo.setContentType("book");
			docInfo.setTitle(publicationIDElement.getAttribute("title"));
			docInfo.setDataUri("/" + publicationIDElement.getAttribute("type") + "/" + publicationIDElement.getAttribute("title"));
		}
		
		NodeList publicationLanguageNodes = getNodeListFromXpath(documentFromFile, "//publicationLanguage", null);
		if (publicationLanguageNodes.getLength() > 0) {
			Element publicationLanguageElement = (Element) publicationLanguageNodes.item(0);
			docInfo.setLang(publicationLanguageElement.getTextContent());
		}
		
		return docInfo;
	}
	
	public static DocInfo getMagazineDocInfo(File file) throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		return getMagazineDocInfo(getDocumentFromFile(file));
	}

	private static DocInfo getMagazineDocInfo(Document documentFromFile) {
		DocInfo docInfo = new DocInfo();

		String magazine = null;
		String year = null;
		String month = null;

		NodeList magazineNodes = getNodeListFromXpath(documentFromFile, "//magazine", null);
		if (magazineNodes.getLength() > 0) {
			Element magazineElement = (Element) magazineNodes.item(0);
			docInfo.setFile(magazineElement.getAttribute("fileID"));
		}
		
		NodeList publicationIDNodes = getNodeListFromXpath(documentFromFile, "//publicationID", null);
		if (publicationIDNodes.getLength() > 0) {
			Element publicationIDElement = (Element) publicationIDNodes.item(0);
			magazine = publicationIDElement.getAttribute("type");
			docInfo.setContentType("magazine");
			year = publicationIDElement.getAttribute("year");
			month = publicationIDElement.getAttribute("month");
		}
		
		NodeList publicationLanguageNodes = getNodeListFromXpath(documentFromFile, "//publicationLanguage", null);
		if (publicationLanguageNodes.getLength() > 0) {
			Element publicationLanguageElement = (Element) publicationLanguageNodes.item(0);
			docInfo.setLang(publicationLanguageElement.getTextContent());
		}
		
		if (year != null && month != null) {
			docInfo.setDataUri("/" + magazine + "/" + year + "/" + month.substring(0, month.indexOf("-")));
		}
		
		return docInfo;
	}
}
