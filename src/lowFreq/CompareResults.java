package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CompareResults {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashMap<Integer,Double> file1Content = new HashMap<Integer, Double>();
		ArrayList<String> file1= new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader("C:\\Documents and Settings\\HZ\\Desktop\\28_NoModernOldMWE1_0.filter"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\compare28.txt"));
		writer.write("CandTerm\tposAfter\tposBefore\tMWEscore\n");
		String line = reader.readLine(); // skip first line
		line = reader.readLine();
		int counter = 0;
		while (line!=null && counter<50){
			String[] tokens = line.split("\t");
			String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
			file1.add(candTerm);
			file1Content.put(counter, Double.parseDouble(tokens[7]));
			line = reader.readLine();
			counter++;
		}
		reader.close();
		reader = new BufferedReader(new FileReader("C:\\Documents and Settings\\HZ\\Desktop\\28_afterNoModernOldMWE1_0.filter"));
		line = reader.readLine(); // skip first line
		line = reader.readLine();
		counter = 0;
		HashSet<Integer> intersecting = new HashSet<Integer>();
		while (line!=null && counter<50){
			String[] tokens = line.split("\t");
			String candTerm = tokens[0].replaceAll("\\p{Punct}|\\d","");
			double score = Double.parseDouble(tokens[7]);
			writer.write(candTerm + "\t" + (counter) + "\t" + file1.indexOf(candTerm) + "\t"+ score + "\n");
			intersecting.add(file1.indexOf(candTerm));
			line = reader.readLine();
			counter++;
		}
		reader.close();
		for(int i=0;i<50;i++)
			if(!intersecting.contains(i))
				writer.write(file1.get(i) + "\t-1\t" + i + "\t" + file1Content.get(i) + "\n");
		writer.close();
	}

}
