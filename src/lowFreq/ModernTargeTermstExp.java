package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Pair;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
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

public class ModernTargeTermstExp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws ParseException 
	 */
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
		
/*		HashSet<String> wikiSet = new HashSet<String>();
		HashSet<String> modernQeSet = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_wikiContrib.txt"));
		String line = reader.readLine();
		line = reader.readLine();
		while (line!= null){
			String[] tokens = line.split("\t");
			int wikiCont = Integer.parseInt(tokens[1]);
			int morphOnly = Integer.parseInt(tokens[2]);
			int addCount = wikiCont-morphOnly;
			if( (addCount < 10*morphOnly) && (addCount>0))
				wikiSet.add(tokens[0]);
			line = reader.readLine();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader("/home/ir/liebesc/ResponsaSys/input/targetTermsAllAnal.txt"));
		line = reader.readLine();
		while (line!= null){
			String[] tokens = line.split("\t");
			int oldCont = Integer.parseInt(tokens[2]);
			if( oldCont < 20)
				modernQeSet.add(tokens[1]);
			line = reader.readLine();
		}
		reader.close();
*/		
		// read the suitable input file
		HashMap<Integer,String> queries = new HashMap<Integer,String>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_morph.txt"));
		String line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			queries.put(Integer.parseInt(line.substring(0,index)),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		
		
		// FO n-grams extraction - from modern Jewish index
		String targetTermFile = "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_orig.txt";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_morph.txt", "/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly");
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ir/liebesc/ResponsaSys/input/tries/targetTerms_qeOnlyAll.txt"));
		IndexReader modernReader = IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes/modernHebrew")));
		IndexReader modernJewishReader = IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly")));
//		String outputDir = "/home/ir/liebesc/ResponsaSys/output/modernJewishOnlyLowFreq";
//		File dir = new File(outputDir+"/Surface_Surface/");
		String outputDir = "/home/ir/liebesc/ResponsaSys/output/baseline/Surface_Surface/";
		File dir = new File(outputDir);
		DiceScorer scorer = new DiceScorer();
//		Wikitionary wiki = new Wikitionary(new File("/home/ir/liebesc/ResponsaSys/input/wikiRel"));
		for (File f:dir.listFiles()) {
//			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_NoOldModernAddDocumentInModernCorpusModernJewishMWE0_01Freq2_0.filter")) {
//			if (f.getName().endsWith("_NoOldModernAddDocumentInModernCorpus.filter")) {
				String targetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
				int queryId = Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_")));
				String queryLine = queryId + "\t" + queries.get(queryId);
				
/*				if (wikiSet.contains(targetTerm)) {
					HashSet<String> wikiRel = wiki.getRelSet(targetTerm, true, 1);
					if(wikiRel.size()>0){
						for(String rel:wikiRel)
							queryLine = queryLine + "\t" + rel;
					}
					writer.write(queryLine+"\n");
				}
					
*/				//else if(modernQeSet.contains(targetTerm)) {
			
					reader = new BufferedReader(new FileReader(f));
					line = reader.readLine(); // skip caption
					line = reader.readLine();
					
					HashSet<Integer> targetDocsSet = new HashSet<Integer>();
					for(ScoreDoc sd:targetDocs.get(targetTerm))
						targetDocsSet.add(sd.doc);
					
					int modernTargetCount = modernReader.docFreq(new Term("TERM_VECTOR",StringUtils.fixQuateForSearch(targetTerm)));
					
					int counter = 0;
					int addedDocs = 0;
					HashSet<String> expSet = new HashSet<String>();
					
					while (line!=null && counter < 3){
						counter ++;
						String tokens[] = line.split("\t");
						// modern co-occurrence score
						int jointCount = Integer.parseInt(tokens[6]);
						String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
						int expCount = modernReader.docFreq(new Term("TERM_VECTOR",candTerm));
						double score = scorer.score(0,modernTargetCount,expCount,jointCount);
						
						int index = candTerm.indexOf("של ");
						if (index != -1){
							boolean bfound = false;
							for(String s:targetTerm.split(" "))
								if(candTerm.indexOf(s)!=-1){
									System.out.println("============================"+targetTerm + "\t" + candTerm);
									bfound = true;
									break;
							}
							if(bfound){
								line = reader.readLine();
								continue;
							}
						}
						
						if (candTerm.equals("ראה ערך")){
							line = reader.readLine();
							continue;
						}
							
						
						//modern jewish added documents 
						TermDocs td = modernJewishReader.termDocs(new Term("TERM_VECTOR",candTerm));
						int addModernJewishCount = 0;
						while(td.next())
							if(!targetDocsSet.contains(td.doc()))
								addModernJewishCount += 1;
						
						//avoid returning multiple forms of the same expression
						boolean bUsed = false;
						for(String s:expSet)
							if(candTerm.contains(s)||s.contains(candTerm)) {
								bUsed = true;
								counter--;
							}
						
						System.out.println("Term: " + candTerm);
						int targetModernJewishMorphCount = targetDocs.get(targetTerm).size();
	//					int addModernJewishCount = Integer.parseInt(tokens[3]);
						System.out.println("AddModernJewishCount: " + addModernJewishCount);
						System.out.println("Score: " + score);
						System.out.println("addedDocs+addModernJewishCount: " + (addedDocs+addModernJewishCount));
						System.out.println(("2*targetModernJewishMorphCount: " + (2*targetModernJewishMorphCount)));
						System.out.println("bUsed: " + bUsed);
						if (score > 0.009 && !bUsed && (addedDocs+addModernJewishCount)<(10*targetModernJewishMorphCount) && expCount < 100){
//						if (score > 0.1 && !bUsed && (addedDocs+addModernJewishCount)<(10*targetModernJewishMorphCount)){
//						if (score > 0.009 && !bUsed && (addModernJewishCount)<(10*targetModernJewishMorphCount)){
//						if (!bUsed && (addedDocs+addModernJewishCount)<(2*targetModernJewishMorphCount)){
							queryLine = queryLine + "\t" + candTerm;
							addedDocs += addModernJewishCount;
							expSet.add(candTerm);
						}
						line = reader.readLine();
					}
					reader.close();
					writer.write(queryLine+"\n");
		}
		}
		writer.close();
	}

}
