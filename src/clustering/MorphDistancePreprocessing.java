package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import morphology.MorphLemmatizer;

import obj.WeightedTerm;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import utils.StringUtils;


public class MorphDistancePreprocessing {
	
	/**
	 * 
	 * @param termsDir
	 * @param indexDir	assign null in case of Unigrams
	 * @param queryField
	 * @param morphType 1 = allLemmas, 2 = best lemma
	 * @param termIndex		the index of the term in the splitted line
	 * @param scoreIndex	the index of the score in the splitted line - might be -1
	 * @param topNum		number of terms to read from the input file
	 * @param scoreThreshold threshold for the loaded terms
	 */
	public MorphDistancePreprocessing(String termsDir, File indexDir, String queryField, int morphType,
										int termIndex, int scoreIndex, int topNum, double scoreThreshold, boolean skipFirst) {	
		m_termsDir = termsDir;
		m_indexDir = indexDir;
		m_queryField = queryField;
		m_morphType = morphType;
		m_termIndex = termIndex;
		m_scoreIndex = scoreIndex;
		m_topNum = topNum;
		m_scoreThreshold = scoreThreshold; 
		m_skipFirst = skipFirst;
	}
	
	/**
	 * Loads the input file and pre-process the data (for MorphDist)
	 * @param queryTerm
	 * The score field is used for saving the judgment when necessary 
	 * @return				Map<String,Double>
	 * @throws MorphDistancePrePException
	 */
	public Map<String,Double> loadDataFile(String queryTerm, String confName) throws MorphDistancePrePException{
		m_unigramsFreq = new HashMap<String, Integer>();
		m_dataMap = new HashMap<String, Set<String>>();
		m_term2Unigram = new HashMap<String, String>();
		
		Map<String,Double> inputTerms = new HashMap<String,Double>();
		try {
			File file = new File(m_termsDir + "\\" +queryTerm);
			if (!file.exists()){
				System.out.println("Missing file: "+ file.getAbsolutePath());
				return null;
			}
//			String encoding = FileUtils.getFileEncoding(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
	    	String line = reader.readLine(); //skip the first line
	    	if(m_skipFirst)
	    		line = reader.readLine();
	    	int counter = 0;
	    	
	    	String sep = "_";
	    	if (!queryTerm.contains(sep))
	    		sep = ".";
	    	Set<String> AllLemmas = MorphLemmatizer.getAllPossibleLemmas(queryTerm.substring(0,queryTerm.indexOf(sep)).trim());
	    	AllLemmas.add(queryTerm.substring(0,queryTerm.indexOf(sep)).trim());
	    	Set<String> BestLemma = MorphLemmatizer.getMostProbableLemma(queryTerm.substring(0,queryTerm.indexOf(sep)).trim());
	    	BestLemma.add(queryTerm.substring(0,queryTerm.indexOf(sep)).trim());
	    	
	    	while(line != null && counter <= m_topNum) {
	    		if(line.trim().equals("")){
	    			line = reader.readLine();
	    			continue;
	    		}
	    		String value = null;
	    		String orig = line.split("\t")[m_termIndex];
	    		orig = orig.replaceAll("[-+*/=](?![^(]*\\))","");
	    		
	    		if(orig.trim().length() < 2 || StringUtils.checkIfNumber(orig.trim())){
	    			line = reader.readLine();
	    			continue;
	    		}
	    		
	    		if(confName.split("_")[0].equals("Surface")){
		    		if(orig.trim().equals(queryTerm.substring(0,queryTerm.indexOf(sep)).trim())){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
	    		else if(confName.split("_")[0].equals("All")){
		    		if(AllLemmas.contains(orig.trim())){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
	    		else if(confName.split("_")[0].equals("Best")){
		    		if(BestLemma.contains(orig.trim())){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
	    		System.out.println(line);
	    		counter++;
	    		double score = 0.0;
	    		if(m_scoreIndex>=0){
	    			score = Double.parseDouble(line.split("\t")[m_scoreIndex]);
	    			if(score < m_scoreThreshold){
	    				line = reader.readLine();
	    				continue;
	    			}
	    		}
//	    		String qTerm = m_queryTerm.substring(0,m_queryTerm.indexOf("."));
//	    		if(orig.contains(qTerm))
//					value = qTerm;
//	    		else {
	    		value = StringUtils.fixAnnotatedTerm(orig);
	    		if (orig.split(" ").length>1)
	    			value = getLessFrequentUnigram(orig);
//	    		}
	    		if (!m_term2Unigram.containsKey(orig))
	    			m_term2Unigram.put(orig,value);
	    		if(!m_dataMap.containsKey(value)) {
	    			Set<String> lemmas = null;
	    			if (m_morphType == 1)
	    			 	lemmas = MorphLemmatizer.getAllPossibleLemmas(value);
	    			else if (m_morphType == 2)
	    				lemmas = MorphLemmatizer.getMostProbableLemma(value);
	    			m_dataMap.put(value, lemmas);
	    		}
	    		inputTerms.put(orig,score);
	    		line = reader.readLine();
	    	}
		} catch(Exception e){
			throw new MorphDistancePrePException("IO Exception - reading input file "+ e);
		}
		
		return inputTerms;
	}
	
	/**
	 * Loads the input file and pre-process the data (for MorphDist)
	 * @param LinkedList<WeightedTerm> list of terms
	 * @param insertScore whether to save scores
	 * The score field is used for saving the judgment when necessary 
	 * @return				Map<String,Double>
	 * @throws MorphDistancePrePException
	 */
	public Map<String,Double> loadDataFile(LinkedList<WeightedTerm> termsList, boolean insertScore) throws MorphDistancePrePException{
		m_unigramsFreq = new HashMap<String, Integer>();
		m_dataMap = new HashMap<String, Set<String>>();
		m_term2Unigram = new HashMap<String, String>();
		
		Map<String,Double> inputTerms = new HashMap<String,Double>();
		try {
	    	for(WeightedTerm wt:termsList) {
	    		String value = null;
	    		String orig = wt.getValue();
	    		double score = 0.0;
	    		if(insertScore){
	    			score = wt.weight();
	    		}
	    		value = StringUtils.fixAnnotatedTerm(orig);
	    		if (orig.split(" ").length>1)
	    			value = getLessFrequentUnigram(orig);
	    		if (!m_term2Unigram.containsKey(orig))
	    			m_term2Unigram.put(orig,value);
	    		if(!m_dataMap.containsKey(value)) {
	    			Set<String> lemmas = null;
	    			if (m_morphType == 1)
	    			 	lemmas = MorphLemmatizer.getAllPossibleLemmas(value);
	    			else if (m_morphType == 2)
	    				lemmas = MorphLemmatizer.getMostProbableLemma(value);
	    			m_dataMap.put(value, lemmas);
	    		}
	    		inputTerms.put(orig,score);
	    	}
		} catch(Exception e){
			throw new MorphDistancePrePException("IO Exception - reading input file "+ e);
		}
		
		return inputTerms;
	}
	
	/**
	 * Gets the lemmas list for a specific term
	 * @param term
	 * @return	Set<String>
	 * @throws MorphDistancePrePException
	 */
	public Set<String> getLemmasList(String term) throws MorphDistancePrePException {
		if (!m_term2Unigram.containsKey(term))
			throw new MorphDistancePrePException("Unable to find term "+ term + " data");
		String value = m_term2Unigram.get(term);
		if (!m_dataMap.containsKey(value))
			throw new MorphDistancePrePException("Unable to find value "+ value + " data");
		return m_dataMap.get(value);
	}
	
	/**
	 * Gets the least frequent unigram from a multi-word expression
	 * @param multiwordTerm
	 * @return	String
	 * @throws MorphDistancePrePException
	 */
	private String getLessFrequentUnigram(String multiwordTerm) throws MorphDistancePrePException{
		TopDocs hits = null;
		List<String> list = new LinkedList<String>();
		String minS = null;
		if(m_indexDir == null)
			throw new MorphDistancePrePException("No index file was given. This method shouldn't be called");
		
		try {
			int minCount=100000;
			IndexSearcher searcher = new IndexSearcher(FSDirectory.open(m_indexDir));
			TermQuery query;
			for(String s:multiwordTerm.split(" ")) {
				int sCount = -1;
				//String fixed = StringUtils.fixQuateForSearch(StringUtils.fixAnnotatedTerm(s));
				String fixed = StringUtils.fixAnnotatedTerm(s);
				if(m_unigramsFreq.containsKey(fixed))
					sCount = m_unigramsFreq.get(fixed);
				else {
					list.clear();
					list.add(fixed);
					query = new TermQuery(new Term(m_queryField,fixed));
					//query = qb.orQuery(list);
					hits = searcher.search(query,1);
					sCount = hits.totalHits;
					m_unigramsFreq.put(fixed, sCount);
				}
				if(sCount <= minCount) {
					minCount = sCount;
					minS = fixed;
				}
			}
		}
		catch(Exception e) {
			throw new MorphDistancePrePException("Problem with IR.new_search");
		}
		return minS;
	}
	
	
	
	private String m_termsDir = null;
	private File m_indexDir = null;
	private String m_queryField = null;
	private Map<String,Set<String>> m_dataMap = null;
	private Map<String,String> m_term2Unigram = null;
	protected HashMap<String,Integer> m_unigramsFreq = null;
	private MorphLemmatizer m_lemmatizer = null;
	private int m_morphType = 1;
	
	private int m_termIndex;
	private int m_scoreIndex;
	private int m_topNum;
	private double m_scoreThreshold; 
	private boolean m_skipFirst = false;
}
