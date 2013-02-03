package lowFreq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import utils.MapUtils;
import utils.TargetTerm2Id;

public class GenerateNgrams {

	public void generate(String outputDir) throws CorruptIndexException, IOException, ParseException{
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(m_responsaMainDir+m_queriesFile));
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(m_indexName)));
		BufferedWriter writer = null;
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(m_responsaMainDir,targetType);
		HashMap<String, ArrayList<ScoreDoc>> addElements = targetRp.extractDocsByRepresentation();
		
		for(String query:addElements.keySet()){
			writer = new BufferedWriter(new FileWriter(outputDir + query + ".lowfreq"));
			LinkedList<Map<String, Integer>> ngramsList = new LinkedList<Map<String,Integer>>() ;
		
			ArrayList<ScoreDoc> docs = addElements.get(query);
			System.out.println("Query: " + query + " Docs num: " + docs.size());
			for (ScoreDoc doc : docs) {
				for (int n=2; n<5; n++) {
				Map<String, Integer> newNgrams = getNgrams(reader.document(doc.doc).get("TERM_VECTOR"),n);
				if(ngramsList.size() >= n-1){
					Map<String, Integer> ngrams = ngramsList.get(n-2);
					ngramsList.remove(n-2);
					ngrams = mergeMaps(ngrams, newNgrams);
					ngramsList.add(n-2, ngrams);
				}
				else
					ngramsList.add(newNgrams);
			}
			}
			for(Map<String, Integer> map:ngramsList)
				printNgramsMap(writer,map);
			writer.close();
		}
		
	}
		
	private void printNgramsMap(BufferedWriter writer, Map<String,Integer> map) throws IOException{
		Map<String, Integer> sortedMap = MapUtils.sortByValue(map);
		for(String key:sortedMap.keySet())
//			if(sortedMap.get(key)>1)
			writer.write(key + " " + sortedMap.get(key) + "\n");
	}
	
	
	private Map<String,Integer> getNgrams(String content, int n){
		HashMap<String,Integer> ngrams = new HashMap<String,Integer>();
		StringTokenizer tokens = new StringTokenizer(content);
		String[] tokenArr = new String[n];
		String ngram = "";
		for (int i=0; i<n-1; i++)
			if (tokens.hasMoreTokens()){
				tokenArr[i] = tokens.nextToken();
		}
		while(tokens.hasMoreTokens()){
			tokenArr[n-1] = tokens.nextToken();
			ngram = "";
			for (int i=0; i<n; i++)
				ngram = ngram+  " " + tokenArr[i];
			ngram = ngram.trim();
			if(ngrams.containsKey(ngram)){
				int freq = ngrams.get(ngram);
				ngrams.put(ngram, freq+1);
			}
			else
				ngrams.put(ngram,1);
			for (int i=0; i<n-1; i++){
				tokenArr[i] = tokenArr[i+1]; // step forward
			}
		}
		return ngrams;
	}
	
	private Map<String,Integer> mergeMaps(Map<String, Integer> ngrams, Map<String, Integer> newNgrams){
		Map<String, Integer> merged = new HashMap<String, Integer>(ngrams); 
		for (Map.Entry<String, Integer> entry : newNgrams.entrySet()) {
		   Integer y = merged.get(entry.getKey()); 
		   merged.put(entry.getKey(), entry.getValue() + (y == null ? 0 : y));
		}
		return merged;
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException{
		GenerateNgrams nGenerator = new GenerateNgrams();
		nGenerator.generate("C:\\LowFreq\\all\\");
	}
	private String m_responsaMainDir = "C:\\ResponsaSys\\";
	private String m_queriesFile = "input\\hozDescQuery.txt";
	private String m_indexName = "C:\\ResponsaSys\\indexes\\modernJewish";
}
