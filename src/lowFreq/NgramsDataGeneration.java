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

import mwe.scorers.MutualExpectation;
import obj.Pair;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;

import utils.StringUtils;
import utils.TargetTerm2Id;

public class NgramsDataGeneration {
	
	public NgramsDataGeneration(String oldIndex, String modernJewishIndex, String modernIndex) throws CorruptIndexException, IOException {
		m_ngramData = new NgramData(oldIndex, modernJewishIndex, modernIndex);
		m_modernJewishIndex = modernJewishIndex;
		
	}
	
	public void generateDataFiles(String outputDir, String statFileSufix, String expTargetTermFile, String oldNgramsFileName, String modernJewishNgramsFileName, int maxN) throws IOException, ParseException{

		// FO n-grams extraction - from modern Jewish index
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, expTargetTermFile, m_modernJewishIndex);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		HashMap<String, HashSet<Integer>> targetDocsSet = new HashMap<String, HashSet<Integer>>();
		for(String targetTerm: targetDocs.keySet()) {
			HashSet<Integer> docsSet = new HashSet<Integer>();
			ArrayList<ScoreDoc> origDocs = targetDocs.get(targetTerm);
			for(ScoreDoc sd:origDocs)
				docsSet.add(sd.doc);
			targetDocsSet.put(targetTerm, docsSet);
		}
		
		// MWE treatment
		MutualExpectation meOld = new MutualExpectation(oldNgramsFileName,maxN);
		MutualExpectation meModernJewish = new MutualExpectation(modernJewishNgramsFileName,maxN);
	
		File dir = new File(outputDir+"/Surface_Surface/");
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith(statFileSufix)) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace(statFileSufix,"_Data.txt")));
				writer.write("Candidate\tDice\tFreq\tOld Period Count\tAdded Union\tAdded Intersection\tModern Intersection\tMWE Modern Jewish\tMWE Old\n");
				String line = reader.readLine();
				int lineNum = 0;
				while (line!=null){
					lineNum++;
					if (lineNum > 2500)
						break;
					String[] tokens = line.split("\t");
					String candTerm = tokens[0];
					//Clean up punctuation and digits
					candTerm = candTerm.replaceAll("\\p{Punct}|\\d","");
					// Remove unigrams
					if (candTerm.split(" ").length<2)
					{
						line = reader.readLine();
						continue;
					}
					int oldPeriodCount = m_ngramData.countOldPeriod(candTerm);
					String origTargetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
					Pair<Integer,Integer> contribPair = m_ngramData.NgramPossibleContribution(candTerm, origTargetTerm, targetDocsSet);
					int modernInter = -1;
					double meOldScore = -1;
					double meModernJewishScore = meModernJewish.score(candTerm);
					if (oldPeriodCount > 0)
						meOldScore = meOld.score(candTerm);
					else
						modernInter = m_ngramData.countModernIntersaction(candTerm, origTargetTerm);
					writer.write(line + "\t" + oldPeriodCount + "\t" + contribPair.key() + "\t" + contribPair.value() + "\t" + modernInter + "\t" + meModernJewishScore + "\t" + meOldScore + "\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
		
	}
	private NgramData m_ngramData = null;
	private String m_modernJewishIndex;
}
