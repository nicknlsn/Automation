package org.lds.cm.content.automation.util;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NetUtils {

	/**
	 * Fires a GET request to url and returns the status code.
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static int getResponseStatus(String url) throws ClientProtocolException, IOException {
		return HttpClients.createDefault().execute(new HttpGet(url)).getStatusLine().getStatusCode();
	}
	
	/**
	 * Fires a GET request to url and returns a JSON object
	 * @param url
	 * @return
	 * @throws ParseException
	 * @throws ClientProtocolException
	 * @throws org.json.simple.parser.ParseException
	 * @throws IOException
	 */
	public static JSONObject getJson(String url) throws ParseException, ClientProtocolException, org.json.simple.parser.ParseException, IOException {
		return (JSONObject) new JSONParser().parse(EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet(url)).getEntity()));
	}
	
	 /**
	  * Returns the html content of an HTTP request
	  * @param url
	  * @return
	  * @throws ParseException
	  * @throws ClientProtocolException
	  * @throws IOException
	  */
	public static String getHTML(String url) throws ParseException, ClientProtocolException, IOException {
		return EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet(url)).getEntity());
	}
}
