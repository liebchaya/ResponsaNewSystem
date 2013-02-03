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

public class TrigramsMutualRankRatio implements MWEScorer {

	public static void main(String[] args) throws IOException{
		TrigramsMutualRankRatio tMRR = new TrigramsMutualRankRatio(new File("C:\\responsa3.cnt"));
		System.out.println("צער בעלי חיים " + tMRR.score("צער בעלי חיים"));
		System.out.println("כיבוד אב ואם " + tMRR.score("כיבוד אב ואם"));
		System.out.println("של צער בעלי " + tMRR.score("של צער בעלי"));
		System.out.println("משום צער בעלי " + tMRR.score("משום צער בעלי"));
	}
	
	public String getName() {
		return m_name;
	}
	
	//mutualRankRatio
	public double score(String trigram){
		double score = Math.pow(rankRatio(trigram, 0)*rankRatio(trigram, 1)*rankRatio(trigram, 2),0.333);
		return score;
	}
	
	public boolean exist(String trigram){ 
		if (m_prefixTrie.containsKey(trigram))
			return true;
		return false;
		
	}
	
	private double rankRatio(String trigram, int pos){
//		if (pos == 2)
//			System.out.println("jkjj");
		double score = 0.0;
		Map<String, Integer> ERcontex = new LinkedHashMap<String, Integer>();
		Map<String, Integer> ARcontex = new HashMap<String, Integer>();
		String prefix = trigram.split(" ")[pos]+ " ";
		for(String entry:m_prefixTrie.prefixMap(prefix).keySet()){
			ARcontex.put(entry, m_prefixTrie.get(entry).key());
			ERcontex.put(entry, m_prefixTrie.get(entry).value());
		}
		for(String entry:m_sufixTrie.prefixMap(prefix).keySet()){
			String [] trigramSplit = entry.split(" ");
			String revTrigram = trigramSplit[2]+ " " + trigramSplit[1]+ " " + trigramSplit[0];
			ARcontex.put(revTrigram, m_sufixTrie.get(entry).key());
			ERcontex.put(revTrigram, m_sufixTrie.get(entry).value());
		}
		for(String entry:m_midTrie.prefixMap(prefix).keySet()){
			String [] trigramSplit = entry.split(" ");
			String revTrigram = trigramSplit[1]+ " " + trigramSplit[0]+ " " + trigramSplit[2];
			ARcontex.put(revTrigram, m_midTrie.get(entry).key());
			ERcontex.put(revTrigram, m_midTrie.get(entry).value());
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
			if (key.equals(trigram))
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
			if (key.equals(trigram))
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
	public TrigramsMutualRankRatio(File trigramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(trigramsFile));
		m_prefixTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		m_sufixTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		m_midTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		String line = reader.readLine();
		int lineNum = 1;
//		int trigramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_prefixTrie.put(tokens[0] + " " + tokens[1] + " " + tokens[2], new Pair<Integer,Integer>(Integer.parseInt(tokens[3].split(" ")[0]),Integer.parseInt(tokens[3].split(" ")[6])));
			m_sufixTrie.put(tokens[2] + " " + tokens[1] + " " + tokens[0], new Pair<Integer,Integer>(Integer.parseInt(tokens[3].split(" ")[0]),Integer.parseInt(tokens[3].split(" ")[4])));
			m_midTrie.put(tokens[1] + " " + tokens[0] + " " + tokens[2], new Pair<Integer,Integer>(Integer.parseInt(tokens[3].split(" ")[0]),Integer.parseInt(tokens[3].split(" ")[5])));
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
	private Trie<String, Pair<Integer,Integer>> m_midTrie = null;
	static private String m_name="TrigramsMutualRankRatio";
	

}
