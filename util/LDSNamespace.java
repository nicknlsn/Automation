package org.lds.cm.content.automation.util;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class LDSNamespace implements NamespaceContext {

	@Override
	public String getNamespaceURI(String paramString) {
		if("lds".equals(paramString)) {
			return "http://www.lds.org/schema/lds-meta/v1";
		}
		throw new IllegalArgumentException(paramString);
	}

	@Override
	public String getPrefix(String paramString) {
		if("http://www.lds.org/schema/lds-meta/v1".equals(paramString)) {
			return "lds";
		}
		throw new IllegalArgumentException(paramString);
	}

	@Override
	public Iterator getPrefixes(String paramString) {
		throw new UnsupportedOperationException();
	}

}
