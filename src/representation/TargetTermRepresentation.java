package representation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import obj.Pair;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


import search.QueryGenerator;
import search.QueryGenerator.InputType;
import utils.TargetTerm2Id;

/**
 * 
 * @author Chaya Liebeskind
 * This class is responsible for document extraction by target term representation
 *
 */
public class TargetTermRepresentation {

	
	/**
	 * Possible target term representations
	 */
	public enum TargetTermType{
		Surface,
		Best,
		All,
		AllBest
	}
	
	public TargetTermRepresentation(String responsaMainDir, TargetTermType targetType){
		m_qg = new QueryGenerator(new StandardAnalyzer(Version.LUCENE_31),"TERM_VECTOR");
		m_indexDir = responsaMainDir+"/indexes/";
		m_inputDir = responsaMainDir+"/input/";
		m_type = targetType;
	}
	
	public TargetTermRepresentation(TargetTermType targetType){
		m_qg = new QueryGenerator(new StandardAnalyzer(Version.LUCENE_31),"TERM_VECTOR");
		m_type = targetType;
	}
	
	/**
	 * Get the type of the target term representation, query with the suitable input formatted file and the
	 * corresponding index
	 * @param type
	 * @return HashMap<String,ArrayList<ScoreDoc>> A set of target terms with their extracted documents
	 * @throws IOException
	 * @throws ParseException
	 */
	public HashMap<String,ArrayList<ScoreDoc>> extractDocsByRepresentation() throws IOException, ParseException{
		String indexName = null, inputFileName = null;
		if (m_type == TargetTermType.Surface) {
			indexName = "unigIndex";
			m_qg.setType(InputType.Query);
			inputFileName = "origQuery.txt";
		}
		else if (m_type == TargetTermType.Best) {
			indexName = "indexTagger";
			m_qg.setType(InputType.Raw);
			inputFileName = "bestLemmaQueryTagger.txt";
		}
		else if (m_type == TargetTermType.All) {
			indexName = "index0";
			m_qg.setType(InputType.Raw);
			inputFileName = "allLemmaQuery.txt";
		}
		else if (m_type == TargetTermType.AllBest) {
			indexName = "indexTagger";
			m_qg.setType(InputType.Raw);
			inputFileName = "allLemmaQuery.txt";
		}
		
		// read the suitable input file
		LinkedList<Pair<String,String>> queries = new LinkedList<Pair<String,String>>();
		BufferedReader reader = new BufferedReader(new FileReader(m_inputDir+inputFileName));
		String line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			queries.add(new Pair<String,String>(line.substring(0,index),line.substring(index+1)));
			line = reader.readLine();
		}
		reader.close();
		
		// search for the queries in the index
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File(m_indexDir+indexName))));
		HashMap<String,ArrayList<ScoreDoc>> termDocs = new HashMap<String, ArrayList<ScoreDoc>>();
		for(Pair<String,String> term:queries){
			Query q = m_qg.generate(term.value());
			termDocs.put(TargetTerm2Id.getStrDesc(Integer.parseInt(term.key())), new ArrayList<ScoreDoc>(Arrays.asList(searcher.search(q,1000).scoreDocs)));
		}
		return termDocs;
		
	}
	
	public TargetTermType getTargetTermType(){
		return m_type;
	}
	
	public void setTargetTermType(TargetTermType type){
		m_type = type;
	}
	
	protected QueryGenerator m_qg = null;
	protected String m_indexDir = "C:\\ResponsaNew\\indexes\\";
	protected String m_inputDir = "C:\\ResponsaNew\\input\\";
	protected TargetTermType m_type = TargetTermType.Surface;
}
