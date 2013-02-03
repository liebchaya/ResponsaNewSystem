package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.MapUtils;


public class GenerateNgramGroupsData {
	
	
	public static void main(String[] args) throws IOException {
		File dir = new File("C:\\lowFreq\\all\\");
		for (File f:dir.listFiles()) {
			if (f.getName().endsWith(".comp")) {
				HashMap<String,Integer> bigrams = new HashMap<String, Integer>();
				HashMap<String,Integer> trigrams = new HashMap<String, Integer>();
				HashMap<String,Integer> fourgrams = new HashMap<String, Integer>();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = reader.readLine();
				while (line!=null){
					String[] tokens = line.split("\t");
					String key = tokens[0];
					int count = Integer.parseInt(tokens[2]);
					if (count > 1) {
						int n = key.split(" ").length;
						if (n==2)
							bigrams.put(key,count);
						else if (n==3)
							trigrams.put(key,count);
						else if (n==4)
							fourgrams.put(key,count);
					}
					line = reader.readLine();
				}
				reader.close();
				BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\lowFreq\\all\\" + f.getName().replace(".comp",".data")));
				Map<String, Integer> sortedMap = MapUtils.sortByValue(fourgrams);
				for (String four:sortedMap.keySet()){
					String keyHead = four.substring(0,four.lastIndexOf(" ")).trim();
					String keyTail = four.substring(four.indexOf(" ")).trim();
					int headCount = 1, tailCount = 1;
					if (trigrams.containsKey(keyHead))
						headCount = trigrams.get(keyHead);
					if (trigrams.containsKey(keyTail))
						tailCount = trigrams.get(keyTail);
					writer.write(four + "\t" + fourgrams.get(four) + "\t" + keyHead + "\t" + headCount + "\t" + keyTail + "\t" + tailCount + "\n");
				}
				sortedMap = MapUtils.sortByValue(trigrams);
				for (String tri:sortedMap.keySet()){
					String keyHead = tri.substring(0,tri.lastIndexOf(" ")).trim();
					String keyTail = tri.substring(tri.indexOf(" ")).trim();
					int headCount = 1, tailCount = 1;
					if (bigrams.containsKey(keyHead))
						headCount = bigrams.get(keyHead);
					if (bigrams.containsKey(keyTail))
						tailCount = bigrams.get(keyTail);
					writer.write(tri + "\t" + trigrams.get(tri) + "\t" + keyHead + "\t" + headCount + "\t" + keyTail + "\t" + tailCount + "\n");
				}
				writer.close();
			}
		}
	}
}
