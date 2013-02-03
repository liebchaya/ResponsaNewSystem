package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;

import obj.Pair;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;

public class BigramSeqOrder {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BigramSeqOrder bSO = new BigramSeqOrder(new File("C:\\responsa2.cnt"));
		BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\LowFreq\\all\\אם פונדקאית.lowfreq")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("C:\\LowFreq\\all\\אם פונדקאית.order")));
		String line = reader.readLine();
		while(line != null){
			if(line.split(" ").length>3)
				break;
			String [] tokens = line.split(" ");
			Pair<Integer,Integer> counts = bSO.countBigram(tokens[0] + " " +tokens[1]);
			writer.write(line + "\t" +  counts.key() + "\t" + counts.value() + "\n");
			line = reader.readLine();
		}
		reader.close();
		writer.close();

	}
	
	public Pair<Integer,Integer> countBigram(String bigram){
		SortedMap<String, Pair<Integer, Integer>> prefixMap = m_prefixTrie.prefixMap(bigram);
		if (prefixMap.size() > 1)
			System.out.println("Search error: " + bigram);
		int countsKey = 0;
		for (String key:prefixMap.keySet())
			countsKey = prefixMap.get(key).key();
		
		String revBigram = bigram.split(" ")[1] + " " + bigram.split(" ")[0];
		prefixMap = m_prefixTrie.prefixMap(revBigram);
		if (prefixMap.size() > 1)
			System.out.println("Search error: " + revBigram);
		int countsVal = 0;
		for (String key:prefixMap.keySet())
			countsVal = prefixMap.get(key).key();
		return new Pair<Integer, Integer>(countsKey,countsVal);
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public BigramSeqOrder(File bigramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(bigramsFile));
		m_prefixTrie = new PatriciaTrie<String, Pair<Integer,Integer>>(StringKeyAnalyzer.INSTANCE);
		String line = reader.readLine();
		int lineNum = 1;
		int bigramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_prefixTrie.put(tokens[0] + " " + tokens[1], new Pair<Integer,Integer>(Integer.parseInt(tokens[2].split(" ")[0]),Integer.parseInt(tokens[2].split(" ")[2])));
			line = reader.readLine();
			if (lineNum%10000==0)
				System.out.println("line: " + lineNum);
		}
		System.out.println("Finished loading trees");
	}
	
	private Trie<String, Pair<Integer,Integer>> m_prefixTrie = null;

}
