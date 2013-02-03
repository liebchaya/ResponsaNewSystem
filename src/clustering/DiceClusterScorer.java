package clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

import representation.FeatureRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;


import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

public class DiceClusterScorer {
	
	public DiceClusterScorer(String confDesc) throws CorruptIndexException, IOException, ParseException {
			FeatureRepresentation featureRep = new FeatureRepresentation("C:\\ResponsaNew\\",FeatureRepresentation.FeatureType.valueOf(confDesc.split("_")[1]));
			m_searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File(featureRep.getIndexNameByRepresentation()))));
			TargetTermRepresentation targetRep = new TargetTermRepresentation(TargetTermRepresentation.TargetTermType.valueOf(confDesc.split("_")[0]));
			m_targetDocs = targetRep.extractDocsByRepresentation();
	}
	
	public void initTargetTermData(Map<String, Double> input, String targetTerm){
		m_scoreMap = input;
		m_targetTerm = targetTerm;
	}
	
	public Double getDiceScore(Set<String> cluster) throws CorruptIndexException, IOException{
		Double score = 0.0;
		BooleanQuery orBq = new BooleanQuery(); 
		for(String str:cluster) {
			orBq.add(new TermQuery(new Term("TERM_VECTOR",str)), Occur.SHOULD);
		}
		TopDocs hits = m_searcher.search(orBq, 100000);
		int clusterCount = hits.totalHits;
		
		int targetCount = m_targetDocs.get(m_targetTerm).size();
		HashSet<Integer> hitsSet = new HashSet<Integer>();
		for (ScoreDoc sd:hits.scoreDocs) {
			hitsSet.add(sd.doc);
		}
		int jointCount = 0;
		for (ScoreDoc sd:m_targetDocs.get(m_targetTerm)) {
			if (hitsSet.contains(sd.doc))
					jointCount ++;
		}
		score = (double)jointCount*2 / (double)(clusterCount + targetCount);
		return score;
	}
	
	private Map<String, Double> m_scoreMap = null;
	private String m_targetTerm = null;
	private IndexSearcher m_searcher = null;
	private HashMap<String,ArrayList<ScoreDoc>> m_targetDocs = null;
	
	
}
