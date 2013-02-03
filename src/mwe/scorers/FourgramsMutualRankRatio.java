package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import obj.Pair;

import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;
import org.ardverk.collection.PatriciaTrie;

public class FourgramsMutualRankRatio implements MWEScorer {

	public static void main(String[] args) throws IOException{
		FourgramsMutualRankRatio fMRR = new FourgramsMutualRankRatio(new File("C:\\responsa4.cnt"));
		System.out.println("המוציא מחבירו עליו הראיה " + fMRR.score("המוציא מחבירו עליו הראיה"));
		System.out.println("יפתח בדורו כשמואל בדורו " + fMRR.score("יפתח בדורו כשמואל בדורו"));
		System.out.println("הרבנים האחרונים הקרובים אלינו " + fMRR.score("הרבנים האחרונים הקרובים אלינו"));
		System.out.println("נאמר כבד את אביך " + fMRR.score("נאמר כבד את אביך"));
	}
	
	public String getName() {
		return m_name;
	}
	
	//mutualRankRatio
	public double score(String fourgram){
		double score = Math.pow(rankRatio(fourgram, 0)*rankRatio(fourgram, 1)*rankRatio(fourgram, 2)*rankRatio(fourgram, 3),0.25);
		return score;
	}
	
	public boolean exist(String fourgram){ 
		if (m_prefixTrie.containsKey(fourgram))
			return true;
		return false;
		
	}
	
	private double rankRatio(String fourgram, int pos){
		double score = 0.0;
		Map<String, Integer> ERcontex = new LinkedHashMap<String, Integer>();
		Map<String, Integer> ARcontex = new HashMap<String, Integer>();
		String prefix = fourgram.split(" ")[pos]+ " ";
		for(String entry:m_prefixTrie.prefixMap(prefix).keySet()){
			ARcontex.put(entry, m_prefixTrie.get(entry).key());
			ERcontex.put(entry, m_prefixTrie.get(entry).value());
		}
		for(String entry:m_sufixTrie.prefixMap(prefix).keySet()){
			String [] fourgramSplit = entry.split(" ");
			String revFourgram = fourgramSplit[3]+ " " + fourgramSplit[2]+ " " + fourgramSplit[1]+ " " + fourgramSplit[0];
			ARcontex.put(revFourgram, m_sufixTrie.get(entry).key());
			ERcontex.put(revFourgram, m_sufixTrie.get(entry).value());
		}
		for(String entry:m_mid1Trie.prefixMap(prefix).keySet()){
			String [] fourgramSplit = entry.split(" ");
			String fourTrigram = fourgramSplit[1]+ " " + fourgramSplit[0]+ " " + fourgramSplit[2]+ " " + fourgramSplit[3];
			ARcontex.put(fourTrigram, m_mid1Trie.get(entry).key());
			ERcontex.put(fourTrigram, m_mid1Trie.get(entry).value());
		}
		for(String entry:m_mid2Trie.prefixMap(prefix).keySet()){
			String [] fourgramSplit = entry.split(" ");
			String revFourgram = fourgramSplit[2]+ " " + fourgramSplit[1]+ " " + fourgramSplit[0]+ " " + fourgramSplit[3];
			ARcontex.put(revFourgram, m_mid2Trie.get(entry).key());
			ERcontex.put(revFourgram, m_mid2Trie.get(entry).value());
		}
//		System.out.println("Finished loading contex data for " + trigram + " " + pos);
		ARcontex = utils.MapUtils.sortByValue(ARcontex);
		double ARrank = 0;
		int rankCounter = 1;
		int prevFreq = 0;
		boolean bFound = false;
		for (String key:ARcontex.keySet()){
			int freq = ARcontex.get(key);
			if (bFound) {
				if (freq != prevFreq){
					if (rankCounter > 1)
						ARrank = (ARrank-1) + 1/(double)(rankCounter-1);
					break;
				} else {
					rankCounter++;
				}
			}
			if (key.equals(fourgram))
				bFound = true;
			if (freq != prevFreq){
				ARrank++;
				rankCounter = 1;
			}
			else {
				rankCounter++;
			}
			prevFreq = freq;
		}
			
		ERcontex = utils.MapUtils.sortByValue(ERcontex);
		double ERrank = 0;
		rankCounter = 1;
		prevFreq = 0;
		bFound = false;
		for (String key:ERcontex.keySet()){
			int freq = ERcontex.get(key);
			if (bFound) {
				if (freq != prevFreq){
					if (rankCounter > 1)
						ERrank = (ERrank-1) + 1/(double)(rankCounter-1);
					break;
				} else {
					rankCounter++;
				}
			}
			if (key.equals(fourgram))
				bFound = true;
			if (freq != prevFreq){
				ERrank++;
				rankCounter = 1;
			}
			else {
				rankCounter++;
			}
			prevFreq = freq;
		}
		if(bFound)
			score = ERrank/ARrank;
		return score;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public FourgramsMutualRankRatio(File fourgramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fourgramsFile));
		m_prefixTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		m_sufixTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		m_mid1Trie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		m_mid2Trie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		String line = reader.readLine();
		int lineNum = 1;
//		int fourgramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_prefixTrie.put(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3], new Pair<Integer,Integer>(Integer.parseInt(tokens[4].split(" ")[0]),Integer.parseInt(tokens[4].split(" ")[14])));
			m_sufixTrie.put(tokens[3] + " " + tokens[2] + " " + tokens[1] + " " + tokens[0], new Pair<Integer,Integer>(Integer.parseInt(tokens[4].split(" ")[0]),Integer.parseInt(tokens[4].split(" ")[11])));
			m_mid1Trie.put(tokens[1] + " " + tokens[0] + " " + tokens[2] + " " + tokens[3], new Pair<Integer,Integer>(Integer.parseInt(tokens[4].split(" ")[0]),Integer.parseInt(tokens[4].split(" ")[13])));
			m_mid2Trie.put(tokens[2] + " " + tokens[1] + " " + tokens[0] + " " + tokens[3], new Pair<Integer,Integer>(Integer.parseInt(tokens[4].split(" ")[0]),Integer.parseInt(tokens[4].split(" ")[12])));
			line = reader.readLine();
			if (lineNum%10000==0)
				System.out.println("line: " + lineNum);
			if (lineNum > 1000000)
				break;
		}
		System.out.println("Finished loading trees");
	}
	
	private Trie<String, Pair<Integer,Integer>> m_prefixTrie = null;
	private Trie<String, Pair<Integer,Integer>> m_sufixTrie = null;
	private Trie<String, Pair<Integer,Integer>> m_mid1Trie = null;
	private Trie<String, Pair<Integer,Integer>> m_mid2Trie = null;
	static private String m_name="FourgramsMutualRankRatio";
	

}
