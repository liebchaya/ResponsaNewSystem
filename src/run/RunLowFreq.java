package run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import lowFreq.FilterGeneratedData;
import lowFreq.Morphology4TargetTermExp;
import lowFreq.NgramsDataGeneration;

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
import fo.scorers.FreqScorer;
import fo.scorers.StatScorer;
import fo.scorers.TfIdfScorer;
import fo.similarity.FeatureVectorExtractor;
import fo.similarity.FeatureVectorExtractor2Measures;

public class RunLowFreq {

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
		String targetTermFile = params.get("target-terms-file");
		String modernJewishIndex = params.get("modern-jewish-index");
		String outputDir = params.get("output-dir");
		
		TargetTerm2Id.loadTargetTerm2IdMapping(new File("/home/ir/liebesc/ResponsaSys/input/step3New30All_orig.txt"));
//		TargetTerm2Id.loadTargetTerm2IdMapping(new File("C:/ResponsaSys/input/targetTermsAll_orig.txt"));
		// Expand target terms with morphology prefixes
		String expTargetTermFile = Morphology4TargetTermExp.generateMorphExpFile("/home/ir/liebesc/ResponsaSys/input/step3New30All_orig.txt", modernJewishIndex);

//		String expTargetTermFile = "/home/ir/liebesc/ResponsaSys/input/tries/targetTerms_oldExpShmulik100MWEBaseline.txt";
		// FO n-grams extraction - from modern Jewish index
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, expTargetTermFile, modernJewishIndex);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		String scorerClass =params.get("stat-scorer");
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
		
		FeatureType featureType = FeatureType.valueOf(params.get("feature-type"));
		FeatureRepresentation featureRp = new JewishFeatureRepresentation(featureType, modernJewishIndex);
		// for accumulative measures
		boolean bAccum = false;
		if (scorerClass.contains("TfIdf"))
			bAccum = true;
		FeatureVectorExtractor vectorExtractor = new FeatureVectorExtractor(scorer,featureRp,bAccum);
//		FeatureVectorExtractor2Measures vectorExtractor = new FeatureVectorExtractor2Measures(scorer,new FreqScorer(),featureRp);
		vectorExtractor.extractTargetTermVectors(targetDocs, targetType, new File(outputDir));
	
		// Classify to modern or old
		// Check modern possible contribution
		// MWE scoring
/*		String modernIndex = params.get("modern-index");
		String oldIndex = params.get("old-index");
		NgramsDataGeneration dataGenerator = new NgramsDataGeneration(oldIndex, modernJewishIndex, modernIndex);
		String oldNgramsFileName = params.get("old-ngarms-file");
		String modernJewishNgramsFileName = params.get("modern-jewish-ngram-file");
		dataGenerator.generateDataFiles(outputDir, "_Dice.txt", expTargetTermFile, oldNgramsFileName, modernJewishNgramsFileName, 4);
	
		// Filter output files
		FilterGeneratedData modernfilter = new FilterGeneratedData(false,true,true,true,0.01,-1,2);
//		FilterGeneratedData modernfilter = new FilterGeneratedData(false,true,true,false,0.01,-1,2);
//		FilterGeneratedData modernfilter = new FilterGeneratedData(false,true,true,true);
		modernfilter.filterFiles(outputDir); 
		
//		FilterGeneratedData oldfilter = new FilterGeneratedData(true,false,false,false,-1,0.01,2);
//		FilterGeneratedData oldfilter = new FilterGeneratedData(true,false,false,false);
//		oldfilter.filterFiles(outputDir);
*/

	}

}
