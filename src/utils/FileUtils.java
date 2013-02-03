package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;

import org.mozilla.universalchardet.UniversalDetector;

public class FileUtils {
	
	/**
	 * Detect a file encoding
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileEncoding(File file) throws IOException{
		 byte[] buf = new byte[4096];
		    java.io.FileInputStream fis = new java.io.FileInputStream(file);

		    // (1)
		    UniversalDetector detector = new UniversalDetector(null);

		    // (2)
		    int nread;
		    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
		      detector.handleData(buf, 0, nread);
		    }
		    // (3)
		    detector.dataEnd();

		    // (4)
		    String encoding = detector.getDetectedCharset();
		    if (encoding != null) {
		      System.out.println("Detected encoding = " + encoding);
		    } else {
		      System.out.println("No encoding detected.");
		    }

		    // (5)
		    detector.reset();
		    return encoding;
	}
	
	/**
	 * Read a file into a String
	 * @param file
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String loadFileToString(File file, String encoding) throws IOException 
	{
		/*
		* To convert the File to String we use the
		* Reader.read(char[] buffer) method. We iterate until the
		* Reader return -1 which means there's no more data to
		* read. We use the StringWriter class to produce the string.
		*/
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		int n;
		while ((n = reader.read(buffer)) != -1) {
		writer.write(buffer, 0, n);
		}
		reader.close();
		return writer.toString();
	}
	
	/**
	 * Reads a text file into a list of strings, each containing one line of the file
	 * @param iFile
	 * @return
	 * @throws IOException
	 */
	public static HashSet<String> loadFileToSet(File iFile) throws IOException {
		HashSet<String> outList = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(iFile));
		String line;
		while ((line = reader.readLine()) != null) {
			outList.add(line);
		}
		return outList;
	}



}
