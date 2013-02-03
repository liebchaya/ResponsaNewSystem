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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


import representation.TargetTermRepresentation.TargetTermType;
import search.QueryGenerator;
import search.QueryGenerator.InputType;
import utils.TargetTerm2Id;

/**
 * 
 * @author Chaya Liebeskind
 * This class is responsible for document extraction by target term representation
 *
 */
public class JewishTargetTermRepresentation extends  TargetTermRepresentation{

	
	public JewishTargetTermRepresentation(String responsaMainDir,
			TargetTermType targetType) {
		super(responsaMainDir, targetType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the type of the target term representation, query with the suitable input formatted file and the
	 * corresponding index
	 * @param type
	 * @return HashMap<String,ArrayList<ScoreDoc>> A set of target terms with their extracted documents
	 * @throws IOException
	 * @throws ParseException
	 */
	@Override
	public HashMap<String,ArrayList<ScoreDoc>> extractDocsByRepresentation() throws IOException, ParseException{
		String indexName = null, inputFileName = null;
		indexName = "modernJewishOnly";
		m_qg.setType(InputType.Query);
		inputFileName = "hozOrigQueryAll.txt";
		
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
}
