package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import utils.StringUtils;

public class CopyAnnotations4RelativeRecall {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String prevJudgements = "C:/ResponsaSys/judgements-allNew/current";
		String curJudgments = "C:/ResponsaSys/judgements/export";
		String newJudgments = "C:/ResponsaSys/judgements/copy";
		
		File curJudgementsDir = new File(curJudgments);
		for(File f:curJudgementsDir.listFiles()) {
			File prevJudgementsFile = new File(prevJudgements+"/"+f.getName());
			if (!prevJudgementsFile.exists()){
				System.out.println("Missing file: " + prevJudgementsFile.getAbsolutePath());
				System.exit(0);
			}
			HashMap<String, Integer> data = loadFileDataToMap(prevJudgementsFile);
			
			FileInputStream fis = new FileInputStream(f);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(fis,"cp1255"));
	        FileOutputStream fos = new FileOutputStream(new File(newJudgments+"/"+f.getName()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "Cp1255"));
			
			String line = reader.readLine();//read header
			writer.write(line+"\n");
			line=reader.readLine();//read first line
			while(line!=null){
				String[] tokens = line.split("\t");
				int judgment = Integer.parseInt(tokens[2]);
				if (judgment != -99)
					writer.write(line+"\n");
				else {
					if (!data.containsKey(StringUtils.fixAnnotatedTerm(tokens[1]))){
						System.out.println("Missing: " + f.getName() + "term: " + tokens[1]);
						System.exit(0);
					}
					else {
						writer.write(tokens[0] + "\t" + tokens[1] + "\t" + data.get(tokens[1]) + "\n");
					}
				}
				line=reader.readLine();
			}
			reader.close();
			writer.close();
		}

	}
	
	private static HashMap<String,Integer> loadFileDataToMap(File f) throws IOException{
		HashMap<String,Integer> data = new HashMap<String, Integer>();
		FileInputStream fis = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis,"cp1255"));
		String line = reader.readLine();//read header
		line=reader.readLine();//read first line
		while(line!=null){
			String[] tokens = line.split("\t");
			data.put(StringUtils.fixAnnotatedTerm(tokens[1]), Integer.parseInt(tokens[2]));
			line=reader.readLine();
		}
		reader.close();
		return data;
	}

}
