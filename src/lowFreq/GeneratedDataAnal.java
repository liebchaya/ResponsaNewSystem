package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;

import utils.TargetTerm2Id;

public class GeneratedDataAnal{
	
	
	 public GeneratedDataAnal(int candidatesNum) throws IOException, ParseException {
		m_candadatesNum = candidatesNum;
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(targetType, "/home/ir/liebesc/ResponsaSys/input/targetTermsAll_morph.txt", "/home/ir/liebesc/ResponsaSys/indexes/modernJewishOnly");
		m_targetDocs = targetRp.extractDocsByRepresentation();
		TargetTermRepresentation oldTargetRp = new JewishTargetTermRepresentation(targetType,"/home/ir/liebesc/ResponsaSys/input/targetTermsAll_morph.txt",  "/home/ir/liebesc/ResponsaSys/indexes/oldResponsa");
		m_oldTargetDocs = oldTargetRp.extractDocsByRepresentation();
	}
	
	public void analFiles(String outputDir) throws IOException, ParseException{
		File dir = new File(outputDir+"/Surface_Surface/");
		BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/targetTermsAllAnal.txt"));
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_Data.txt")) {
				writer.write(analFile(f));
			}
		}
		writer.close();
	}
	
	public String analFile(File f) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String targetTermName = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
		String line = reader.readLine();
		int lineNum = 0;
		line = reader.readLine();
		int oldCounter = 0;
		while (line!=null && lineNum < m_candadatesNum){
			if(line.split("\t")[0].split(" ").length > 1){
				if (isOld(line))
					oldCounter++;
				lineNum++;
			}
			line = reader.readLine();			
		}
		reader.close();
		return new String(f.getName().substring(0,f.getName().indexOf("_")) + "\t" + targetTermName + "\t" + oldCounter + "\t" + m_targetDocs.get(targetTermName).size() + "\t" + m_oldTargetDocs.get(targetTermName).size() + "\n");
	}
	/**
	 * fileLine format: 
	 * term + "\t" + score + "\t" + frequency + "\t" + oldPeriodCount + "\t" + contribPair.key() + "\t" + contribPair.value() + "\t" + modernInter + "\t" + meModernJewishScore + "\t" + meOldScore + "\n");
	 * @param fileLine
	 * @return
	 */
	private boolean isOld(String fileLine){
		String tokens[] = fileLine.split("\t");
		if (Integer.parseInt(tokens[3]) > 0)
			return true;
		return false;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException{
		TargetTerm2Id.loadTargetTerm2IdMapping(new File("/home/ir/liebesc/ResponsaSys/input/targetTermsAll_orig.txt"));
		GeneratedDataAnal dataAnal = new GeneratedDataAnal(50);
		dataAnal.analFiles("/home/ir/liebesc/ResponsaSys/output/modernJewishOnlyAllDiceFreq");
	}
	 
	private int m_candadatesNum;
	private HashMap<String, ArrayList<ScoreDoc>> m_targetDocs = null;
	private HashMap<String, ArrayList<ScoreDoc>> m_oldTargetDocs = null;
 
 
}
