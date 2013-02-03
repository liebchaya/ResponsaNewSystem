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

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.FeatureRepresentation;
import representation.JewishFeatureRepresentation;
import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.TargetTermRepresentation.TargetTermType;
import fo.scorers.StatScorer;
import fo.scorers.TfIdfScorer;
import fo.similarity.FeatureVectorExtractor2Measures;

import utils.StringUtils;
import utils.TargetTerm2Id;

public class CountModernJewish {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {

		String responsaMainDir = "/home/ir/liebesc/ResponsaSys/";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(responsaMainDir+"input/hozDescQuery.txt"));
		
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(responsaMainDir,targetType);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
	
		File dir = new File("/home/ir/liebesc/ResponsaSys/output/modernJewishOnly/Surface_Surface/");
		IndexReader reader = IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly")));
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_OldCounts.txt")) {
				BufferedReader fileReader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace("_OldCounts.txt","_ModernCounts.txt")));
				System.out.println("New file name: " +dir.getAbsolutePath() + "/" + f.getName().replace("_OldCounts.txt","_ModernCounts.txt"));
				
				HashSet<Integer> docsSet = new HashSet<Integer>();
				String origQuery = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
				ArrayList<ScoreDoc> origDocs = targetDocs.get(origQuery);
				for(ScoreDoc sd:origDocs)
					docsSet.add(sd.doc);
				
				String line = fileReader.readLine();
				while (line!=null){
					String[] tokens = line.split("\t");
					String query = tokens[0];
		
					TermDocs docs = reader.termDocs(new Term("TERM_VECTOR",query));
					int addDocsNum = 0;
					while(docs.next()){
						if(!docsSet.contains(docs.doc()))
							addDocsNum++;
					}
					writer.write(line + "\t" + addDocsNum + "\n");
					line = fileReader.readLine();
				}
				fileReader.close();
				writer.close();
			}
		}
		
		

	}

}
