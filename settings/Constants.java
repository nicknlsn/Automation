package org.lds.cm.content.automation.settings;

import org.lds.stack.qa.TestConfig;

/**
 * A container for test environment property injection. {@link TestConfig}
 * will initialize each public, static, non-final field with a matching test
 * environment property. The test environment properties are selected based on
 * the value of the "testEnv" system property.
 * 
 * @see TestConfig
 *
 * @author <a href="http://code.lds.org/maven-sites/stack/">Stack Starter</a>
 */
public final class Constants {

	public static String mlPreviewHost;
	public static int mlPreviewPort;
	public static String mlPreviewUsername;
	public static String mlPreviewPassword;

	public static String mlPublishHost;
	public static int mlPublishPort;
	public static String mlPublishUsername;
	public static String mlPublishPassword;
	
	public static String dbUrl;
	public static String dbUsername;
	public static String dbPassword;
	public static String dbDriver;
	
	public static String epPreviewCss;
	public static String epTransform;
	public static String epPublishBroadcast;
	public static String epPreviewFileByBatchGuid;
	public static String epPreviewFileByFileID;
	public static String epPreviewFileByURI;
	
	public static String xmlRoot;
	public static String cixRoot;


	static {
		// Load test environment properties using the "testEnv" System property.
		new TestConfig().applyConfiguration(Constants.class);
	}

	// Private constructor to discourage instantiation.
	private Constants() {}
}
