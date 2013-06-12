package lowFreq;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import clustering.JungClustering;

public class ClusterMWE {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashSet<String> input = new HashSet<String>();
		FileInputStream fIn = new FileInputStream("C:\\Documents and Settings\\HZ\\Desktop\\33_Data.txt");
//		BufferedReader reader = new BufferedReader(new InputStreamReader(fIn, "Windows-1255"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fIn, "UTF-8"));
//		BufferedReader reader = new BufferedReader(new FileReader("C:\\Documents and Settings\\HZ\\Desktop\\50_Data.txt"));
		String line = reader.readLine();
		line = reader.readLine();
		int counter = 0;
		while (line != null && counter < 100) {
			if (Integer.parseInt(line.split("\t")[2]) > 0 && Double.parseDouble(line.split("\t")[6]) > 0.01 && Double.parseDouble(line.split("\t")[7]) > 0.1 ) {
				input.add(line.split("\t")[0].replaceAll("\\p{Punct}|\\d",""));
				counter++;
			}
		    line = reader.readLine();
		}
		
		JungClustering jung = new JungClustering();
		jung.buildGraph(input,new MWEdistance());
		Set<Set<String>> responsePartition = jung.cluster();
	    for(Set<String> cluster:responsePartition) {
	    	System.out.println(cluster);
	    }
	}

}
