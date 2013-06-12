package evaluation.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import utils.StringUtils;

import ac.biu.nlp.nlp.general.file.FileUtils;



public class CombineClusters {
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException{
		String foDir = "C:\\Combined\\Best_Surface\\clusters50_SUMSCORE_2\\clustersTop15RGSFinal.txt";
		String soDir = "C:\\Combined\\All_Surface_Surface_LIN\\clusters50_SUMSCORE_2\\clustersTop15RGSFinal.txt";
		String outputDir = "C:\\Combined\\CombinedPer\\clusters50_SUMSCORE_2";
		
		combine2Files(foDir,soDir,outputDir);
	}
	
	public static void combine2Files(String foFile, String soFile, String outputDir) throws IOException, InterruptedException {
		BufferedReader firstReader = null;
		BufferedReader secondReader = null;
		
		firstReader = new BufferedReader(new FileReader(foFile));
		secondReader = new BufferedReader(new FileReader(soFile));
		
		HashMap<String, LinkedList<String>> clsMap = new HashMap<String, LinkedList<String>>();
		
		Boolean bFO = true;
		int counter = 0;
		String line = firstReader.readLine();
		
		String curTarget = null;
		while (line != null){
			if(line.split("\t").length == 1){
				counter = 0;
				curTarget = line.trim();
				clsMap.put(curTarget, new LinkedList<String>());
			}
			else {
				if(counter < 10)
					clsMap.get(curTarget).add(line.trim());
				counter++;
			}
			line = firstReader.readLine();
		}
		firstReader.close();
		
		line = secondReader.readLine();
		curTarget = null;
		while (line != null){
			if(line.split("\t").length == 1){
				counter = 0;
				bFO = true;
				curTarget = line.trim();
				if (!clsMap.containsKey(curTarget)){
					clsMap.put(curTarget, new LinkedList<String>());
					bFO = false;
				}
			}
			else {
				if (counter < 5 || ( counter < 15 && !bFO))
					clsMap.get(curTarget).add(line.trim());
				counter++;
			}
			line = secondReader.readLine();
		}
		secondReader.close();
		
		List<String> sortedKeys=new ArrayList<String>(clsMap.keySet());
		Collections.sort(sortedKeys);
		
		String newDir =  outputDir + "\\";
		File newFolder = new File(newDir);
		newFolder.mkdirs();
		for(String target:sortedKeys){
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(newDir+target+".clusters")));
			for(String cls:clsMap.get(target))
				writer.write(cls+"\n");
			writer.close();
		}
		
	}
}
