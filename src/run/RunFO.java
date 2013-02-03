package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import ac.biu.nlp.nlp.general.configuration.ConfigurationException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFileDuplicateKeyException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;

import representation.FeatureRepresentation;
import representation.JewishFeatureRepresentation;
import representation.JewishTargetTermRepresentation;
import representation.MilaFeatureRepresentation;
import representation.MilaTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.TargetTermRepresentation.TargetTermType;

import utils.TargetTerm2Id;

import fo.scorers.DiceScorer;
import fo.scorers.StatScorer;
import fo.scorers.TfIdfScorer;
import fo.similarity.FeatureVectorExtractor;
import fo.similarity.FeatureVectorExtractor2Measures;

public class RunFO {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ConfigurationException 
	 * @throws ConfigurationFileDuplicateKeyException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, ParseException, ConfigurationFileDuplicateKeyException, ConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
		String responsaMainDir = params.get("responsa-dir");
//		TargetTerm2Id.loadTargetTerm2IdMapping(new File(responsaMainDir+"input\\origQuery.txt"));
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(responsaMainDir+"input/hozDescQuery.txt"));
		
		TargetTermType targetType = TargetTermType.valueOf(params.get("target-term-type"));
//		TargetTermRepresentation targetRp = new TargetTermRepresentation(responsaMainDir,targetType);
//		TargetTermRepresentation targetRp = new MilaTargetTermRepresentation(responsaMainDir,targetType);
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(responsaMainDir,targetType);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		String scorerClass =params.get("stat-scorer");
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
		
		FeatureType featureType = FeatureType.valueOf(params.get("feature-type"));
//		FeatureRepresentation featureRp = new FeatureRepresentation(responsaMainDir,featureType);
//		FeatureRepresentation featureRp = new MilaFeatureRepresentation(responsaMainDir,featureType);
		FeatureRepresentation featureRp = new JewishFeatureRepresentation(responsaMainDir,featureType);
//		// for accumulative measures
//		boolean bAccum = false;
//		if (scorerClass.contains("TfIdf"))
//			bAccum = true;
//		FeatureVectorExtractor vectorExtractor = new FeatureVectorExtractor(scorer,featureRp,bAccum);
		FeatureVectorExtractor2Measures vectorExtractor = new FeatureVectorExtractor2Measures(scorer,new TfIdfScorer(),featureRp);
		String outputDir = params.get("output-dir");
		vectorExtractor.extractTargetTermVectors(targetDocs, targetType, new File(outputDir));

	}

}
