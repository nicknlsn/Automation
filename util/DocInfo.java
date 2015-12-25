package org.lds.cm.content.automation.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class DocInfo {
	
	private String dataAid = null;
	private String dataAidVersion = null;
	private String contentType = null;
	private String dataUri = null;
	private String lang = null;
	private String thumbnail = null;
	private String file = null;
	private String title = null;
	
	public String getDataAid() {
		return dataAid;
	}
	public void setDataAid(String dataAid) {
		this.dataAid = dataAid;
	}
	public String getDataAidVersion() {
		return dataAidVersion;
	}
	public void setDataAidVersion(String dataAidVersion) {
		this.dataAidVersion = dataAidVersion;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String dataContentType) {
		this.contentType = dataContentType;
	}
	public String getDataUri() {
		return dataUri;
	}
	public void setDataUri(String dataUri) {
		this.dataUri = dataUri;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		try {
			double d = Double.parseDouble(lang);
			// search isoCodes.xml for language value
			// need to fix this to use FileUtil.getResourceFile() or something similar
			Document isoCodes = XMLUtils.getDocumentFromFile(new File("C:/Users/nicknlsn/Documents/content-automation.git/qa/src/main/resources/xslt/isoCodes.xml"));
			NodeList languageNodes = XMLUtils.getNodeListFromXpath(isoCodes, "//language[@code=\"" + lang + "\"]", null);
			Element languageElement = (Element) languageNodes.item(0);
			this.lang = languageElement.getAttribute("iso3");
		} catch(NumberFormatException e) {
			this.lang = lang;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
