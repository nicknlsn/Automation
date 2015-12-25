package org.lds.cm.content.automation.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileUtil {
	public static File getResourceFile (String fileLocation) {
		//Get file from resources folder
		ClassLoader classLoader = FileUtil.class.getClassLoader();
		URL resource = classLoader.getResource(fileLocation);
		
		File file = null;
		if (resource != null) {
			file = new File(resource.getFile());
		}
		
		return file;
	}
	
	public static void copyContentsToFile (String fileContents, File file) {
		if (! fileContents.contains("<!DOCTYPE html>")) {
			StringBuilder newFileContents = new StringBuilder("<!DOCTYPE html>");
			newFileContents.append(fileContents);

			FileUtil.writeContentsToFile(file, newFileContents.toString());
		} else {
			FileUtil.writeContentsToFile(file, fileContents);
		}
	}

	public static void writeContentsToFile (File file, String contents) {
		// write contents to new file
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, false);
			writer.write(contents);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}

	}
	
	public static void writeContentsToFile (File file, InputStream contents) throws IOException {
		Path path = file.toPath();
		Files.copy(contents, path, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public static String readFileToString(File file) {
		String fileContent = null;
		
		try {
			fileContent = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileContent;
	}

}
