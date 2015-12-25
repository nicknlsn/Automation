package org.lds.cm.content.automation.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.lds.cm.content.automation.settings.Constants;

/**
 * This class can be used to get random source files to use in tests.
 * @author nicknlsn
 *
 */
public class SourceXMLUtil {
	
	/**
	 * Returns the CIX files from the location specified by cixRoot in the properties.
	 * @return
	 */
	public static List<File> getCIXFiles() {
		return getAllSourceFiles(Constants.cixRoot);
	}
	
	/**
	 * Returns all the source files of the given type and language.
	 * @param type
	 * @param language
	 * @return
	 */
	public static List<File> getSourceFiles(String type, String language) {
		return getAllSourceFiles(Constants.xmlRoot + type + (language.equals("ALL") ? "" : "/" + language));
	}
	
	/**
	 * Returns all files in the given directory. Use this method for getting scriptures because they don't follow the same naming pattern for languages.
	 * @param root
	 * @return
	 */
	public static List<File> getAllSourceFiles(String root) {
		return new ArrayList<>(FileUtils.listFiles(new File(root), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
	}
	
	/**
	 * Returns numFiles random files from the root of all source files as specified by xmlRoot in the properties.
	 * @param numFiles
	 * @return
	 */
	public static List<File> getRandomSourceFiles(int numFiles) {
		return getRandomSourceFiles(Constants.xmlRoot, numFiles);
	}
	
	/**
	 * Returns numFiles random files from the location specified by the type and language.
	 * @param type
	 * @param language
	 * @param numFiles
	 * @return
	 */
	public static List<File> getRandomSourceFiles(String type, String language, int numFiles) {
		return getRandomSourceFiles(Constants.xmlRoot + type + (language.equals("ALL") ? "" : "/" + language), numFiles);
	}
	
	/**
	 * Returns numFiles random files from the directory specified by root.
	 * @param root
	 * @param numFiles
	 * @return
	 */
	public static List<File> getRandomSourceFiles(String root, int numFiles) {
		File rootDirectory = new File(root);
		Collection<File> collection = FileUtils.listFiles(rootDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		List<File> filePaths = new ArrayList<>();
		
		// if numFiles is greater than collection then just return all in collection.
		if (numFiles > collection.size()) {
			filePaths = new ArrayList<>(collection);
		} else {
			// generate numFiles random numbers between 0 and the size of collection.
			Random r = new Random();
			int size = collection.size();
			List<File> allFiles = new ArrayList<>(collection);
			for (int i = 0; i < numFiles; i++) {
				filePaths.add(allFiles.get(r.nextInt(size)));
			}
		}
		return filePaths;
	}
}
