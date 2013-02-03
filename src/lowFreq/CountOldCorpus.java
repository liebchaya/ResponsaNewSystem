package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import obj.Pair;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import search.QueryGenerator;
import utils.StringUtils;
import utils.TargetTerm2Id;

public class CountOldCorpus {
	
	public static void main(String[] args) throws IOException, ParseException {
		HashMap<String,Integer> oldExp = new HashMap<String, Integer>();
		File dir = new File("C:\\lowFreq\\all\\");
		for (File f:dir.listFiles()) {
			if (f.getName().endsWith(".comp")) {
				BufferedReader fileReader = new BufferedReader(new FileReader(f));
				String line = fileReader.readLine();
				while (line!=null){
					String[] tokens = line.split("\t");
					String query = tokens[1];
					for (String t:StringUtils.convertStringToSet(query)) {
						oldExp.put(t, 0);
						for(String s:t.split(" "))
							oldExp.put(s, 0);
					}
					line = fileReader.readLine();
				}
			}
		}
		System.out.println("Finish reading directory");		
		IndexReader reader = IndexReader.open(FSDirectory.open(new File("C:\\ResponsaSys\\indexes\\responsaNgrams")));
		IndexSearcher searcher = new IndexSearcher(reader);
		BooleanQuery periodQ = new BooleanQuery();
		for (int i=1; i<4; i++)
			periodQ.add(new TermQuery(new Term("PERIOD",Integer.toString(i))),Occur.SHOULD);
		TopDocs docs = searcher.search(periodQ,100000);
		System.out.println("Quering...");
		int docCounter = 0;
		System.out.println("Docsnum: "+docs.totalHits);
		for (ScoreDoc doc:docs.scoreDocs) {
			docCounter++;
			TermFreqVector tfv = reader.getTermFreqVector(doc.doc,"TERM_VECTOR");
			for (String term : tfv.getTerms()) {
				if (oldExp.containsKey(term)){
					oldExp.put(term, reader.docFreq(new Term("TERM_VECTOR",term)));
				}
			}
			if (docCounter%100==0)
				System.out.println(docCounter + " docs");
		}
		System.out.println("Finish loading terms data");
		for (File f:dir.listFiles()) {
			if (f.getName().endsWith(".comp")) {
				BufferedReader fileReader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\lowFreq\\all\\" + f.getName().replace(".comp",".counts")));
				String line = fileReader.readLine();
				while (line!=null){
					String[] tokens = line.split("\t");
					String query = tokens[1];
					int count = 0;
					String[] expTokens = tokens[0].split(" ");
					int[] countsArr = new int[expTokens.length];
					for(int i=0;i<tokens[0].split(" ").length;i++)
						countsArr[i] = 0;
					
					for (String t:StringUtils.convertStringToSet(query)) {
						if (oldExp.containsKey(t))
							count+= oldExp.get(t);
						for (int i=0;i<t.split(" ").length;i++){
							countsArr[i] += oldExp.get(t.split(" ")[i]);
						}
						
					}
					String tokensString = "";
					for(int i=0; i<expTokens.length; i++)
						tokensString += expTokens[i]  + "\t" + countsArr[i] + "\t";
					writer.write(line + "\t" + count + "\t" + tokensString.trim() + "\n");
					line = fileReader.readLine();
				}
				fileReader.close();
				writer.close();
			}
		}
	}
}
