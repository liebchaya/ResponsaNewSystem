package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

import obj.Term;
import obj.WeightedTerm;
import utils.StringUtils;

public class ClusterList {
	public static HashSet<Cluster> mapToClusters(HashMap<String,HashSet<String>> map) {
		HashSet<Cluster> clusters = new HashSet<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			set.add(key);
			clusters.add(new Cluster(set,map.get(key)));
		}
		return clusters;
	}
	
	public static HashSet<Cluster> mapToClusters(HashMap<String,HashMap<String,Integer>> map, boolean bAnno) {
		HashSet<Cluster> clusters = new HashSet<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			Integer anno = 0;
			for(String t:map.get(key).keySet()) {
				Integer cur_anno = map.get(key).get(t);
				if(cur_anno > anno )
					anno = cur_anno;
				set.add(key);
			}
			clusters.add(new Cluster(set,map.get(key).keySet(),bAnno,anno));
		}
		return clusters;
	}
	
	public static LinkedList<Cluster> mapToClustersList(HashMap<String,HashMap<String,Integer>> map, boolean bAnno) {
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			Integer anno = 0;
			for(String t:map.get(key).keySet()) {
				Integer cur_anno = map.get(key).get(t);
				if(cur_anno > anno )
					anno = cur_anno;
				set.add(key);
			}
			clusters.add(new Cluster(set,map.get(key).keySet(),bAnno,anno));
		}
		return clusters;
	}
	
	public static LinkedList<Cluster> wtListToClusters(List<WeightedTerm> responsaScores) {
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		for(WeightedTerm wt:responsaScores) {
			clusters.add(new Cluster(StringUtils.convertStringToSet(wt.getLemma()),StringUtils.convertStringToSet(wt.getValue()),wt.weight()));
		}
		return clusters;
	}
	
	public static LinkedList<WeightedTerm> clustersToWt(List<Cluster> responsaClusters) {
		LinkedList<WeightedTerm> responsaScores = new LinkedList<WeightedTerm>();
		for(Cluster cls:responsaClusters) {
			responsaScores.add(new WeightedTerm(new Term(StringUtils.convertSetToString(cls.getTerms()),StringUtils.convertSetToString(cls.getLemmas())),cls.getScore()));
		}
		return responsaScores;
	}
	
	public static void assignClustersJudgments(LinkedList<Cluster> clusters, HashSet<String> posTerms){
		for(Cluster cls:clusters){
			int posCount = 0, negCount =0;
			boolean bPos = false;
			for(String t:cls.getTerms())
				if(posTerms.contains(t)){
					bPos = true;
					posCount++;
				} else
					negCount++;
			cls.setbAnno(bPos);
			if(posCount != cls.getTerms().size() && negCount != cls.getTerms().size())
				cls.setIsConflict(true);
		}
	}
	
	public static int getClustersConflictsNum(LinkedList<Cluster> clusters){
		int num = 0;
		for(Cluster cls:clusters){
			if(cls.isConflict()){
				num +=1;
				System.out.println("conflict: "+ cls);
			}
		}
		return num;
	}
	
	public static void initClustersJudgments(LinkedList<Cluster> clusters, boolean anno){
		for(Cluster cls:clusters){
			cls.setbAnno(anno);
		}
	}
	
	/*
	 * 
	 */
	public static int getClustersMissingNum(LinkedList<Cluster> judgedClusters,HashSet<Cluster> relevantClusterJudges){
		int num = 0;
		for(Cluster cls:judgedClusters){
			if(cls.getbAnno()&& !relevantClusterJudges.contains(cls)){
				num +=1;
				System.out.println("missing recall point: "+ cls);
			}
		}
		return num;
	}
	
	public static LinkedList<Cluster> loadClusterFromFile(File clustersFile) throws IOException{
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
		String line = reader.readLine();
		while (line != null) {
			String [] tokens = line.split("\t");
			clusters.add(new Cluster(StringUtils.convertStringToSet(tokens[1]), StringUtils.convertStringToSet(tokens[0]), Double.parseDouble(tokens[2])));
			line = reader.readLine();
		}
		reader.close();
		return clusters;
	}
	
	public static LinkedList<Cluster> loadClusterFromResultFile(File clustersFile) throws IOException, MorphLemmatizerException{
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
		String line = reader.readLine();
		while (line != null) {
			String [] tokens = line.split("\t");
			HashSet<String> resSet = StringUtils.convertStringToSet(tokens[0]);
			Set<String> lemmaSet = new HashSet<String>();
			if (resSet.size()>0)
				lemmaSet = MorphLemmatizer.getMostProbableLemma((String) resSet.toArray()[0]);
			clusters.add(new Cluster(lemmaSet, resSet, Double.parseDouble(tokens[1])));
			line = reader.readLine();
		}
		reader.close();
		return clusters;
	}
	
	public static LinkedList<String> convertClusters2StringList(List<Cluster> responsaClusters) {
		LinkedList<String> stringSet = new LinkedList<String>();
		for(Cluster cls:responsaClusters) {
			for (String term:cls.getTerms())
				stringSet.add(term);
		}
		return stringSet;
	}
	
}
