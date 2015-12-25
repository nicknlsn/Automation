package org.lds.cm.content.automation.enums;

public enum MarkLogicDatabase {
	Preview ("/preview/content-automation/content"), Published ("/published/content-automation/content"), 
	uriMapping ("/preview/content-automation/uri-mapping/");

	private final String contentRoot;
	private final String webmlRoot = "/webml/ldsorg content";
	
	private MarkLogicDatabase (String contentRoot) {
		this.contentRoot = contentRoot;
	}
	
	public String getContentRoot () {
		return contentRoot;
	}
	
	public String getWebmlRoot () {
		return webmlRoot;
	}
}

