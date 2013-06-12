package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class MutualExpectation implements MWEScorer {

	public static void main(String[] args) throws IOException{
		MutualExpectation me = new MutualExpectation("C:\\responsa",4);
		System.out.println("בית הצואר " + me.score("בית הצואר"));
		System.out.println("בית המקדש " + me.score("בית המקדש"));
		System.out.println("בית ספר " + me.score("בית ספר"));
		System.out.println("בית הוא " + me.score("בית הוא"));
	}

	
	public String getName() {
		return m_name;
	}

	
	public double score(String ngram) {
		if (!m_ngramsData.containsKey(ngram))
			return 0;
		int n = ngram.split(" ").length;
		int k = Integer.parseInt(m_ngramsData.get(ngram).split(" ")[0]);
		double p = (double)k/n;
		double score = p*normalizedExpectation(ngram);
		return score;
	}
	
	private double normalizedExpectation(String ngram){
		double score = 0.0;
		String[] ngramData = m_ngramsData.get(ngram).split(" ");
		int k = Integer.parseInt(ngramData[0]);
		int omittedNgramsSum = 0;
		int n = ngram.split(" ").length;
		int i=1; // bigram
		if (n>2)
			i += n;
		for (; i< ngramData.length; i++)
			omittedNgramsSum += Integer.parseInt(ngramData[i]);
		double FPE = ((double)1/n) *(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	}
	
	public MutualExpectation(String ngramsFileName, int maxN) throws IOException {
		m_ngramCounts = new HashMap<Integer, Integer>();
		m_ngramsData = new HashMap<String, String>();
		for (int i=2; i<maxN+1; i++) {
			String countsFile = ngramsFileName+i+".cnt";
			BufferedReader reader = new BufferedReader(new FileReader(countsFile));
			String line = reader.readLine();
			int lineNum = 1;
			m_ngramCounts.put(i,Integer.parseInt(line));
			line = reader.readLine();
			while (line!=null){
				lineNum++;
				String[] tokens = line.split("<>");
				String ngram = "";
				for(int j=0; j<tokens.length-1; j++)
					ngram = ngram + " " + tokens[j];
				ngram = ngram.trim();
				m_ngramsData.put(ngram, tokens[tokens.length-1]);
				line = reader.readLine();
				if (lineNum%10000==0)
					System.out.println("line: " + lineNum);
//					break;
			}
			reader.close();
			System.out.println("Finished loading data file: " + countsFile);
		}
	}
	
	private HashMap<String,String> m_ngramsData = null;
	private HashMap<Integer,Integer> m_ngramCounts = null;
	static private String m_name="MutualExpectation";

}
