package org.lds.cm.content.automation.util;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.lds.cm.content.automation.settings.Constants;

public class EndpointUtil {
	
	public String previewFileByBatchGuid(String batchGuid) {
		String fileContents = null;
		
		try {
			fileContents = NetUtils.getHTML(Constants.epPreviewFileByBatchGuid + batchGuid);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return fileContents;
	}
	
	public String previewFileByFileID(String fileID) {
		String fileContents = null;
		
		try {
			fileContents = NetUtils.getHTML(Constants.epPreviewFileByFileID + fileID);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return fileContents;
	}
	
	public String previewFileByURI(String uri) {
		String fileContents = null;

		try {
			fileContents = NetUtils.getHTML(Constants.epPreviewFileByURI + uri);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return fileContents;
	}
	
	/**
	 * Upload and transform a file. This method assumes the file exists.
	 * @param file
	 * @return jsonResponse
	 */
	public static JSONObject transformFile(File file) {
		JSONObject jsonResponse = null;
		String responseString = null;
		
		try {
			// build request
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(Constants.epTransform);
			HttpEntity entity1 = MultipartEntityBuilder.create().addBinaryBody("file", file).build();
			httppost.setEntity(entity1);

			// send request
			System.out.println("transforming file: " + file.getPath().substring(file.getPath().lastIndexOf("\\") + 1));
			System.out.println("executing request " + httppost.getRequestLine());
			CloseableHttpResponse response = httpClient.execute(httppost);

			// get response
//			System.out.println(response.getStatusLine());
			HttpEntity entity2 = response.getEntity();
			responseString = EntityUtils.toString(entity2);
			jsonResponse = (JSONObject) new JSONParser().parse(responseString);

			EntityUtils.consume(entity2);
			response.close();
			httpClient.close();
		} catch (org.json.simple.parser.ParseException | IOException e) {
			System.out.println(responseString);
			e.printStackTrace();
		}
		
		return jsonResponse;
	}
	
	public static JSONObject publishBroadcast(String batchGuid) {
		JSONObject jsonResponse = null;
		
		try {
			jsonResponse = NetUtils.getJson(Constants.epPublishBroadcast + batchGuid);
		} catch (ParseException | org.json.simple.parser.ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return jsonResponse;
	}
}
