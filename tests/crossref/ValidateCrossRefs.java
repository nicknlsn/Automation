package org.lds.cm.content.automation.tests.crossref;

import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.lds.cm.content.automation.util.JDBCUtils;
import org.lds.cm.content.automation.util.MarkLogicUtils;
import org.testng.annotations.Test;

public class ValidateCrossRefs {
	
	/**
	 * Make sure cross refs are formed correctly in the unresolved state.
	 * Make sure unresolved cross refs are resolved and activated in preview and publish.
	 */
	@Test
	public void resolveAndActivateCrossRefs() {
		System.out.println("ML HOST: " + Constants.mlPreviewHost);
		
		// clear files from preview and publish
		String uriPreview = MarkLogicDatabase.Preview.getContentRoot() + "/eng/manual/jesus-christ-and-the-everlasting-gospel-teacher-manual";
		String uriPublished = MarkLogicDatabase.Published.getContentRoot() + "/eng/manual/jesus-christ-and-the-everlasting-gospel-teacher-manual";
		MarkLogicUtils.deleteFileFromMarkLogic(MarkLogicDatabase.Preview, uriPreview + "/lesson-2-jesus-christ-is-central-to-all-human-history.html");
		MarkLogicUtils.deleteFileFromMarkLogic(MarkLogicDatabase.Preview, uriPreview + "/lesson-3-jehovah-and-his-premortal-ministry.html");
		MarkLogicUtils.deleteFileFromMarkLogic(MarkLogicDatabase.Published, uriPublished + "/lesson-2-jesus-christ-is-central-to-all-human-history.html");
		MarkLogicUtils.deleteFileFromMarkLogic(MarkLogicDatabase.Published, uriPublished + "/lesson-3-jehovah-and-his-premortal-ministry.html");
		
		// clear uri files
		String uriFileToDelete = MarkLogicDatabase.uriMapping.getContentRoot() + "PD10052296_000_000-uris.xml";
		MarkLogicUtils.deleteFileFromMarkLogic(MarkLogicDatabase.uriMapping, uriFileToDelete);
		
		// clear content_publish table
		String query = "delete from content_publish";
		JDBCUtils.executeQuery(query);
		
		// transform first manual
//		String pathManual1 = "C:/Users/nicknlsn/Documents/html5-migration-source.git/manual/eng/PD10052296_000_000.xml";
//		File fileToTransform = new File(pathManual1);
//		Boolean transformResponse = (Boolean) EndpointUtil.transformFile(fileToTransform).get("transformSuccess");
//		System.out.println(transformResponse);
		// check xrefs
		// to do this: 
		
		
		// publish first manual
		// check xrefs
		
		// transform second manual
		// check xrefs in first manual in preview
		
		// publish second manual
		// check xrefs in first manual in publish
	}

}
