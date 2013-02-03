package so;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FormatSSORules {
	
	public static void FormatRules(File rulesFile, String outputDir) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(rulesFile));
		String outputFolder = null;
		if (rulesFile.getName().equals("linRules.txt"))
			outputFolder = outputDir + "/LIN";
		else //if (rulesFile.equals("balRules.txt"))
			outputFolder = outputDir + "/COVER";
		File folder = new File(outputFolder);
		if(!folder.exists())
			folder.mkdir();
		
		String prevType = null, prevQuery = null;
		File typeFolder = null; 
		String line = reader.readLine();
		BufferedWriter writer = null;
		while (line!= null) {
			String[] tokens = line.split("\t");
			String type = tokens[0].split("_")[1];
			String query = tokens[0];
			if (!type.equals(prevType)) {
				typeFolder = new File(outputFolder + "/" + type);
				if (!typeFolder.exists())
					typeFolder.mkdir();
			}
			if (!query.equals(prevQuery)) {
				if (writer != null)
					writer.close();
				writer = new BufferedWriter(new FileWriter(typeFolder.getAbsolutePath()+"/"+query.split("_")[0] + ".txt"));
			}
			writer.write(tokens[1] + "\t" + tokens[2] + "\n");
			prevType = type;
			prevQuery = query;	
			line = reader.readLine();
		}
		reader.close();
		writer.close();
	}
	

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FormatSSORules.FormatRules(new File("C:\\SOStatisticsFolder\\Surface_All\\balRules.txt"), "C:\\SOStatisticsFolder\\Surface_All");

	}

}
