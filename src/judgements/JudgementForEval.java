package judgements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import morphology.MorphLemmatizerException;

import clustering.Cluster;
import clustering.ClusterList;

import obj.TermsLemmaJudgements;
import obj.WeightedTerm;
import utils.StringUtils;
import utils.WeightedTermUtils;

public class JudgementForEval {

	public JudgementForEval(String judgementsDir){
		m_judgementsDir = judgementsDir;
	}

	public HashMap<String,HashSet<Cluster>> loadRelativeRecallGoldStandard(File gsFile, String outputDir, String clusteringType, String clustersFileName) throws IOException{
		if (!gsFile.exists()) {
			generateRelativeRecallGoldStandard(gsFile, outputDir, clusteringType, clustersFileName);
		}
		return loadRelativeRecallGoldStandardFromFile(gsFile);
		
	}
	
	private void generateRelativeRecallGoldStandard(File gsFile, String outputDir, String clusteringType, String clustersFileName) throws IOException{
		HashMap<String,LinkedList<Cluster>> gsClusters = loadGoldStandardClusters();
		GoldStandard4RelativeRecall gs = new GoldStandard4RelativeRecall(gsClusters);
		gs.markRelativeGoldStandard(outputDir, clusteringType, clustersFileName);
		gs.printGoldStandard(gsFile);
	}
	
	public HashMap<String,LinkedList<Cluster>> loadGoldStandardClusters() throws IOException{
		HashMap<String,LinkedList<Cluster>> gsClusters = new HashMap<String, LinkedList<Cluster>>();
		File judgements = new File(m_judgementsDir+"\\current");
		for( File f: judgements.listFiles()){
			String targetTerm = f.getName().substring(0,f.getName().indexOf("."));
			if (f.getName().endsWith(".groups")) { //avoid getting each target term twice
				initTermJudgement(targetTerm);
				gsClusters.put(targetTerm, getRelevantClustersList());
			}
		}
		return gsClusters;
	}
	
	private HashMap<String,HashSet<Cluster>> loadRelativeRecallGoldStandardFromFile(File gsFile) throws IOException{
		HashMap<String,HashSet<Cluster>> relevantCluster = new HashMap<String, HashSet<Cluster>>();
		BufferedReader reader = new BufferedReader(new FileReader(gsFile));
		String line = reader.readLine();
		String targetTerm = null;
		while (line != null) {
			if (!line.contains("\t")) { // target term name
				targetTerm = line.trim();
			} else{
				String [] tokens = line.split("\t");
					Cluster cls = new Cluster(StringUtils.convertStringToSet(tokens[1]),StringUtils.convertStringToSet(tokens[0]),true,Integer.parseInt(tokens[2]));
					if (relevantCluster.containsKey(targetTerm))
						relevantCluster.get(targetTerm).add(cls);
					else {
						HashSet<Cluster> clsList = new HashSet<Cluster>();
						clsList.add(cls);
						relevantCluster.put(targetTerm,clsList);
					}
			}
			line = reader.readLine();
		}
		reader.close();
		return relevantCluster;
	}
	
	public boolean canEvaluate(File outputDir, int termsNum) throws IOException, MorphLemmatizerException {
		boolean hasMissingTerms = false;
		for(File f:outputDir.listFiles()) {
			if (f.isFile()) {
				LinkedList<WeightedTerm> expTermsList = WeightedTermUtils.loadWTFromFile(f, termsNum, outputDir.getName());
				String sep = "_";
				if (!f.getName().contains("_"))
					sep = ".";
				TermsLemmaJudgements termsJudgements = new TermsLemmaJudgements(f.getName().substring(0,f.getName().indexOf(sep)),m_judgementsDir);
				termsJudgements.append(WeightedTermUtils.convertWTList2StringList(expTermsList));
				if (termsJudgements.exportJFile()) {
					System.out.println("Missing judgement in: " + f.getAbsolutePath());
					hasMissingTerms = true;
				}
			}
		}
		return hasMissingTerms;
	}
	
	public boolean canEvaluate(LinkedList<Cluster> responsaClusters, String targetTerm) throws IOException, MorphLemmatizerException {
		boolean hasMissingTerms = false;
		TermsLemmaJudgements termsJudgements = new TermsLemmaJudgements(targetTerm,m_judgementsDir);
		termsJudgements.append(ClusterList.convertClusters2StringList(responsaClusters));
		if (termsJudgements.exportJFile()) {
			System.out.println("Missing judgement for: " + targetTerm);
			hasMissingTerms = true;
		}
		return hasMissingTerms;
	}
	
	public HashSet<Cluster> getRelevantClusters() {
		if (m_termsJudgements != null) {
			HashMap<String,HashMap<String,Integer>> relevantTermJudges = m_termsJudgements.getGroupMapRelJudeges();
			return ClusterList.mapToClusters(relevantTermJudges,true);
		}
		return null;
	}
	
	public LinkedList<Cluster> getRelevantClustersList() {
		if (m_termsJudgements != null) {
			HashMap<String,HashMap<String,Integer>> relevantTermJudges = m_termsJudgements.getGroupMapRelJudeges();
			return ClusterList.mapToClustersList(relevantTermJudges,true);
		}
		return null;
	}
	
	public HashSet<String> getRelevantJudgements(){
		if (m_termsJudgements != null)
			return m_termsJudgements.getTermsLstRelJudeges();
		return null;
	}
	
	public void initTermJudgement(String targetTerm) throws IOException {
		m_termsJudgements = new TermsLemmaJudgements(targetTerm,m_judgementsDir);
	}
	
	private String m_judgementsDir = "C:\\ResponsaNew\\judgements";
	private TermsLemmaJudgements m_termsJudgements;
}
