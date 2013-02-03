package judgements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import clustering.Cluster;

public class PrintGoldStandard {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/**/ File gsFile = new File("C:\\goldStandard.txt");
		/**/ String judgementsDir = "C:\\ResponsaNew\\judgements";
		
		JudgementForEval judge = new JudgementForEval(judgementsDir);
		HashMap<String, LinkedList<Cluster>> gsMap = judge.loadGoldStandardClusters();
		BufferedWriter writer = new BufferedWriter(new FileWriter(gsFile));
		SortedSet<String> keys = new TreeSet<String>(gsMap.keySet());
		for (String targetTerm: keys){
			writer.write(targetTerm + "\n");
			LinkedList<Cluster> clsList = gsMap.get(targetTerm);
			for (Cluster cls:clsList) {
				writer.write(cls.getTerms() + "\t" + cls.getLemmas() + "\t" + cls.getGroup() + "\n");
			}
		}
		writer.close();
	}

}
