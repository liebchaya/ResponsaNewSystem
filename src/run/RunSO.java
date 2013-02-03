package run;


import java.io.File;

import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import fo.scorers.StatScorer;
import representation.FeatureRepresentation.FeatureType;
import representation.SORelatedTermRepresentation.SORelatedTermType;
import representation.TargetTermRepresentation.TargetTermType;
import so.GenerateFeatureVectorMatrices;
import so.SOFeatureVectorInfo;
import so.similarity.SOSimilarityCalculator;

public class RunSO {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		boolean incVecExtraction = params.getBoolean("include_vector_extraction");
		boolean incVecTruncating = params.getBoolean("include_vector_truncating");
		boolean incAllTargetRepresentations = params.getBoolean("include_all_target_representations");
		
		
//		String responsaMainDir = "C:\\ResponsaNew\\";
//		String responsaMainDir = "/home/ir/liebesc/ResponsaNew";
		String responsaMainDir = params.get("responsa-dir");
		GenerateFeatureVectorMatrices ex = new GenerateFeatureVectorMatrices(responsaMainDir);
		TargetTermType targetType = TargetTermType.valueOf(params.get("target-term-type"));
		
		if(incVecExtraction) {
		
			String scorerClass =params.get("stat-scorer");
			Class<?> cls = Class.forName(scorerClass);
			StatScorer scorer  = (StatScorer) cls.newInstance();
			
			FeatureType featureType = FeatureType.valueOf(params.get("feature-type"));
			SORelatedTermType soRelatedTermType = SORelatedTermType.valueOf(params.get("related-term-type"));
			
			String outputMainDir = params.get("output-dir");
			int maxVectorLength = params.getInt("max-vector-length");
			int filesNum = params.getInt("split-file-num");
			
			SOFeatureVectorInfo info = ex.runMatrixAExtraction(scorer,featureType, soRelatedTermType,maxVectorLength, filesNum, outputMainDir);
			ex.runMatrixBExtraction(info,targetType,incAllTargetRepresentations);
			SOSimilarityCalculator sc = new SOSimilarityCalculator(info);
			sc.calculateSoSmilarity();
		}
		
		else {
			File infoFile = params.getFile("information-file");
			SOFeatureVectorInfo info = new SOFeatureVectorInfo(infoFile);
			
			if (incVecTruncating){
				ex.truncateFeatureVectorMatrix(info);
			}
			else
				ex.runMatrixBExtraction(info, targetType, incAllTargetRepresentations);
			SOSimilarityCalculator sc = new SOSimilarityCalculator(info);
			sc.calculateSoSmilarity();
		}

	}

}
