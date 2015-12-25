package org.lds.cm.content.automation.tests;

import java.util.List;

import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.LDSNamespace;
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


public class mediaXMLhtmlAudioConf 
{

	public static String mp3Link = null;
	public static Node itemId = null;
	public static Node itemType = null;
	public static String audioLink = null;
	public static Node currentNode =null;

	@Test
	public void XMLnodes () throws Exception 
	{
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		//	String mediaXmlPath = "/webml/ldsorg content/english/conference/2015/october/media/13768_000_000-media.xml";

// Supposed to get only get all english MediaXML files			
		//String exampleQuery1 = "<conference-media conferenceId=\"$word _000_\"></conference-media>";
		String exampleQuery1 = "<conference-media conferenceId=\"PD50028768_002_000\"></conference-media>";
		List<Document> mediaXmlDocs = MarkLogicUtils.findMediaXMLByExample(exampleQuery1, MarkLogicDatabase.Preview);
		System.out.println(mediaXmlDocs.size());
		
		for (Document mediaXmlDoc : mediaXmlDocs) 
		{	System.out.println("\nconferenceId = "+mediaXmlDoc.getDocumentElement().getAttribute("conferenceId"));
		
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
					getXMLAudio(mediaXmlDoc);
					getHtmlAudio(mediaXmlDoc);
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
	public static void getXMLAudio(Document mediaXmlDoc) 
	{
//	get the audio link from the XMLMedia file to compare with the link from the http file.	
		String audioLinkXpath = "//conference-item[@"+itemId+"]/media/audio/path"; //[@type=\"mp3\"]
//			System.out.println(audioLinkXpath);
		NodeList audioItemNodes = XMLUtils.getNodeListFromXpath(mediaXmlDoc, audioLinkXpath, null);
		
//			System.out.println("audioItemNodes = "+audioItemNodes.getLength());
		Assert.assertTrue(audioItemNodes != null);
		Assert.assertTrue(audioItemNodes.getLength() == 1);
	
		Node audioItem = audioItemNodes.item(0);
		mp3Link = audioItem.getFirstChild().toString();  //.getAttributes();
		System.out.println("\n"+mp3Link);


	}

@Test
	public static void getHtmlAudio(Document mediaXmlDoc) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
	//  Get audio information from the html file specified in the MediaXML						
		System.out.println("node itemID: "+itemId.getNodeValue());
		String exampleQuery = "<lds:title type=\"file\">" + itemId.getNodeValue() + "</lds:title>";
		List<Document> html5Docs = MarkLogicUtils.findHtml5ByExample(exampleQuery, MarkLogicDatabase.Preview);
		// Should be only 1 document.
		Assert.assertTrue(html5Docs.size() == 1, "Checking " + itemId.getNodeValue());
		
		Document htmldocument = html5Docs.get(0);
		String xpath = "//lds:meta/lds:audio/lds:source";
		NodeList htmlnodes = XMLUtils.getNodeListFromXpath(htmldocument, xpath, new LDSNamespace());
		
		Assert.assertTrue(htmlnodes != null);
		Assert.assertTrue(htmlnodes.getLength() == 1);
	
		currentNode = htmlnodes.item(0);
		NamedNodeMap audioAttributes1 = currentNode.getAttributes();
		audioLink = audioAttributes1.getNamedItem("src").getNodeValue();
		
	}

@Test
	public static void compareXMLhtml() throws Exception 
	{
// Submit the audio REST link and verify that the retrieved audio information is not blank and that the mp3 links match						
		System.out.println(audioLink);
		System.out.println("http repsonse: "+NetUtils.getResponseStatus(audioLink));
		if (NetUtils.getResponseStatus(audioLink) == 200) 
		{ 
			String jUrl = NetUtils.getJson(audioLink).get("url").toString();
			System.out.println("jUrl = "+jUrl);
			
			String jDuration = NetUtils.getJson(audioLink).get("duration").toString();
			System.out.println("duration = "+jDuration);
			
			String jSize = NetUtils.getJson(audioLink).get("size").toString();
			System.out.println("size = "+jSize);
			
			Assert.assertNotEquals(jSize, "", "Size is Empty for: "+currentNode);
			Assert.assertNotEquals(jDuration, "", "Duration is Empty for: "+currentNode);
			Assert.assertNotEquals(jUrl, "", "Url is empty for: "+currentNode);
			Assert.assertEquals(mp3Link, "[#text: "+jUrl+"]", "The retrieved url from the html is not the same as in the MediaXML");
		}else {System.out.println("The web service has some problem.");}
	
			
	}
}


//File mediaXmlFile = MarkLogicUtils.readFileFromML(mediaXmlDoc);
//Document mediaXmlDocument = XMLUtils.getDocumentFromFile(mediaXmlFile);
