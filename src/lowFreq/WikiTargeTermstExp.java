package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Pair;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;

import fo.scorers.DiceScorer;
import fo.scorers.TfIdfScorer;

import utils.StringUtils;
import utils.TargetTerm2Id;

public class WikiTargeTermstExp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws ParseException 
	 */
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
		
		HashSet<String> wikiSet = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_wikiContrib.txt"));
		String line = reader.readLine();
		line = reader.readLine();
		while (line!= null){
			String[] tokens = line.split("\t");
			int wikiCont = Integer.parseInt(tokens[1]);
			int morphOnly = Integer.parseInt(tokens[2]);
			int addCount = wikiCont-morphOnly;
			if( ((addCount < 10*morphOnly) && (addCount>0))||((morphOnly == 0) && (wikiCont > 0)))
				wikiSet.add(tokens[0]);
			line = reader.readLine();
		}
		reader.close();
		
		System.out.println("Number of wiki expanded terms: " + wikiSet.size());
		
		// read the suitable input file
		HashMap<Integer,String> queries = new HashMap<Integer,String>();
		reader = new BufferedReader(new FileReader("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_orig.txt"));
		line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			queries.put(Integer.parseInt(line.substring(0,index)),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		
		String targetTermFile = "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_orig.txt";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ir/liebesc/ResponsaSys/input/targetTerms_allWithWiki.txt"));
		Wikitionary wiki = new Wikitionary(new File("/home/ir/liebesc/ResponsaSys/input/wikiRel"));
		for(int queryId:queries.keySet()) {
			String queryLine = queryId + "\t" + queries.get(queryId);
			String targetTerm = TargetTerm2Id.getStrDesc(queryId);
				if (wikiSet.contains(targetTerm)) {
					HashSet<String> wikiRel = wiki.getRelSet(targetTerm, true, 1);
					if(wikiRel.size()>0){
						for(String rel:wikiRel)
							queryLine = queryLine + "\t" + rel;
					}
					writer.write(queryLine+"\n");
				}
		}
		writer.close();
	}

}
