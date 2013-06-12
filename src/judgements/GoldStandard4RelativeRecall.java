package judgements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import utils.StringUtils;

import clustering.Cluster;

public class GoldStandard4RelativeRecall {

	
	
	
	public GoldStandard4RelativeRecall(
			HashMap<String, LinkedList<Cluster>> clusters) {
		m_gsClusters = clusters;
	}

	public void markRelativeGoldStandard(String outputDir, String clusteringType, String clustersFileName) throws IOException{
		File output = new File(outputDir);
		for ( File f: output.listFiles() ) {
			if (f.isDirectory()&& f.getName().contains("LIN")) {
				File clustersFile = new File(f.getAbsolutePath()+"\\"+clusteringType+"\\"+ clustersFileName);
				BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
				String line = reader.readLine();
				String targetTerm = null;
				while (line != null ){
					if (!line.contains("\t")) { // target term name
						targetTerm = line.trim();
					} else{
						String [] tokens = line.split("\t");
						if(tokens[2].equals("true")) {
							Cluster cls = new Cluster(new HashSet<String>(),StringUtils.convertStringToSet(tokens[0]),Double.parseDouble(tokens[1]));
							cls.setbAnno(true);
							int clusterPos = m_gsClusters.get(targetTerm).indexOf(cls);
							m_gsClusters.get(targetTerm).get(clusterPos).setbIsUsed(true);
						}
					}
					line = reader.readLine();
				}
				reader.close();
			}
		}
		
	}
	
	public void printGoldStandard(File gsFile) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(gsFile));
		SortedSet<String> keys = new TreeSet<String>(m_gsClusters.keySet());
		for (String targetTerm: keys){
			writer.write(targetTerm + "\n");
			LinkedList<Cluster> clsList = m_gsClusters.get(targetTerm);
			for (Cluster cls:clsList) {
				if(cls.getbIsUsed())
					writer.write(cls.getTerms() + "\t" + cls.getLemmas() + "\t" + cls.getGroup() + "\n");
			}
		}
		writer.close();
	}
	
	private HashMap<String,LinkedList<Cluster>> m_gsClusters = null;
}
