package org.lds.cm.content.automation.tests.endpoints;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.JDBCUtils;
import org.lds.cm.content.automation.util.NetUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class TestPreviewCSS {
	
	/**
	 * testCSSURLs: Test all the CSS URL's to see that they are valid
	 */
	@Test
	public static void testCSSURLs() {
		System.out.println();
		System.out.println("Test: testCSSURLs");
		System.out.println("Test: Test all the CSS URL's to see that they are valid.");
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		String endPoint = Constants.epPreviewCss;
		
		try {
			// get the json
			JSONObject jsonObject = NetUtils.getJson(endPoint);
		    JSONArray jsonArray = (JSONArray) jsonObject.get("previewCssFiles");
		    
		    List<String> failedCssURLs = new ArrayList<>();
		    
		    for (int i = 0; i < jsonArray.size(); i++) {
		    	JSONObject css = (JSONObject) jsonArray.get(i);
		    	System.out.println(css.get("name"));
		    	System.out.println(css.get("url"));
		    	
		    	int statusCode = NetUtils.getResponseStatus(css.get("url").toString());
		    	System.out.println("Status code: " + statusCode);

		    	if (statusCode != 200) {
		    		failedCssURLs.add(css.get("name").toString() + " returned " + statusCode);
		    	}
		    }
		    
		    Assert.assertTrue(failedCssURLs.size() == 0, "Failed CSS names: " + failedCssURLs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * compareURLs: compare the css urls in oracle with the ones returned from the endpoint, make sure they are the same
	 */
	@Test
	public static void testEndpoint() {
		System.out.println();
		System.out.println("Test: testEndpoint");
		System.out.println("Description: Compare the oracle table with what it returned from the end point.");
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		String endPoint = Constants.epPreviewCss;
		String query = "select * from preview_css where deleted_flag = 0";
		
		// run query and get result set
		ResultSet rs = JDBCUtils.getResultSet(query);
		
		try {
			// get json from endpoint
			JSONObject jsonObject = NetUtils.getJson(endPoint);
			JSONArray jsonArray = (JSONArray) jsonObject.get("previewCssFiles");
			Map<String, String> cssMap = new HashMap<>();
			
			// put items in a map
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject item = (JSONObject) jsonArray.get(i);
				cssMap.put(item.get("name").toString(), item.get("url").toString());
			}
			
			// compare endpoint results with query results
			// this is not done
			System.out.println("Comparing results...");
			List<String> failedCssURLs = new ArrayList<>();
			
		    while (rs.next()) {
		    	String url = rs.getString("URL");
		    	String nameKey = rs.getString("NAME");
		    	
		    	if (cssMap.containsKey(nameKey) && cssMap.get(nameKey).equals(url)) {
		    		System.out.println(cssMap.get(nameKey));
		    	} else {
		    		failedCssURLs.add(url);
		    	}
		    	
		    	//System.out.println(nameKey);
		    	//System.out.println(url);
		    }
		    
		    Assert.assertTrue(failedCssURLs.isEmpty(), "CSS that does not match: " + failedCssURLs);
		    
		    rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
	}
		
	/**
	 * Close the connection after all the tests have been run
	 */
	@AfterClass
	public void close() {
		JDBCUtils.closeAll();
	}

}

