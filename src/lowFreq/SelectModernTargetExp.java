package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;

import fo.scorers.DiceScorer;
import fo.scorers.TfIdfScorer;

import utils.StringUtils;
import utils.TargetTerm2Id;

public class SelectModernTargetExp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws ParseException 
	 */
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
		
		// FO n-grams extraction - from modern Jewish index
		String targetTermFile = "/home/ir/liebesc/ResponsaSys/input/targetTerms_orig.txt";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, "/home/ir/liebesc/ResponsaSys/input/targetTerms_morph.txt", "/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly");
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		IndexReader modernReader = IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes/modernHebrew")));
		String outputDir = "/home/ir/liebesc/ResponsaSys/output/modernJewishOnlyLowFreq";
		File dir = new File(outputDir+"/Surface_Surface/");
		DiceScorer scorer = new DiceScorer();
		TfIdfScorer tfscorer = new TfIdfScorer();
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("NoOldModernAddDocumentInModernCorpusModernJewishMWE0_09.filter")) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace("_NoOldModernAddDocumentInModernCorpusModernJewishMWE0_09.filter","_ModernExp.txt")));
				String line = reader.readLine();
				writer.write("Term\tScore\tModernDice\tModernTfIdf\tAddInfo\tTargetCount\n");
				line = reader.readLine();
				String targetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
				int targetCount = modernReader.docFreq(new Term("TERM_VECTOR",StringUtils.fixQuateForSearch(targetTerm)));
				int counter = 0;
				while (line!=null && counter < 10){
					counter++;
					
					String tokens[] = line.split("\t");
					int jointCount = Integer.parseInt(tokens[5]);
					String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
					int expCount = modernReader.docFreq(new Term("TERM_VECTOR",candTerm));
					double score = scorer.score(0,targetCount,expCount,jointCount);
					double tfscore = tfscorer.score(0,targetCount,expCount,jointCount);
					int targetMorphCount = targetDocs.get(targetTerm).size();
					writer.write(candTerm+"\t"+tokens[1]+"\t"+score+"\t"+tfscore+"\t"+tokens[3]+"\t"+targetMorphCount+"\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
	}

}
