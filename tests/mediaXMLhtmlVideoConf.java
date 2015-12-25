package org.lds.cm.content.automation.tests;

import java.util.List;

import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.MarkLogicUtils;
import org.lds.cm.content.automation.util.NetUtils;
import org.lds.cm.content.automation.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class mediaXMLhtmlVideoConf 
{

	public static String mp4Link = null;
	public static Node itemId = null;
	public static Node itemType = null;
	public static Node streamingID = null;
	
	public static String videoLink = null;
	public static Node currentNode =null;

	@Test
	public void XMLnodes () throws Exception 
	{
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
				//			String mediaXmlPath = "/webml/ldsorg content/english/conference/2015/october/media/13768_000_000-media.xml";

// Supposed to get only get all english MediaXML files			
		String exampleQuery1 = "<conference-media conferenceId=\"$word _000_\"></conference-media>";
		List<Document> mediaXmlDocs = MarkLogicUtils.findMediaXMLByExample(exampleQuery1, MarkLogicDatabase.Preview);
		
		for (Document mediaXmlDoc : mediaXmlDocs) 
		{	System.out.println("\nconferenceId = "+mediaXmlDoc.getDocumentElement().getAttribute("conferenceId"));
			if(!mediaXmlDoc.getDocumentElement().getAttribute("conferenceId").contains("_000_") || mediaXmlDoc.getDocumentElement().getAttribute("conferenceId").contains("13768"))
			{continue;}
		try {
			String conferenceItemXpath = "//conference-item";
			NodeList conferenceItemNodes = XMLUtils.getNodeListFromXpath(mediaXmlDoc, conferenceItemXpath, null);
			Assert.assertTrue(conferenceItemNodes != null);
			Assert.assertTrue(conferenceItemNodes.getLength() > 0);
				
			for (int nodeIndex = 0; nodeIndex < conferenceItemNodes.getLength(); nodeIndex++) 
			{
				Node conferenceItem = conferenceItemNodes.item(nodeIndex);
				NamedNodeMap attributes = conferenceItem.getAttributes();
				itemType = attributes.getNamedItem("type");
				itemId = attributes.getNamedItem("articleId");
					
				if (itemType != null && itemType.getNodeValue().equals("talk") && itemId.getNodeValue().length() > 10) 
				{
					getXMLVideo(mediaXmlDoc);
					if (streamingID == null) {continue;}; 
					getHtmlVideo(mediaXmlDoc);
					compareXMLhtml();
					
//  Video verification 
						
						
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | DOMException e) {
			e.printStackTrace();
			Assert.fail("Test failed: " + e.getMessage());
		}
		}
	}
@Test	
	public static void getXMLVideo(Document mediaXmlDoc) 
	{
//	get the video link from the XMLMedia file to compare with the link from the http file.	
	System.out.println("itemId = "+itemId);
		String videoLinkXpath = "//conference-item[@"+itemId+"]/media/video/path"; //[@type=\"ovp\"]
		NodeList videoItemNodes = XMLUtils.getNodeListFromXpath(mediaXmlDoc, videoLinkXpath, null);
			
		Assert.assertTrue(videoItemNodes != null);
		Assert.assertTrue(videoItemNodes.getLength() > 0);
	
		Node VideoItem = videoItemNodes.item(0);
		NamedNodeMap attributes = VideoItem.getAttributes();
		streamingID = attributes.getNamedItem("id");
		
		if (streamingID == null) 
		{
			String videoLinkXpathN = "//conference-item[@"+itemId+"]/media/video/path[@type=\"mp4\"][@size=\"360p\"]";
			//System.out.println("videoLinkXpathN = "+videoLinkXpathN);
			NodeList videoItemNodesN = XMLUtils.getNodeListFromXpath(mediaXmlDoc, videoLinkXpathN, null);
			Node videoItem = videoItemNodesN.item(0);
			mp4Link = videoItem.getFirstChild().toString();  //.getAttributes();
			System.out.println("\n No Streaming ID found for: "+mp4Link+"  Search by link under development");
			return;
			
			// Need the link from Richard to query for the mp4 info.
		}

	}

@Test
	public static void getHtmlVideo(Document mediaXmlDoc) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
	//  Get video information from the html file specified in the MediaXML						
		String exampleQuery = "<lds:title type=\"file\">" + itemId.getNodeValue() + "</lds:title>";
		List<Document> html5Docs = MarkLogicUtils.findHtml5ByExample(exampleQuery, MarkLogicDatabase.Preview);
		// Should be only 1 document.
		System.out.println("# of html docs: "+html5Docs.size());
		Assert.assertTrue(html5Docs.size() > 0, "html document not found: " + itemId.getNodeValue());
		
		Document htmldocument = null;
		for (Document html5Doc : html5Docs) 
		{
			
			String xPathContentType = "/html[@data-content-type]"; //\"general-conference-talk\"]";
			
			NodeList htmlNodes = XMLUtils.getNodeListFromXpath(html5Doc, xPathContentType, null);
			NamedNodeMap attributes = htmlNodes.item(0).getAttributes();  // should only be 1 node.
			String contentType =  attributes.getNamedItem("data-content-type").getNodeValue();
			System.out.println("contentType: "+contentType);
			if (contentType.contains("general-conference-talk")) 
			{	htmldocument = html5Doc;}
		}
		
		
//		Document htmldocument = html5Docs.get(0);
		String xpath = "//body/header/video";
		NodeList htmlnodes = XMLUtils.getNodeListFromXpath(htmldocument, xpath, null);
		
		Assert.assertTrue(htmlnodes != null);
		System.out.println("htmlnodes = "+htmlnodes.getLength());
		Assert.assertTrue(htmlnodes.getLength() == 1);
	
		currentNode = htmlnodes.item(0);
		NamedNodeMap videoAttributes1 = currentNode.getAttributes();
		videoLink = videoAttributes1.getNamedItem("src").getNodeValue();
		
	}

@Test
	public static void compareXMLhtml() throws Exception 
	{
// Submit the video REST link and verify that the retrieved video information is not blank and that the mp3 links match						
		System.out.println("http repsonse: "+NetUtils.getResponseStatus(videoLink));
		Assert.assertTrue(NetUtils.getResponseStatus(videoLink) == 200, "The web service has some problem.");
		if (NetUtils.getResponseStatus(videoLink) == 200) 
		{ 
			Assert.assertTrue(NetUtils.getJson(videoLink) != null, "No Json object found for: "+videoLink);
			System.out.println("Streaming ID = "+streamingID.getNodeValue()+"     videoLink = "+videoLink+"\n");
			Assert.assertTrue(videoLink.contains(streamingID.getNodeValue()), "The retrieved url from the html is not the same as in the MediaXML");
		}
	
			
	}
}
