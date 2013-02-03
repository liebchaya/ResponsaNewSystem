package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import utils.MapUtils;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

public class GroupMorphVariants {

	/**
	 * @param args
	 * @throws MorphLemmatizerException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws MorphLemmatizerException, IOException {
		MorphLemmatizer.initLemmatizer("C:\\projects_ws\\Tagger\\", true);
		File dir = new File("C:\\lowFreq\\all");
		for (File f:dir.listFiles()) {
			if (f.getName().endsWith(".lowfreq")) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			HashMap<String,HashSet<String>> triples = new HashMap<String, HashSet<String>>();
			HashMap<String,Integer> counts = new HashMap<String, Integer>();
			String line = reader.readLine();
			while (line!=null){
				String[] tokens = line.split(" ");
				String key = "";
				for(int i=0; i<line.split(" ").length-1; i++){
					key = key + " " + MorphLemmatizer.getProbableLemma(tokens[i]);
				}
				key = key.trim();
				int count = Integer.parseInt(tokens[line.split(" ").length-1]);
				if(triples.containsKey(key)){
					triples.get(key).add(line.substring(0,line.lastIndexOf(" ")));
					int prevCount =counts.get(key);
					counts.put(key, prevCount+count);
				}
				else {
					HashSet<String> list = new HashSet<String>();
					list.add(line.substring(0,line.lastIndexOf(" ")));
					triples.put(key, list);
					counts.put(key, count);
				}
				line = reader.readLine();
			}
			reader.close();
			Map<String, Integer> sortedMap = MapUtils.sortByValue(counts);
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\lowFreq\\all\\" + f.getName().replace(".lowfreq",".comp")));
			for(String key:sortedMap.keySet()){
				writer.write(key+"\t"+triples.get(key)+"\t"+counts.get(key)+"\n");
			}
			writer.close();
			}
		}
	}

	

}
