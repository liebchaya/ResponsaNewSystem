package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class TrigramsMutualExpectation implements MWEScorer {

	public static void main(String[] args) throws IOException{
		TrigramsMutualExpectation tMRR = new TrigramsMutualExpectation(new File("C:\\responsa3.cnt"));
		System.out.println("צער בעלי חיים " + tMRR.score("צער בעלי חיים"));
		System.out.println("כיבוד אב ואם " + tMRR.score("כיבוד אב ואם"));
		System.out.println("של צער בעלי " + tMRR.score("של צער בעלי"));
		System.out.println("משום צער בעלי " + tMRR.score("משום צער בעלי"));
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
		double p = (double)k/m_trigramsNum;
		double score = p*normalizedExpectation(ngram);
		return score;
	}
	
	private double normalizedExpectation(String trigram){
		double score = 0.0;
		String[] trigramData = m_ngramsData.get(trigram).split(" ");
		int k = Integer.parseInt(trigramData[0]);
		int omittedNgramsSum = 0;
		for (int i=4; i< trigramData.length; i++)
			omittedNgramsSum += Integer.parseInt(trigramData[i]);
		double FPE = 0.333*(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	}
	
	public TrigramsMutualExpectation(File bigramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(bigramsFile));
		m_ngramsData = new HashMap<String, String>();
		String line = reader.readLine();
		int lineNum = 1;
		m_trigramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_ngramsData.put(tokens[0] + " " + tokens[1] + " " + tokens[2], tokens[3]);
			line = reader.readLine();
			if (lineNum%10000==0)
				System.out.println("line: " + lineNum);
			if (lineNum > 1000000)
				break;
		}
		System.out.println("Finished loading data");
	}
	
	private HashMap<String,String> m_ngramsData = null;
	private int m_trigramsNum = 0;
	static private String m_name="TrigramsMutualExpectation";

}
