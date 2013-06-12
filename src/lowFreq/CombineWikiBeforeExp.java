package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import ac.biu.nlp.nlp.general.file.FileUtils;

import obj.Pair;

import utils.TargetTerm2Id;

public class CombineWikiBeforeExp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		HashMap<Integer, String> origMap = new HashMap<Integer, String>();
		HashMap<Integer, String> wikiMap = new HashMap<Integer, String>();
		String origTargetTerm = "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_morph.txt";
		String wikiTargetTerm = "/home/ir/liebesc/ResponsaSys/input/targetTerms_wikiOnly_morph.txt";

		BufferedReader reader = new BufferedReader(new FileReader(origTargetTerm));
		String line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			origMap.put(Integer.parseInt(line.substring(0,index)),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(wikiTargetTerm));
		line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			wikiMap.put(Integer.parseInt(line.substring(0,index)),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		
		String wikiFolderStr = "/home/ir/liebesc/ResponsaSys/output/wikiNoOrig/Surface_Surface";
		String origFolderStr = "/home/ir/liebesc/ResponsaSys/output/baseline/Surface_Surface";
		String newFolder = "/home/ir/liebesc/ResponsaSys/output/baselineIntersectionWiki/Surface_Surface";
		
		
		for(int id:origMap.keySet()){
			File wikiFile = new File(wikiFolderStr+"/"+id+"_Data.txt");
			File origFile = new File(origFolderStr+"/"+id+"_Data.txt");
			if (wikiFile.exists() && !origFile.exists())
				FileUtils.copyFile(wikiFile, new File(newFolder+"/"+id+"_Data.txt"));
			else if (!wikiFile.exists() && origFile.exists())
				FileUtils.copyFile(origFile, new File(newFolder+"/"+id+"_Data.txt"));
			else if (wikiFile.exists() && origFile.exists()){
				HashMap<String,String> wikiData = new HashMap<String, String>();
				reader = new BufferedReader(new FileReader(wikiFile));
				line = reader.readLine();//skip first line
				line = reader.readLine();
				while (line != null){
					int index = line.indexOf("\t");
					wikiData.put(line.substring(0,index),line.substring(index+1));
					line = reader.readLine();
				}
				reader.close();
				BufferedWriter writer = new BufferedWriter(new FileWriter(newFolder+"/"+id+"_Data.txt"));
				reader = new BufferedReader(new FileReader(origFile));
				line = reader.readLine();//skip first line
				line = reader.readLine();
				while (line != null){
					int index = line.indexOf("\t");
					String term = line.substring(0,index);
					if (wikiData.containsKey(term)){
						writer.write(line + "\t" + wikiData.get(term) + "\t" + origMap.get(id).split("\t").length + "\t" + wikiMap.get(id).split("\t").length + "\n");
					}
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
	
	}

}
