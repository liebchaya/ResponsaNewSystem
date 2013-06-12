package fo.similarity;

import fo.scorers.StatScorer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;

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

public class FeatureVectorExtractor {
	
	public FeatureVectorExtractor(StatScorer scorer, FeatureRepresentation featureRp) throws CorruptIndexException, IOException{
		m_scorer = scorer;
		m_featureRp = featureRp;
	    m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp.getIndexNameByRepresentation())));
		loadFeatureDataFromIndex();
	}
	
	public FeatureVectorExtractor(StatScorer scorer, FeatureRepresentation featureRp, boolean accum) throws CorruptIndexException, IOException{
		m_scorer = scorer;
		m_featureRp = featureRp;
	    m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp.getIndexNameByRepresentation())));
	    m_bAccum = accum;
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
			int freqCount = 0;
			if (!m_bAccum)
				freqCount = features.docFreq();
			else {
				while (m_reader.termDocs(features.term()).next())
					freqCount += m_reader.termDocs(features.term()).freq();	
			}
			m_featureFreq.put(featureId, freqCount);
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
			if (targetDocs.get(target).size() == 0 )//|| target==null)
				continue;
//			System.out.println(target);
			File targetFile = new File(configDir + "/" +TargetTerm2Id.getIntDesc(target)+ "_" + m_scorer.getName() + ".txt");
			if(targetFile.exists()) {
				System.out.println("Target term file " + targetFile.getName() + " already exist in " + configDir.getAbsolutePath());
				continue;
			}
			else {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),"UTF8"));
				for(WeightedTerm wt:getTargetTermVector(targetDocs.get(target)))
					if (!wt.getValue().isEmpty())
						writer.write(wt.getValue()+"\t" + wt.weight() + "\n");
				writer.close();
			}
		}
	}
	
	
	private List<WeightedTerm> getTargetTermVector(List<ScoreDoc> targetDocs) throws IOException{
		List<WeightedTerm> featureList = new LinkedList<WeightedTerm>();
		if (targetDocs.size() == 0)
			return featureList;
		
		TIntLongHashMap countsMap = new TIntLongHashMap();
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

				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
					if (!m_bAccum)
						countsMap.put(m_featureDesc2Id.leftGet(term), 1);
					else
						countsMap.put(m_featureDesc2Id.leftGet(term), tfv.getTermFrequencies()[i]);
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
					if (!m_bAccum)
						countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
					else
						countsMap.put(m_featureDesc2Id.leftGet(term), score + tfv.getTermFrequencies()[i]);
				}

			}
			
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, targetDocs.size(), m_featureFreq.get(featureId), countsMap.get(featureId));

			if (score > 0) {
				featureList.add(new WeightedTerm(new Term(m_featureDesc2Id.rightGet(featureId)),score));
			}
		}
		Collections.sort(featureList,new SortByWeightName());
		return featureList;
	}
	
	
	private SimpleBidirectionalMap<String,Integer> m_featureDesc2Id; 
	private StatScorer m_scorer;
	private FeatureRepresentation m_featureRp;
	private TIntIntHashMap m_featureFreq;
	private int m_totalElementsCount = 0;
	private IndexReader m_reader = null;
	private boolean m_bAccum = false;
}
