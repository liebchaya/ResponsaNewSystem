package fo.similarity;

import fo.scorers.StatScorer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import obj.Pair;
import obj.SortByWeightName;
import obj.Term;
import obj.WeightedTerm;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.FeatureRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import utils.TargetTerm2Id;

import ac.biu.nlp.nlp.general.SimpleBidirectionalMap;

public class FeatureVectorExtractor2Measures {
	
	public FeatureVectorExtractor2Measures(StatScorer scorer, StatScorer accumScorer,  FeatureRepresentation featureRp) throws CorruptIndexException, IOException{
		m_scorer = scorer;
		m_accumScorer = accumScorer;
		m_featureRp = featureRp;
	    m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp.getIndexNameByRepresentation())));
		loadFeatureDataFromIndex();
	}
	
	
	private void loadFeatureDataFromIndex() throws IOException{
		int featureId = 0;
		m_featureDesc2Id = new SimpleBidirectionalMap<String,Integer>();
		m_featureFreq = new TIntIntHashMap();
	    
	     /**
	      * @TODO Assign the correct value of m_totalElementsCount when used
	      * e.g. PMI first order measure
	      * 	m_totalElementsCount = reader.numDocs();
	      */	    
		TermEnum features = m_reader.terms();
		while (features.next()) {
			if (!features.term().field().equals("TERM_VECTOR"))
				continue;
			
			/**
		      * @TODO Add feature filter
		      */	
			if( m_featureRp.isRemoveMarkedFeatures() && features.term().text().contains("$"))
				continue;
			
			m_featureDesc2Id.put(features.term().text(), featureId);
			m_featureFreq.put(featureId, features.docFreq());
			featureId++;
		}
	}
	
	public void extractTargetTermVectors(HashMap<String,ArrayList<ScoreDoc>> targetDocs, TargetTermType targetType,  File outputDir) throws IOException{
		BufferedWriter writer = null;
		if(!outputDir.exists())
			outputDir.mkdir();
		File configDir = new File(outputDir.getAbsolutePath() + "/" + targetType.toString() + "_" + m_featureRp.getFeatureType());
		if(!configDir.exists())
			configDir.mkdir();
		for(String target:targetDocs.keySet()){
			if (targetDocs.get(target).size() == 0)
				continue;
			File targetFile = new File(configDir + "/" +TargetTerm2Id.getIntDesc(target)+ "_" + m_scorer.getName() + ".txt");
			if(targetFile.exists()) {
				System.out.println("Target term file " + targetFile.getName() + " already exist in " + configDir.getAbsolutePath());
				continue;
			}
			else {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),"UTF8"));
				for(WeightedTerm wt:getTargetTermVector(targetDocs.get(target)))
					if (!wt.getValue().isEmpty())
						writer.write(wt.getValue()+"\t" + wt.weight() + "\t" + Double.parseDouble(wt.getLemma()) + "\n");
				writer.close();
			}
		}
	}
	
	
	private List<WeightedTerm> getTargetTermVector(List<ScoreDoc> targetDocs) throws IOException{
		List<WeightedTerm> featureList = new LinkedList<WeightedTerm>();
		if (targetDocs.size() == 0)
			return featureList;
		
		TIntObjectHashMap<Pair<Long,Long>> countsMap = new TIntObjectHashMap<Pair<Long,Long>>();
		for (ScoreDoc doc : targetDocs) {
			TermFreqVector tfv = m_reader.getTermFreqVector(doc.doc,"TERM_VECTOR");
//			for (String term : tfv.getTerms()) {
//				term = term.replaceAll("[-+*/=](?![^(]*\\))","");
//				if (!m_featureDesc2Id.leftContains(term))
//					continue;
//
//				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
//					countsMap.put(m_featureDesc2Id.leftGet(term), 1);
//				else {
//					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
//					countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
//				}
//
//			}
			
			for (int i=0; i<tfv.size(); i++) {
				String term = tfv.getTerms()[i];
				term = term.replaceAll("[-+*/=](?![^(]*\\))","");
				if (!m_featureDesc2Id.leftContains(term))
					continue;

				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term))) {
						countsMap.put(m_featureDesc2Id.leftGet(term), new Pair<Long,Long>((long)1,(long)tfv.getTermFrequencies()[i]));
				}
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term)).key();
					Long accumScore = countsMap.get(m_featureDesc2Id.leftGet(term)).value();
					countsMap.put(m_featureDesc2Id.leftGet(term), new Pair<Long,Long>(score + 1, accumScore + tfv.getTermFrequencies()[i]));
				}

			}
			
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, targetDocs.size(), m_featureFreq.get(featureId), countsMap.get(featureId).key());
			double accumScore = m_accumScorer.score(m_totalElementsCount, targetDocs.size(), m_featureFreq.get(featureId), countsMap.get(featureId).value());

			if (score > 0) {
				featureList.add(new WeightedTerm(new Term(m_featureDesc2Id.rightGet(featureId),Double.toString(accumScore)),score));
			}
		}
		Collections.sort(featureList,new SortByWeightName());
		return featureList;
	}
	
	
	private SimpleBidirectionalMap<String,Integer> m_featureDesc2Id; 
	private StatScorer m_scorer;
	private StatScorer m_accumScorer;
	private FeatureRepresentation m_featureRp;
	private TIntIntHashMap m_featureFreq;
	private int m_totalElementsCount = 0;
	private IndexReader m_reader = null;
}
