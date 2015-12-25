package org.lds.cm.content.automation.tests.endpoints;

import java.io.File;
import java.util.List;

import org.json.simple.JSONObject;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.EndpointUtil;
import org.lds.cm.content.automation.util.SourceXMLUtil;
import org.testng.annotations.Test;

public class TestOpenEndpoints {
	
	/**
	 * Test the end point that retrieves a file based on the batch guid. This only works if one file at a time is transformed because multiple
	 * files transformed at the same time will each have the same batch guid.
	 * 
	 * I think this could be changed to implement the transform end point for one file at a time, and then try to retrieve the file.
	 */
//	@Test
//	public void testPreviewFile() {
//		System.out.println();
//		System.out.println("Test: testPreviewFile");
//		System.out.println("Test: Make sure the preview file end point returns the correct file with the correct css.");
//		System.out.println("ML HOST: " + Constants.mlPreviewHost);
//		
//		List<String> failedCases = new ArrayList<>();
//		
//		// get result set for successful transforms
//		String query = "select * from process_log where status = 'SUCCESSFUL' and process_name = 'TRANSFORM' order by MODIFIED_DATE desc";
//		ResultSet rs = JDBCUtils.getResultSet(query);
//		
//		// loop through batch_guid and get files with end point
//		try {
//			for (int i = 0; i < 100; i++) {
//				rs.next();
//				String uriFromDB = rs.getString("NAVIGATION_URI");
//				//String fileNameFromDB = rs.getString("FILE_NAME");
//				String batch_guid = rs.getString("BATCH_GUID");
//				System.out.println("Testing file: " + uriFromDB);
//				
//				
//				// get html from endpoint
//				String htmlString = NetUtils.getHTML(Constants.epPreviewFileByBatchGuid + batch_guid);
//				Pattern pattern = Pattern.compile("data-uri=\"(.*?)\" ");
//				Matcher matcher = pattern.matcher(htmlString);
//				if (matcher.find()) {
//					String uriFromFile = matcher.group(1);
//					//System.out.println(", URI: " + uriFromFile);
//					
//					// compare uri from result set with uri in file
//					if (uriFromDB != null) {
//						if (uriFromDB.contains(uriFromFile)) {
//							System.out.println(uriFromDB);
//						} else {
//							failedCases.add("URI from DB: " + uriFromDB + " did not match URI from file: " + uriFromFile);
//						}
//					}
//					
//					// also check that correct css is present
//					
//					
//				} else {
//					System.out.println("Could not find URI");
//					// more error handling here
//				}
//			}
//			
//			Assert.assertTrue(failedCases.isEmpty(), "Failed cases: " + failedCases);
//		} catch (SQLException | ParseException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * test the transformed file with endpoints to see that they work and return the correct file
	 * the json passed in is the response from the transform endpoint containing validation errors
	 * @param json
	 */
	public static void preview(JSONObject json) {
		if (json.get("transformSuccess").equals("true")) {
			
		}
	}
	
	/**
	 * update this method
	 */
	@Test
	public static void testTransform() {
		System.out.println();
		System.out.println("Test method: testTransform");
		System.out.println("Test: Test the transform endpoint.");
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		List<File> filePaths = SourceXMLUtil.getRandomSourceFiles(10);
		
		for (File file : filePaths) {
			JSONObject response = EndpointUtil.transformFile(file);
			
			preview(response);
		}
	}
}
