package so;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;


import representation.FeatureRepresentation;
import representation.SORelatedTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.SORelatedTermRepresentation.SORelatedTermType;
import representation.TargetTermRepresentation.TargetTermType;
import utils.TargetTerm2Id;
import fo.scorers.StatScorer;

/**
 * This class is responsible for the extraction of the final matrices for Second-order Similarity computation
 * Including vectors' truncating 
 * 
 * @author HZ
 *
 */
public class GenerateFeatureVectorMatrices {
	
	
	
	public GenerateFeatureVectorMatrices(String dirName) {
		super();
		m_mainDirName = dirName;
	}
	
	public void truncateFeatureVectorMatrix(SOFeatureVectorInfo info) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		String scorerClass = "fo.scorers."+info.getStatScorerName()+"Scorer";
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
		SOFeatureVectorExtractor vectorExtractor = new SOFeatureVectorExtractor(scorer);
		vectorExtractor.truncateTargetVectors(info.getTrunactedVectorSize(), new File(info.getVectorsDir()));
		vectorExtractor.truncateVectors(info.getTrunactedVectorSize(), new File(info.getVectorsDir()));
	}

	/**
	 * Extracting A feature vector matrix, containing all possible candidate related terms
	 * @return SOFeatureVectorInfo - All the necessary information for extraction a suitable B matrix
	 * for similarity computation, where B is the Target term feature vector matrix
	 * @throws IOException
	 */
	
	public SOFeatureVectorInfo runMatrixAExtraction(StatScorer statScorer, FeatureType featType, SORelatedTermType soRelatedType, int maxVecLenght, int filesNum,  String outputDirName ) throws IOException {
		SOFeatureVectorInfo soInfo = null;
	/**/	StatScorer scorer = statScorer; //new DiceScorer();
	/**/	FeatureType featureType = featType; //FeatureType.Surface;
	/**/	SORelatedTermType soRelatedTermType = soRelatedType; //SORelatedTermType.All;
	FeatureRepresentation featureRp = new FeatureRepresentation(m_mainDirName,featureType);
	SOFeatureVectorExtractor vectorExtractor = new SOFeatureVectorExtractor(scorer,featureRp);
	SORelatedTermRepresentation soRelatedRp = new SORelatedTermRepresentation(m_mainDirName,soRelatedTermType);
//	/**/	String outputDir = "C:\\NewStatisticsFolder\\";
//	/**/	String outputDir = "/home/ir/liebesc/SO-Index/NewStatisticsFolder";
	String outputDir = outputDirName;
	/**/	int maxVectorLength = maxVecLenght; //1000;
	int relatedTermsNum = vectorExtractor.extractSORelatedTermVectors(soRelatedRp,new File(outputDir), filesNum);
//	// vectors were extracted
	File configDir = null;
	if (relatedTermsNum > 0) {
		configDir = new File(outputDir + "/" + featureType.toString() + "_" + soRelatedTermType.toString());
		vectorExtractor.truncateVectors(maxVectorLength, configDir);
	}
	soInfo = new SOFeatureVectorInfo(featureType,soRelatedTermType,scorer.getName(),maxVectorLength, filesNum, configDir.getAbsolutePath());
	soInfo.printFeatureVectorInfo2File();
	return soInfo;
	}
	
	/**
	 * Extracting B feature vector matrix, containing the desired target terms
	 * @param SOFeatureVectorInfo - All the necessary information for extraction a suitable B matrix
	 * for similarity computation, where B is the Target term feature vector matrix
	 * @param isAllTargetRep - Whether to include all term representations in one run
	 * @throws IOException
	 * @throws ParseException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void runMatrixBExtraction(SOFeatureVectorInfo info, TargetTermType targetTType, boolean isAllTargetRep) throws IOException, ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException{
//		/**/	String responsaMainDir = "/home/ir/liebesc/ResponsaNew";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(m_mainDirName+"/input/origQuery.txt"));
		TargetTermType targetType =targetTType;
		TargetTermRepresentation targetRp = new TargetTermRepresentation(m_mainDirName,targetType);

		HashMap<String, ArrayList<ScoreDoc>> allTargetDocs;
		if(!isAllTargetRep)
			allTargetDocs = targetRp.extractDocsByRepresentation();
		else {
			allTargetDocs = new HashMap<String, ArrayList<ScoreDoc>>();
			
			HashMap<String, ArrayList<ScoreDoc>> targetDocs;
			targetRp.setTargetTermType(TargetTermType.All);
			targetDocs = targetRp.extractDocsByRepresentation();
			for(String q:targetDocs.keySet()){
				allTargetDocs.put(q+"_"+TargetTermType.All.toString(), targetDocs.get(q));
			}
			targetDocs.clear();
			
			targetRp.setTargetTermType(TargetTermType.AllBest);
			targetDocs = targetRp.extractDocsByRepresentation();
			for(String q:targetDocs.keySet()){
				allTargetDocs.put(q+"_"+TargetTermType.AllBest.toString(), targetDocs.get(q));
			}
			targetDocs.clear();
			
			targetRp.setTargetTermType(TargetTermType.Best);
			targetDocs = targetRp.extractDocsByRepresentation();
			for(String q:targetDocs.keySet()){
				allTargetDocs.put(q+"_"+TargetTermType.Best.toString(), targetDocs.get(q));
			}
			targetDocs.clear();
			
			targetRp.setTargetTermType(TargetTermType.Surface);
			targetDocs = targetRp.extractDocsByRepresentation();
			for(String q:targetDocs.keySet()){
				allTargetDocs.put(q+"_"+TargetTermType.Surface, targetDocs.get(q));
			}
			targetDocs.clear();
		}
		
		String scorerClass = "fo.scorers."+info.getStatScorerName()+"Scorer";
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
    	FeatureType featureType = info.getFeatureType();
		FeatureRepresentation featureRp = new FeatureRepresentation(m_mainDirName,featureType);
		SOFeatureVectorExtractor vectorExtractor = new SOFeatureVectorExtractor(scorer,featureRp);
		String outputDir = info.getVectorsDir();
		int relatedTermsNum = vectorExtractor.extractTargetTermVectorsForSO(allTargetDocs, targetType, new File(outputDir));
		if (relatedTermsNum > 0) {
			vectorExtractor.truncateTargetVectors(info.getTrunactedVectorSize(), new File(info.getVectorsDir()));
		}

	}
	
	private String m_mainDirName = null;
	
	
}
