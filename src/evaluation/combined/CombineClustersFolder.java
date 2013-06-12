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



public class CombineClustersFolder {
	
	private static HashMap<String, Integer> m_confRank = null;
	private static String m_inputFolder = "C:\\SONewStatisticsFolder\\";

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException{
		CombineClustersFolder.loadConfRank(new File("C:\\confRank.txt"));
		File confDir = new File(m_inputFolder);
		String outputDir = "C:\\CombineAll\\";
		HashSet<String> foConfSet = new HashSet<String>();
		HashSet<String> soConfSet = new HashSet<String>();
		for(File f:confDir.listFiles())
			if (f.isDirectory()&& !f.getName().contains("COVER")) {
				if(f.getName().contains("LIN"))
					soConfSet.add(f.getName());
				else
					foConfSet.add(f.getName());
			}
		for(String confFO:foConfSet)
			for(String confSO:soConfSet)
				combine2Files(confFO,confSO,outputDir);
		
	}
	
	public static void combine2Files(String foFile, String soFile, String outputDir) throws IOException, InterruptedException {
//		String foDir = "C:\\Combined\\Best_Surface\\clusters50_SUMSCORE_2\\clustersTop15RGSFinal.txt";
//		File soDir = new File("C:\\Combined\\All_Surface_Surface_LIN\\clusters50_SUMSCORE_2\\clustersTop15RGSFinal.txt");

		String fileName = "\\clusters50_SUMSCORE_2\\clustersTop15RGSFinal.txt";
		BufferedReader firstReader = null;
		BufferedReader secondReader = null;
		if(m_confRank.get(foFile) > m_confRank.get(soFile)){
			firstReader = new BufferedReader(new FileReader(m_inputFolder+soFile+fileName));
			secondReader = new BufferedReader(new FileReader(m_inputFolder+foFile+fileName));
		} else {
			firstReader = new BufferedReader(new FileReader(m_inputFolder+foFile+fileName));
			secondReader = new BufferedReader(new FileReader(m_inputFolder+soFile+fileName));
		}
		HashMap<String, LinkedList<String>> clsMap = new HashMap<String, LinkedList<String>>();
		
		String line = secondReader.readLine();
		String curTarget = null;
		while (line != null){
			if(line.split("\t").length == 1){
				curTarget = line.trim();
				clsMap.put(curTarget, new LinkedList<String>());
			}
			else {
				clsMap.get(curTarget).add(line.trim());
			}
			line = secondReader.readLine();
		}
		secondReader.close();
		
		line = firstReader.readLine();
		curTarget = null;
		while (line != null){
			if(line.split("\t").length == 1){
				curTarget = line.trim();
				if (!clsMap.containsKey(curTarget))
					clsMap.put(curTarget, new LinkedList<String>());
			}
			else {
				clsMap.get(curTarget).add(line.trim());
			}
			line = firstReader.readLine();
		}
		firstReader.close();
		
		List<String> sortedKeys=new ArrayList<String>(clsMap.keySet());
		Collections.sort(sortedKeys);
		
		String newDir =  outputDir + foFile+"__"+soFile+"\\clusters50_SUMSCORE_2\\";
		File newFolder = new File(newDir);
		newFolder.mkdirs();
		for(String target:sortedKeys){
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(newDir+target+".clusters")));
			for(String cls:clsMap.get(target))
				writer.write(cls+"\n");
			writer.close();
		}
		
	}
	
	public static void loadConfRank(File rankFile) throws IOException{
		m_confRank = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(rankFile));
		String line = reader.readLine();
		while(line != null){
			String[] tokens = line.split("\t");
			int rank = Integer.parseInt(tokens[1]);
			int confStartIndex = tokens[0].lastIndexOf('\\');
			String conf = tokens[0].substring(confStartIndex+1);
			m_confRank.put(conf,rank);
			line = reader.readLine();
		}
	}
	
}
