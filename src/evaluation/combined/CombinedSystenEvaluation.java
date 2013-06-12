package evaluation.combined;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import judgements.JudgementForEval;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

import clustering.Cluster;
import clustering.ClusterList;
import clustering.ClusterFilter;
import evaluation.Evaluation;

import utils.FileUtils;


public class CombinedSystenEvaluation {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MorphLemmatizerException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, MorphLemmatizerException, InstantiationException, IllegalAccessException {
		
		/**/    String judgementsDir = "C:\\ResponsaNew\\judgements";
		/**/	File outputDir = new File("C:\\Combined\\CombinedPer");
		/**/	int termsNum = 50;
		/**/	int clustersNum = 15;
		
		/**/ 	String clusteringType = "clusters50_SUMSCORE_2";
		/**/ 	String clustersFileName = "clustersTop15RGS.txt";
		
		JudgementForEval judge = new JudgementForEval(judgementsDir);
		HashMap<String, HashSet<Cluster>> gsMap = judge.loadRelativeRecallGoldStandard(new File("C:\\relativeRecall15GS_SUMSCORE_All.txt"), outputDir.getAbsolutePath(), clusteringType, clustersFileName);
		
		/**/	String taggerDir = "C:\\projects_ws\\Tagger\\";
		/**/	Boolean isMila = true;
		MorphLemmatizer.initLemmatizer(taggerDir, isMila);
		ClusterFilter clsFilter = new ClusterFilter();
		
		Evaluation<Cluster> clusterEval = null;
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir+"\\"+"analData.eval"));
		int clusterGS = 0, clusterSum = 0, conflictsNum = 0, lostNum = 0, dupNum = 0;//, missGsNum = 0;
		writer.write("Configuration\tDisagreement Rate\tLost Rate\tDuplicate Rate\n"); //Missing GoldStandard Rate\n");
		
//		for(File conf: outputDir.listFiles()) {
//			if (conf.isDirectory() && !conf.getName().contains("COVER")) {
		
		File conf = new File("C:\\Combined\\CombinedPer");
				System.out.println(conf.getAbsolutePath());
////				// Verify that there aren't missing terms
//				if (judge.canEvaluate(conf, termsNum)) {
//					System.out.println("Cannot evaluate - missing judgements");
//					System.exit(0);
//				}
			
//			
				BufferedWriter evalWriter = new BufferedWriter(new FileWriter(conf.getAbsolutePath() + "\\" + clusteringType + "\\evalFinal.txt"));
				BufferedWriter topWriter = new BufferedWriter(new FileWriter(conf.getAbsolutePath() + "\\" + clusteringType + "\\clustersTop15RGSFinal.txt"));
				clusterGS = 0; clusterSum = 0; conflictsNum = 0; lostNum = 0; dupNum = 0;// missGsNum = 0;
				
				
				File clsDir = new File(conf.getAbsoluteFile()+ "\\" + clusteringType);
				String sep = "_";
				for (File f:clsDir.listFiles()) {
					if (f.getName().endsWith(".clusters")) {
					if (!f.getName().contains(sep))
						sep = ".";
					String targetTerm = f.getName().substring(0,f.getName().indexOf(sep));
					judge.initTermJudgement(targetTerm);
					
					HashSet<Cluster> relevantClusters = gsMap.get(targetTerm);
					if (relevantClusters == null) // initiate an empty set
						relevantClusters = new HashSet<Cluster>();
					LinkedList<Cluster> responsaClusters = ClusterList.loadClusterFromResultFile(f);
					
					clsFilter.createFilterCluster(targetTerm);
					HashSet<Cluster> relevantClusterJudgesWithSeeds = relevantClusters;
					relevantClusters = (HashSet<Cluster>) clsFilter.filterSeed(relevantClusters);
					
					ClusterList.assignClustersJudgments(responsaClusters, judge.getRelevantJudgements());
					responsaClusters = (LinkedList<Cluster>) clsFilter.filterSeed(responsaClusters);
					responsaClusters = clsFilter.filterTop(responsaClusters,clustersNum);
					
					// Count conflict in duplicates clusters as well
					conflictsNum += ClusterList.getClustersConflictsNum(responsaClusters);
					clusterSum += responsaClusters.size();
					
					responsaClusters = clsFilter.filterDuplicates(responsaClusters,relevantClusters, true);
					
					// Verify that there aren't missing terms
					if (judge.canEvaluate(responsaClusters, targetTerm)) {
						System.out.println("Cannot evaluate - missing judgements");
//						System.exit(0);
					}
					
					topWriter.write(targetTerm + "\n");
		
					for (Cluster cls:responsaClusters)
						topWriter.write(cls.getTerms() + "\t" + cls.getScore() + "\t" +cls.getbAnno()+ "\n");
					clusterEval = new Evaluation<Cluster>(responsaClusters,relevantClusters,"terms");
					evalWriter.write(targetTerm+"\tgroupJudges\t"+clusterEval.getShortEvalString()+"\n");
					
					lostNum += clusterEval.getLostClustersNum();
//					if(ClusterList.getClustersMissingNum(responsaClusters, relevantClusterJudgesWithSeeds)> 0)
//						System.out.println(targetTerm+ " "+responsaClusters);
//					missGsNum += ClusterList.getClustersMissingNum(responsaClusters, relevantClusterJudgesWithSeeds);
					dupNum += clsFilter.getDuplicateNum();
					clusterGS += relevantClusters.size();
					
					}
				}
				writer.write(conf.getName() + "\t");
				//Disagreement Rate
				writer.write((double)conflictsNum/(double)clusterSum + "\t");
				//Lost Rate
				writer.write((double)lostNum/(double)clusterGS + "\t");
				//Duplicate Rate
				writer.write((double)dupNum/(double)clusterSum + "\n");
				//Missing GoldStandard Rate
//				writer.write((double)missGsNum/(double)clusterSum + "\n");
				
				evalWriter.close();
				topWriter.close();
//				}
//		}
		writer.close();
	}
		
		
		
	
}
