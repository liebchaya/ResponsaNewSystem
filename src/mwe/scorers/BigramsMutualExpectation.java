package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class BigramsMutualExpectation implements MWEScorer {

	public static void main(String[] args) throws IOException{
		BigramsMutualExpectation bMRR = new BigramsMutualExpectation(new File("C:\\responsa2.cnt"));
		System.out.println("בית הצואר " + bMRR.score("בית הצואר"));
		System.out.println("בית המקדש " + bMRR.score("בית המקדש"));
		System.out.println("בית ספר " + bMRR.score("בית ספר"));
		System.out.println("בית הוא " + bMRR.score("בית הוא"));
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
		double p = (double)k/m_bigramsNum;
		double score = p*normalizedExpectation(ngram);
		return score;
	}
	
	private double normalizedExpectation(String bigram){
		double score = 0.0;
		String[] bigramData = m_ngramsData.get(bigram).split(" ");
		int k = Integer.parseInt(bigramData[0]);
		int omittedNgramsSum = 0;
		for (int i=1; i< bigramData.length; i++)
			omittedNgramsSum += Integer.parseInt(bigramData[i]);
		double FPE = 0.5*(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	}
	
	public BigramsMutualExpectation(File bigramsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(bigramsFile));
		m_ngramsData = new HashMap<String, String>();
		String line = reader.readLine();
		int lineNum = 1;
		m_bigramsNum = Integer.parseInt(line);
		line = reader.readLine();
		while (line!=null){
			lineNum++;
			String[] tokens = line.split("<>");
			m_ngramsData.put(tokens[0] + " " + tokens[1], tokens[2]);
			line = reader.readLine();
			if (lineNum%10000==0)
				System.out.println("line: " + lineNum);
		}
		System.out.println("Finished loading data");
	}
	
	private HashMap<String,String> m_ngramsData = null;
	private int m_bigramsNum = 0;
	static private String m_name="BigramsMutualExpectation";

}
