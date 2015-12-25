package org.lds.cm.content.automation.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lds.cm.content.automation.enums.MarkLogicDatabase;
import org.lds.cm.content.automation.settings.Constants;
import org.w3c.dom.Document;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.RawQueryByExampleDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;

public class MarkLogicUtils {
	private static DatabaseClient workingClient = null;
	private static DatabaseClient publishedClient = null;
	private static XMLDocumentManager docMgr = null;
	
	
	/////////////////////// BEGIN Read Methods /////////////////////////////
	public static File readFileFromML (String path, Boolean addDoctype) throws ResourceNotFoundException {
		File file = getDocManager(MarkLogicDatabase.Preview).read(path,  new FileHandle()).get();
		
		if (addDoctype) {
			String fileContents = FileUtil.readFileToString(file);
			
			FileUtil.copyContentsToFile (fileContents, file);
		}
		return file;
	}
	
	public static File readFileFromML(String path) throws ResourceNotFoundException{
		return readFileFromML(path, true);
	}
	
	public static Boolean docExists (String path) {
		if (getDocManager(MarkLogicDatabase.Preview).exists(path) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static List<Document> findHtml5ByExample (String example, MarkLogicDatabase database) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return runQueryByExample(example, database, database.getContentRoot());
	}

	public static List<Document> findMediaXMLByExample (String example, MarkLogicDatabase database) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return runQueryByExample(example, database, database.getWebmlRoot());
	}

	private static List<Document> runQueryByExample (String example, MarkLogicDatabase database, String rootDirectory) {
		DatabaseClient client = getConnection(database);
		QueryManager queryMgr = client.newQueryManager();
		
		String query = 	"<q:qbe xmlns:q='http://marklogic.com/appservices/querybyexample' xmlns:lds='http://www.lds.org/schema/lds-meta/v1'>" +
							"<q:query>" +
								example + 
							"</q:query>" +
						"</q:qbe>";
		
		StringHandle rawHandle = new StringHandle(query);
		
		RawQueryByExampleDefinition queryDef = queryMgr.newRawQueryByExampleDefinition(rawHandle);
		
		queryDef.setDirectory(rootDirectory + "/");
		
		queryMgr.setPageLength(100);
		SearchHandle resultsHandle = queryMgr.search(queryDef, new SearchHandle());
		
		MatchDocumentSummary[] results = resultsHandle.getMatchResults();
		Document doc = null;
		List<Document> docs = new ArrayList<>();
		if (results.length > 0) {
			for(int index = 0; index < results.length; index++) {
				String uri = results[index].getUri();
				XMLDocumentManager docMgr = client.newXMLDocumentManager();
				DOMHandle handle = new DOMHandle();
				docMgr.read(uri, handle);
				doc = handle.get();
				docs.add(doc);
			}
		}
		
		return docs;
	}
	
	public static List<Document> findHtml5ByFileId(String fileId, MarkLogicDatabase database, String rootDirectory, Integer items) {
		DatabaseClient client = getConnection(database);
		QueryManager queryMgr = client.newQueryManager();
		
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder();
		List<StructuredQueryDefinition> definitions = new ArrayList<>();
		definitions.add(qb.value(qb.field("identifiers"), null, new String[] {"wildcarded"}, 1.0, fileId + "*"));

		StructuredQueryDefinition queryDef = qb.and(definitions.toArray(new StructuredQueryDefinition[definitions.size()]));
		queryDef.setDirectory(rootDirectory);
		queryMgr.setPageLength(items);

		SearchHandle resultsHandle = queryMgr.search(queryDef, new SearchHandle());
		
		MatchDocumentSummary[] results = resultsHandle.getMatchResults();

		Document doc = null;
		List<Document> docs = new ArrayList<>();
		if (results.length > 0) {
			for(int i = 0; i < results.length; i++) {
				String uris = results[i].getUri();
				XMLDocumentManager docMgr = client.newXMLDocumentManager();
				DOMHandle handle = new DOMHandle();
				docMgr.read(uris, handle);
				doc = handle.get();
				docs.add(doc);
			}
		}
		return docs;
	}

	/////////////////////// END Read Methods /////////////////////////////
	
	/////////////////////// BEGIN Delete Methods /////////////////////////////
	public static void deleteFileFromMarkLogic (MarkLogicDatabase db, String path) {
		getDocManager(db).delete(path);
	}
	
	public static void deleteFileFromMarkLogic (MarkLogicDatabase db, List<String> paths) {
		for (String path: paths) getDocManager(db).delete(path);
	}
	
	/////////////////////// END Delete Methods /////////////////////////////
	
	/////////////////////// BEGIN Connection Methods /////////////////////////////
	private static XMLDocumentManager getDocManager (MarkLogicDatabase db) {
		return getConnection(db).newXMLDocumentManager();
	}
	
	private static DatabaseClient getConnection(MarkLogicDatabase db) {
		DatabaseClient client = null;
		switch(db) {
			case Preview:
				if(null == workingClient) {
					workingClient = DatabaseClientFactory.newClient(Constants.mlPreviewHost, Constants.mlPreviewPort, Constants.mlPreviewUsername, Constants.mlPreviewPassword, Authentication.DIGEST);
				}
				client = workingClient;
				break;
			case Published:
				if(null == publishedClient) {
					publishedClient = DatabaseClientFactory.newClient(Constants.mlPublishHost, Constants.mlPublishPort, Constants.mlPublishUsername, Constants.mlPublishPassword, Authentication.DIGEST);
				}
				client = publishedClient;
				break;
			case uriMapping:
				if(null == workingClient) {
					workingClient = DatabaseClientFactory.newClient(Constants.mlPreviewHost, Constants.mlPreviewPort, Constants.mlPreviewUsername, Constants.mlPreviewPassword, Authentication.DIGEST);
				}
				client = workingClient;
				break;
		}
		return client;
	}
	
	/////////////////////// END Connection Methods /////////////////////////////

}
