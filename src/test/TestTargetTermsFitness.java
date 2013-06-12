package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lowFreq.Morphology4TargetTermExp;
import lowFreq.Wikitionary;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import utils.TargetTerm2Id;

public class TestTargetTermsFitness {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		String targetTermFile = "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_orig.txt";
		String modernJewishIndex = "/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		// Expand target terms with morphology prefixes
		String expTargetTermFile = Morphology4TargetTermExp.generateMorphExpFile(targetTermFile, modernJewishIndex);
		
		// FO n-grams extraction - from modern Jewish index
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, expTargetTermFile, modernJewishIndex);
		HashMap<String, ArrayList<ScoreDoc>> morphTargetDocs = targetRp.extractDocsByRepresentation();
		
		TargetTermRepresentation oldTargetRp = new JewishTargetTermRepresentation(targetType,expTargetTermFile,  "/home/ir/liebesc/ResponsaSys/indexes/oldResponsa");
		HashMap<String, ArrayList<ScoreDoc>> oldTargetDocs = oldTargetRp.extractDocsByRepresentation();
		
		Wikitionary wiki = new Wikitionary(new File("/home/ir/liebesc/ResponsaSys/input/wikiRel"));
		BufferedReader reader = new BufferedReader(new FileReader(expTargetTermFile));
		String wikiMorphfile = expTargetTermFile.replace("_morph", "wiki_morph");
		BufferedWriter writer = new BufferedWriter(new FileWriter(wikiMorphfile));
		String line = reader.readLine();
		
		while(line!=null){
			String targetTerm = line.split("\t")[1];
			HashSet<String> wikiSet = wiki.getRelSet(targetTerm, true, 1);
			for(String rel:wikiSet){
				line = line + "\t" + rel;
			}
			writer.write(line + "\n");
			line = reader.readLine();
		}
		reader.close();
		writer.close();

//		String expTargetTermFile = "/home/ir/liebesc/ResponsaSys/input/targetTerms_modernExpBaselineStrict.txt";
		// FO n-grams extraction - from modern Jewish index
		targetType = TargetTermType.Surface;
		targetRp = new JewishTargetTermRepresentation(targetType, wikiMorphfile, modernJewishIndex);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		writer = new BufferedWriter(new FileWriter("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_wikiContrib.txt"));
		writer.write("Target term\tWiki contrib\tMorph only\tOld appearences\n");
		
		for(String term:targetDocs.keySet()){
			writer.write(term + "\t" + targetDocs.get(term).size()+ "\t" + morphTargetDocs.get(term).size() +"\t" + oldTargetDocs.get(term).size()+ "\n");
		}
		
		writer.close();
		

	}

}
