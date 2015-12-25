package org.lds.cm.content.automation.tests.testcix;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.ParseException;
import org.json.simple.JSONObject;
import org.lds.cm.content.automation.util.DocInfo;
import org.lds.cm.content.automation.util.EndpointUtil;
import org.lds.cm.content.automation.util.FileUtil;
import org.lds.cm.content.automation.util.JDBCUtils;
import org.lds.cm.content.automation.util.MarkLogicUtils;
import org.lds.cm.content.automation.util.NetUtils;
import org.lds.cm.content.automation.util.SourceXMLUtil;
import org.lds.cm.content.automation.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class TestCIXProcess {

	/**
	 * Test the three endpoints for CIX files:
	 * 	- transform
	 * 	- preview
	 * 	- publish
	 * 
	 * I can improve this by creating a getDocInfo method for pre-transform CIX files and use that to compare to the preview and published files.
	 */
	@Test
	public void testCIXFiles() {
		
		// get files to transform
		List<File> filePaths = new ArrayList<>();
		filePaths = SourceXMLUtil.getCIXFiles();
		System.out.println("Number of files: " + filePaths.size());
		
		List<String> failsTransform = new ArrayList<>();
		List<String> failsPublish = new ArrayList<>();
		
		try {
			int i = 0;
			for (File file : filePaths) {
				String fileName = file.getName();
				if (file.exists()) {
					System.out.println("\nFile " + ++i + " out of " + filePaths.size());
					// transform CIX files
					System.out.println("CIX transform:");
					JSONObject transformJson = null;
					transformJson = EndpointUtil.transformFile(file);
					// make sure transform endpoint is working, we should get json back
					Assert.assertTrue(transformJson != null, "Transform endpoint returned nothing for file: " + fileName);
				
					// preview CIX HTML5
					if ((boolean) transformJson.get("transformSuccess")) {
						System.out.println("CIX preview:");
						String html5String =  null;
						String previewUrl = transformJson.get("previewUrl").toString();
						System.out.println("preview url: " + previewUrl);
						String batchGuid = previewUrl.substring(previewUrl.lastIndexOf("=") + 1);
						html5String = NetUtils.getHTML(previewUrl);
						
						// make sure preview url returns html5
						Assert.assertTrue(html5String != null, "Preview URL returned nothing: " + previewUrl);
						
						File tempFile = new File("C:/TEMP/tempFile.html");
						FileUtil.copyContentsToFile(html5String, tempFile);
						DocInfo docInfoPreview = XMLUtils.getHtml5DocInfo(tempFile);

						// make sure returned html5 doc info matches process log info
						String query = "select * from process_log where batch_guid = '" + batchGuid + "'";
						System.out.println("Checking process log with query: " + query);
						ResultSet rs = JDBCUtils.getResultSet(query);
						rs.next();
						Assert.assertTrue(rs.getString("NAVIGATION_URI").contains(docInfoPreview.getDataUri()), "Batch GUID does not match URI.");
						
						// publish with batch guid
						System.out.println("CIX publish:");
						JSONObject publishJson = null;
						publishJson = EndpointUtil.publishBroadcast(batchGuid);
						// endpoint should return json
						Assert.assertTrue(publishJson != null, "");

						// retrieve published files from broadcast-ldsxml
						if (publishJson.get("success").toString().equals("1")) {
							String ldsXMLPath = "/preview/content-automation/broadcast-ldsxml/" + fileName;
							File publishedFile = MarkLogicUtils.readFileFromML(ldsXMLPath, false);
							DocInfo docInfoPublished = XMLUtils.getBroadcastXMLDocInfo(publishedFile);
//							System.out.println("Preview title: " + docInfoPreview.getTitle());
//							System.out.println("Published title: " + docInfoPublished.getTitle());
							Assert.assertTrue(
									docInfoPreview.getTitle().equals(docInfoPublished.getTitle()), 
									"Published document title does not match preview file title."
									);
//							System.out.println("Preview content type: " + docInfoPreview.getDataContentType());
//							System.out.println("Published content type: " + docInfoPublished.getDataContentType());
							Assert.assertTrue(
									docInfoPreview.getContentType().equals(docInfoPublished.getContentType()) ||
									docInfoPreview.getContentType().contains(docInfoPublished.getContentType()), 
									"Published content type does not match preview content type."
									);
//							System.out.println("Preview file ID: " + docInfoPreview.getFile());
//							System.out.println("Published file ID: " + docInfoPublished.getFile());
							Assert.assertTrue(
									docInfoPreview.getFile().equals(docInfoPublished.getFile()) ||
									docInfoPreview.getFile().contains(docInfoPublished.getFile()),
									"Published file ID does not match preview file ID."
									);
							System.out.println("Published document matches preview document.");
						} else {
							System.out.println("Publish to broadcast-ldsxml failed.");
							failsPublish.add(file.getAbsolutePath());
						}
					} else {
						System.out.println("File failed to transform.");
						failsTransform.add(file.getAbsolutePath());
					}
				} else {
					System.out.println("File does not exist: " + fileName);
				}
			}
			
			System.out.println("\nFailed to transform: " + failsTransform);
			System.out.println("Failed to publish:" + failsPublish);
			
		} catch (ParseException | IOException | ParserConfigurationException | SAXException | SQLException e) {
			e.printStackTrace();
		}
	}
}
