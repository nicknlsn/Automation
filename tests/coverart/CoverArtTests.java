package org.lds.cm.content.automation.tests.coverart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONObject;
import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.DocInfo;
import org.lds.cm.content.automation.util.EndpointUtil;
import org.lds.cm.content.automation.util.MarkLogicUtils;
import org.lds.cm.content.automation.util.NetUtils;
import org.lds.cm.content.automation.util.SourceXMLUtil;
import org.lds.cm.content.automation.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CoverArtTests {
	
	/**
	 * Check for correct output HTML5 and check for correct cover art reference in the manifest file for general conference
	 * 
	 * Need to write a test similar to this that goes out to MarkLogic to get all already existing manifest files and test those,
	 * instead of transforming each file first.
	 * @throws SAXParseException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public static void checkCoverArt() throws SAXParseException, ParserConfigurationException, SAXException, IOException, InterruptedException {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		System.out.println("Test: Check manifest for cover art reference.");
		
		List<String> failedToTransform = new ArrayList<>();
		List<String> incorrectDocInfoList = new ArrayList<>();
		List<String> missingCoverArReftList = new ArrayList<>();
		List<String> badCoverArtReferenceList = new ArrayList<>();
		List<String> wrongEntryList = new ArrayList<>();
		List<String> notFoundInMap = new ArrayList<>();
		
		// change numFiles to control how many of each content type to test with
		int numFiles = 5;
		List<File> files = new ArrayList<>();
		files.addAll(SourceXMLUtil.getRandomSourceFiles("magazines", "ALL", numFiles));
		files.addAll(SourceXMLUtil.getRandomSourceFiles("manual", "ALL", numFiles));
		files.addAll(SourceXMLUtil.getRandomSourceFiles("general-conference", "ALL", numFiles));
		System.out.println("Number of files to test: " + files.size());
		
		int i = 0;
		for (File file: files) {
			
			System.out.println();
			System.out.println("File " + ++i + " of " + files.size());
			JSONObject transformResonse = EndpointUtil.transformFile(file);
			
			if ((boolean) transformResonse.get("transformSuccess")) {
				
				// first make sure the expected file is there
				System.out.println("Checking output HTML5...");
				
				DocInfo sourceDocInfo = new DocInfo();
				if (i - 1 < numFiles) sourceDocInfo = XMLUtils.getMagazineDocInfo(file);
				else if (i - 1 < numFiles * 2) sourceDocInfo = XMLUtils.getManualDocInfo(file);
				else if (i - 1 < numFiles * 3) sourceDocInfo = XMLUtils.getMergedConferenceDocInfo(file);
				
				System.out.println("	Source doc info:" + 
								" language=" + sourceDocInfo.getLang() + 
								" fileID=" + sourceDocInfo.getFile() +
								" type=" + sourceDocInfo.getContentType() +
								" uri=" + sourceDocInfo.getDataUri());
				File outputHTML5 = MarkLogicUtils.readFileFromML(
						MarkLogicDatabase.Preview.getContentRoot() + "/" + sourceDocInfo.getLang() + sourceDocInfo.getDataUri() + "/_manifest.html",
						false);
				DocInfo html5DocInfo = XMLUtils.getHtml5DocInfo(outputHTML5);
				System.out.println("	HTML5 doc info: " + 
						" language=" + html5DocInfo.getLang() + 
						" fileID=" + html5DocInfo.getFile() +
						" type=" + html5DocInfo.getContentType() +
						" uri=" + html5DocInfo.getDataUri());
				
				if (!html5DocInfo.getDataUri().contains(sourceDocInfo.getDataUri()) || 
					!html5DocInfo.getContentType().equals(sourceDocInfo.getContentType()) ||
					!html5DocInfo.getLang().equals(sourceDocInfo.getLang()) ||
					!html5DocInfo.getFile().equals(sourceDocInfo.getFile())) {
					incorrectDocInfoList.add(file.getName());
				}
				
				// check cover art
				System.out.println("Checking cover art...");
				if (html5DocInfo.getThumbnail() != null) {
					
					int refResponse = NetUtils.getResponseStatus(html5DocInfo.getThumbnail());
					System.out.println("	" + html5DocInfo.getThumbnail() + " " + Integer.toString(refResponse));
					if (refResponse != 200) {
						badCoverArtReferenceList.add(file.getName());
					}
					
					// check cover art map for entry and compare file ID
					File coverArtMap = new File("C:/Users/nicknlsn/Documents/content-automation.git/web/src/main/resources/xslt/ldsXML_cover-art/cover-art-map.xsl");
					String mediaID = html5DocInfo.getThumbnail().substring(html5DocInfo.getThumbnail().lastIndexOf("/") + 1);
					NodeList imageNodes = XMLUtils.getNodeListFromXpath(coverArtMap, "//image[@mediaID=\"" + mediaID + "\"]");
					if (imageNodes.getLength() > 0) {
						Element imageElement = (Element) imageNodes.item(0);
						String expectedFileID = sourceDocInfo.getFile().substring(0, sourceDocInfo.getFile().indexOf("_"));
						if (!imageElement.getAttribute("fileID").contains(expectedFileID)) {
							wrongEntryList.add("\n" + file.getName() + " - " + mediaID);
						}
					} else {
						notFoundInMap.add("\n" + file.getAbsolutePath());
					}
				} else {
					missingCoverArReftList.add("\n" + file.getAbsolutePath());
				}
			} else {
				System.out.println("File failed to transform: " + file.getAbsolutePath());
				failedToTransform.add("\n" + file.getAbsolutePath());
			}
		} // end for loop
		
		System.out.println();
		if (!failedToTransform.isEmpty()) System.out.println("These files failed to transform: " + failedToTransform);
		if (!incorrectDocInfoList.isEmpty())  System.out.println("Source doc info did not match output doc info for files: " + incorrectDocInfoList);
		if (!missingCoverArReftList.isEmpty())  System.out.println("Missing cover art reference for files: " + missingCoverArReftList);
		if (!badCoverArtReferenceList.isEmpty())  System.out.println("Bad cover art reference for files: " + badCoverArtReferenceList);
		if (!wrongEntryList.isEmpty())  System.out.println("Wrong entry in cover art map for these files and mediaID pairs: " + wrongEntryList);
		if (!notFoundInMap.isEmpty())  System.out.println("No entry found in cover art map for these files: " + notFoundInMap);
		
		Assert.assertTrue(
				incorrectDocInfoList.isEmpty() && 
				missingCoverArReftList.isEmpty() && 
				badCoverArtReferenceList.isEmpty() && 
				wrongEntryList.isEmpty() && 
				notFoundInMap.isEmpty());
	}
}
