package org.lds.cm.content.automation.tests;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.json.simple.JSONObject;
import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.DocInfo;
import org.lds.cm.content.automation.util.FileUtil;
import org.lds.cm.content.automation.util.JDBCUtils;
import org.lds.cm.content.automation.util.LDSNamespace;
import org.lds.cm.content.automation.util.MarkLogicUtils;
import org.lds.cm.content.automation.util.NetUtils;
import org.lds.cm.content.automation.util.SourceXMLUtil;
import org.lds.cm.content.automation.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ExampleFT {
	@Test
	public void findMediaXml () {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		try {
			String exampleQuery1 = "<conference-media conferenceId=\"PD50028768_002_000\"></conference-media>";
			List<Document> mediaXmlDocs = MarkLogicUtils.findMediaXMLByExample(exampleQuery1, MarkLogicDatabase.Preview);
			
			System.out.println("SIZE: " + mediaXmlDocs);
			
//			File xml = MarkLogicUtils.readFileFromML("/webml/ldsorg content/english/conference/2011/april/media/PD50028768_002_000-media.xml", false);
//			System.out.println(xml.getName());
//			File xml = MarkLogicUtils.readFileFromML("/webml/mobile/apw/general-conference/2013/04/beautiful-mornings/10785_012_56porter.xml", false);
//			System.out.println(xml.getName());
			File xml = MarkLogicUtils.readFileFromML("/preview/content-automation/content/eng/friend/2014/05/_manifest.html", false);
			System.out.println(xml.getName());
		} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Test
	public void findHtml5ByFileId () {
		// so if I transform a file, I can grab the file ID, then bring back all html5 content with that file ID.
		// Then I can compare what I would expect to see in the html5 content, with what I actually see.
		
		String [] fileIdArray = {"10991_000"};
		
		for (String fileId : fileIdArray) {
			List<Document> docs = MarkLogicUtils.findHtml5ByFileId(fileId, MarkLogicDatabase.Preview, MarkLogicDatabase.Preview.getContentRoot(), 200);
			
			String videoXpath = "//html/body/div[@class=\"body-block\"]/video";
			
			int index = 1;
			for (Document doc : docs) {
				NodeList videoNodes = XMLUtils.getNodeListFromXpath(doc, videoXpath, null);
				
				if (videoNodes != null && videoNodes.getLength() > 0) {
					for (int nodeIndex = 0; nodeIndex < videoNodes.getLength(); nodeIndex++) {
						Element videoNode = (Element) videoNodes.item(nodeIndex);
						
						String videoUrl = videoNode.getAttribute("src");
						
						if (StringUtils.isNotEmpty(videoUrl)) {
							JSONObject results = null;
							try {
								results = NetUtils.getJson(videoUrl);
							} catch (ParseException | org.json.simple.parser.ParseException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if (results != null) {
								String hlsurl = (String) results.get("HLSURL");
								Assert.assertTrue(StringUtils.isNotEmpty(hlsurl), "HLSURL is empty: " + videoUrl);
								System.out.println(hlsurl);
							}
						}
					}
				}
				
			}
		}
		
	}
	/**
	 * Simple test to see if a file can be queried from MarkLogic
	 */
	@Test
	public void readFileFromMarkLogic () {
		String pathToCheck = MarkLogicDatabase.Preview.getContentRoot() + "/eng/friend/2010/03/front-cover.html";
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		// first try, read the file and check for empty contents
		File tempFile = MarkLogicUtils.readFileFromML(pathToCheck);
		String fileContents = FileUtil.readFileToString(tempFile);
		System.out.println(fileContents);
		Assert.assertTrue(StringUtils.isNotEmpty(fileContents));
		
		// second try, call utility method to check for existence
		Assert.assertTrue(MarkLogicUtils.docExists(pathToCheck), "Call to ML.docExists()");
	}
	
	/**
	 * retrieve an HTML5 doc and look for an lds:meta element with an lds:title child, then print out some data
	 */
	@Test
	public void checkExistenceLdsMeta_ldsTitle () {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		String pathToCheck = MarkLogicDatabase.Preview.getContentRoot() + "/eng/friend/2010/03/bulletin-board.html";
		File tempFile = MarkLogicUtils.readFileFromML(pathToCheck);
		
		try {
			Document document = XMLUtils.getDocumentFromFile(tempFile);
			
			String xpath = "//lds:meta/lds:title";
			NodeList nodes = XMLUtils.getNodeListFromXpath(document, xpath, new LDSNamespace());
//			String xpathNoNamespace = "//a[@class=\"legacy-cross-ref\"]";
//			NodeList nodes = XMLUtils.getNodeListFromXpath(document, xpath, null);
			
			Assert.assertTrue(nodes != null);
			Assert.assertTrue(nodes.getLength() > 0);
			
			for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
				Node currentNode = nodes.item(nodeIndex);
				NamedNodeMap attributes = currentNode.getAttributes();
				
				for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++) {
					Node currentAttribute = attributes.item(attributeIndex);
					System.out.println(currentNode.getNodeName() + " " + currentAttribute.getNodeName() + "=" + currentAttribute.getNodeValue() + " >> " + currentNode.getTextContent());
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			Assert.fail("Test failed: " + e.getMessage());
		}
	}
	
	/**
	 * open a media xml file, and for each talk retrieve the corresponding HTML5 document
	 */
	@Test
	public void checkMediaNodes () {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		String mediaXmlPath = "/webml/ldsorg content/english/conference/2014/october/media/10991_000_000-media.xml";
		File mediaXmlFile = MarkLogicUtils.readFileFromML(mediaXmlPath);
		
		try {
			Document mediaXmlDocument = XMLUtils.getDocumentFromFile(mediaXmlFile);
			String conferenceItemXpath = "//conference-item";
			NodeList conferenceItemNodes = XMLUtils.getNodeListFromXpath(mediaXmlDocument, conferenceItemXpath, null);
			
			Assert.assertTrue(conferenceItemNodes != null);
			Assert.assertTrue(conferenceItemNodes.getLength() > 0);
			
			for (int nodeIndex = 0; nodeIndex < conferenceItemNodes.getLength(); nodeIndex++) {
				Node conferenceItem = conferenceItemNodes.item(nodeIndex);
				NamedNodeMap attributes = conferenceItem.getAttributes();
				Node itemType = attributes.getNamedItem("type");
				Node itemId = attributes.getNamedItem("articleId");
				
				if (itemType != null && itemType.getNodeValue().equals("talk")) {
					System.out.println(itemId.getNodeValue());
					String exampleQuery = "<lds:title type=\"file\">" + itemId.getNodeValue() + "</lds:title>";
					List<Document> html5Docs = MarkLogicUtils.findHtml5ByExample(exampleQuery, MarkLogicDatabase.Preview);
					
					// assert that we have at least one document
					Assert.assertTrue(html5Docs.size() > 0, "Checking " + itemId.getNodeValue());
				}
				
			}
		} catch (ParserConfigurationException | SAXException | IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | DOMException e) {
			e.printStackTrace();
			Assert.fail("Test failed: " + e.getMessage());
		}
	}
	
	@Test
	public void processConferenceMediaByExample () {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		String exampleQuery = "<conference-media conferenceId=\"$word 12565_\"></conference-media>";
		
		try {
			List<Document> mediaXmlDocs = MarkLogicUtils.findMediaXMLByExample(exampleQuery, MarkLogicDatabase.Preview);
			
			for (Document mediaXmlDoc : mediaXmlDocs) {
				System.out.println(mediaXmlDoc.getDocumentElement().getAttribute("conferenceId"));
			}
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Get the cover art map file and read it as a document object
	 */
	@Test
	public void getCoverArtMap () {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		String coverArtMapLocation = "xslt/cover-art-map.xsl";
		File coverArtMap = FileUtil.getResourceFile(coverArtMapLocation);
		
		try {
			Document coverArtDocument = XMLUtils.getDocumentFromFile(coverArtMap);
			String xpath = "//image[@mediaID=\"1482478\"]";
			NodeList coverArtNodes = XMLUtils.getNodeListFromXpath(coverArtDocument, xpath, null);
			
			Assert.assertTrue(coverArtNodes != null, "No cover art entries matching xpath " + xpath);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * Demonstrate the getResultSet method in JDBCUtils
	 */
	@Test
	public void performSqlQuery() {
		String query = "select * from transform_xslt order by modified_date desc";
		
		// run query and get result set
		ResultSet rs = JDBCUtils.getResultSet(query);
		
		try {
			while (rs.next()) {
				System.out.println(rs.getString("MODIFIED_DATE") + " - " + rs.getString("XSLT_NAME"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Demo DocInfo object use.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws SAXParseException 
	 */
	@Test
	public void testDocInfo() throws SAXParseException, ParserConfigurationException, SAXException, IOException {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		String filePath = MarkLogicDatabase.Preview.getContentRoot() + "/eng/general-conference/2015/04/_manifest.html";
		File file = MarkLogicUtils.readFileFromML(filePath);
		DocInfo docInfo = XMLUtils.getHtml5DocInfo(file);
		
		System.out.println("Document information for: " + filePath);
		System.out.println(
				"data-aid=\"" + docInfo.getDataAid() + "\"" +
				"data-aid-version=\"" + docInfo.getDataAidVersion() + "\" " + 
				"data-content-type=\"" + docInfo.getContentType() + "\" " +
				"data-uri=\"" + docInfo.getDataUri() + "\" " +
				"lang=\"" + docInfo.getLang() + "\" "
				);
		System.out.println("Thumbnail reference: " + docInfo.getThumbnail());
		System.out.println("File ID: " + docInfo.getFile());
	}
	
	/**
	 * Demo getting source files to use in tests.
	 */
	@Test
	public void testGetSourceFiles() {
		List<File> allRussianMagazines = SourceXMLUtil.getSourceFiles("magazines", "rus");
		for (File path: allRussianMagazines) System.out.println(path);
		
		List<File> randomPaths = SourceXMLUtil.getRandomSourceFiles("general-conference", "eng", 10);
		for (File path: randomPaths) System.out.println(path);
		
		List<File> randomPathsAll = SourceXMLUtil.getRandomSourceFiles(10);
		for (File file: randomPathsAll) System.out.println(file);
		
		List<File> spanishScriptures = SourceXMLUtil.getAllSourceFiles(Constants.xmlRoot + "scriptures/06897_002_Spanish-2011-Triple_2015-10-06");
		for (File file: spanishScriptures) System.out.println(file);
	}
	
	/**
	 * Demo DocInfo object use.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@Test
	public void testDocInfoDoc() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		String exampleQuery1 = "<conference-media conferenceId=\"PD50028768_002_000\"></conference-media>";
		List<Document> files = MarkLogicUtils.findMediaXMLByExample(exampleQuery1, MarkLogicDatabase.Preview);
		DocInfo docInfo = XMLUtils.getHtml5DocInfo(files.get(0));
		
		System.out.println("Document information for: " + files.get(0));
		System.out.println(
				"data-aid=\"" + docInfo.getDataAid() + "\"" +
				"data-aid-version=\"" + docInfo.getDataAidVersion() + "\" " + 
				"data-content-type=\"" + docInfo.getContentType() + "\" " +
				"data-uri=\"" + docInfo.getDataUri() + "\" " +
				"lang=\"" + docInfo.getLang() + "\" "
				);
		System.out.println("Thumbnail reference: " + docInfo.getThumbnail());
		System.out.println("File ID: " + docInfo.getFile());
		
	
	}
	
	
	/**
	 * Z:/ is where test MarkLogic is mapped on my local machine. This can be stored in the properties file if it is needed. 
	 * 
	 * This just makes it easier to delete entire directories on MarkLogic because MarkLogic's 
	 * API requires that you specify the URI of every file you want to delete.
	 * 
	 * This can be used to delete things for testing xrefs.
	 */
	@Test
	public void deleteMarkLogicDirectory() {
		String testMLPath = "Z:/published/content-automation/content/eng";
		File root = new File(testMLPath);
		List<File> files = (List<File>) FileUtils.listFiles(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (File file: files) {
			System.out.println(file.getAbsolutePath());
		}
		
		File directoryToDelete = new File("Z:/preview/content-automation/content/eng/manual/jesus-christ-and-the-everlasting-gospel-teacher-manual");
		try {
			FileUtils.deleteDirectory(directoryToDelete);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
