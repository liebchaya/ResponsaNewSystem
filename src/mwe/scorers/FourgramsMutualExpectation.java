package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class FourgramsMutualExpectation implements MWEScorer {

	public static void main(String[] args) throws IOException{
		FourgramsMutualExpectation fMRR = new FourgramsMutualExpectation(new File("C:\\responsa4.cnt"));
		System.out.println("המוציא מחבירו עליו הראיה " + fMRR.score("המוציא מחבירו עליו הראיה"));
		System.out.println("יפתח בדורו כשמואל בדורו " + fMRR.score("יפתח בדורו כשמואל בדורו"));
		System.out.println("הרבנים האחרונים הקרובים אלינו " + fMRR.score("הרבנים האחרונים הקרובים אלינו"));
		System.out.println("נאמר כבד את אביך " + fMRR.score("נאמר כבד את אביך"));
	}
	
	public boolean exist(String ngram) {
		if (m_ngramsData.containsKey(ngram))
			return true;
		return false;
	}

	
	public String getName() {
		return m_name;
	}

	
	public double score(String ngram) {
		int k = Integer.parseInt( m_ngramsData.get(ngram).split(" ")[0]);
		double p = (double)k/m_fourgramsNum;
		double score = p*normalizedExpectation(ngram);
		return score;
	}
	
	private double normalizedExpectation(String fourgram){
		double score = 0.0;
		String[] fourgramData = m_ngramsData.get(fourgram).split(" ");
		int k = Integer.parseInt(fourgramData[0]);
		int omittedNgramsSum = 0;
		for (int i=5; i< fourgramData.length; i++)
			omittedNgramsSum += Integer.parseInt(fourgramData[i]);
		double FPE = 0.25*(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	}
	
	public FourgramsMutualExpectation(File bigramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(bigramsFile));
		m_ngramsData = new HashMap<String, String>();
		String line = reader.readLine();
		int lineNum = 1;
		m_fourgramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_ngramsData.put(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3], tokens[4]);
			line = reader.readLine();
			if (lineNum%10000==0)
				System.out.println("line: " + lineNum);
			if (lineNum > 1000000)
				break;
		}
		System.out.println("Finished loading data");
	}
	
	private HashMap<String,String> m_ngramsData = null;
	private int m_fourgramsNum = 0;
	static private String m_name="FourgramsMutualExpectation";

}
