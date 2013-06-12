package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class GroupMWE {

	GroupMWE(int candidatesNum){
		m_candidatesNum = candidatesNum;
	}
	
	
	void groupContainingMWE(File input, File output) throws IOException{
		HashMap<String,Double> candidates = new HashMap<String, Double>(); 
		BufferedReader reader = new BufferedReader(new FileReader(input));
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		String line = reader.readLine(); // skip first line
		line = reader.readLine();
		int counter = 0;
		while(line != null && counter < m_candidatesNum){
			String[] tokens = line.split("\t");
			String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
			double score = Double.parseDouble(tokens[7]);
			boolean bFound = false;
			for(String cand:candidates.keySet()){
				if(candTerm.equals(cand)){
					bFound = true;
					break;
				}
				if(candTerm.contains(cand)||cand.contains(candTerm)) {
					double prevScore = candidates.get(cand);
					if(score > prevScore)
					{
						candidates.remove(cand);
						candidates.put(candTerm, score);
					}
					bFound = true;
					break;
				}
			}
			if(!bFound){
				candidates.put(candTerm, score);
				counter++;
			}
			line = reader.readLine();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(input));
		line = reader.readLine(); // skip first line
		line = reader.readLine();
		while(line != null && candidates.size()>0){
			String[] tokens = line.split("\t");
			String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
			double score = Double.parseDouble(tokens[1]);
			if (candidates.containsKey(candTerm)){
				writer.write(candTerm + "\t" + score + "\n");
				candidates.remove(candTerm);
			}
			line = reader.readLine();
		}
		reader.close();
		writer.close();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		GroupMWE group = new GroupMWE(50);
		group.groupContainingMWE(new File("C:\\Documents and Settings\\HZ\\Desktop\\20_NoModernOldMWE1_0.filter"), new File("C:\\groupingMWE20"));

	}
	
	private int m_candidatesNum;

}
