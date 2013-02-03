package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import judgements.JudgementForEval;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

import clustering.Cluster;
import clustering.ClusterList;
import clustering.ClusterFilter;

import utils.FileUtils;


public class PrevReplicatedEvaluation {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MorphLemmatizerException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, MorphLemmatizerException, InstantiationException, IllegalAccessException {
		
		/**/    String judgementsDir = "C:\\ResponsaNew\\judgements";
		/**/	File outputDir = new File("C:\\NewStatisticsFolder\\AllBest_Best");
		/**/	int termsNum = 50;
		/**/	int clustersNum = 15;
		
		// Verify that there aren't missing terms
		JudgementForEval judge = new JudgementForEval(judgementsDir);
		if (!judge.canEvaluate(outputDir, termsNum)) {
			System.out.println("Cannot evaluate - missing judgements");
			System.exit(0);
		}
	
		/**/	String taggerDir = "C:\\projects_ws\\Tagger\\";
		/**/	Boolean isMila = true;
		MorphLemmatizer.initLemmatizer(taggerDir, isMila);
		ClusterFilter clsFilter = new ClusterFilter();
		HashSet<String> clusterMem = FileUtils.loadFileToSet(new File("C:\\ResponsaNew\\evaluations\\clusterMem.txt"));
		
		Evaluation<Cluster> clusterEval = null;
		BufferedWriter evalWriter = new BufferedWriter(new FileWriter("C:\\NewStatisticsFolder\\AllBest_Best\\clusters50_MAXLENGTH_2\\eval.txt"));
		BufferedWriter topWriter = new BufferedWriter(new FileWriter("C:\\NewStatisticsFolder\\AllBest_Best\\clusters50_MAXLENGTH_2\\clustersTop15.txt"));
		
		File clsDir = new File(outputDir.getAbsoluteFile()+"\\clusters50_MAXLENGTH_2");
		for (File f:clsDir.listFiles()) {
			if (f.getName().endsWith(".clusters")) {
			String targetTerm = f.getName().substring(0,f.getName().indexOf("_"));
			judge.initTermJudgement(targetTerm);
			
			if(targetTerm.equals("גיבונים"))
				System.out.println("fdfd");
			
			HashSet<Cluster> relevantClusters = judge.getRelevantClusters();
			LinkedList<Cluster> responsaClusters = ClusterList.loadClusterFromFile(f);
			
			clsFilter.createFilterCluster(targetTerm);
			relevantClusters = (HashSet<Cluster>) clsFilter.filterSeed(relevantClusters);
			relevantClusters = clsFilter.filterGoldStandard(relevantClusters, clusterMem);
			
			ClusterList.assignClustersJudgments(responsaClusters, judge.getRelevantJudgements());
			responsaClusters = (LinkedList<Cluster>) clsFilter.filterSeed(responsaClusters);
			responsaClusters = clsFilter.filterTop(responsaClusters,clustersNum);
			responsaClusters = clsFilter.filterDuplicates(responsaClusters,relevantClusters, true);
			
			topWriter.write(targetTerm + "\n");

			for (Cluster cls:responsaClusters)
				topWriter.write(cls.getTerms() + "\t" + cls.getScore() + "\t" +cls.getbAnno()+ "\n");
			clusterEval = new Evaluation<Cluster>(responsaClusters,relevantClusters,"terms");
			evalWriter.write(targetTerm+"\tgroupJudges\t"+clusterEval.getShortEvalString()+"\n");
			}
		}
		evalWriter.close();
		topWriter.close();
	}
		
		
		
	
}
