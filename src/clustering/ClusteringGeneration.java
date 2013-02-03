package clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;
import obj.SortByWeightName;
import obj.Term;
import obj.WeightedTerm;


import utils.FileUtils;
import utils.TargetTerm2Id;

import clustering.ClusterScorer.ScorerType;

import com.aliasi.spell.EditDistance;
import com.aliasi.util.Distance;

import ac.biu.nlp.nlp.general.configuration.ConfigurationException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFileDuplicateKeyException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;

public class ClusteringGeneration {
	
	
	
	public ClusteringGeneration(String confName, String termsDir, File indexDir, String queryField, int morphType,
			int termIndex, int scoreIndex, int topNum, double scoreThresholdString, String distMeasure) {
		m_morphPre = new MorphDistancePreprocessing(termsDir,indexDir,queryField,morphType,termIndex, scoreIndex, topNum, scoreThresholdString, false);
		if(distMeasure.equals("Morph"))
			m_distance = new MorphDistance(m_morphPre);
		else // if(distMeasure.equals("Edit"))
			m_distance = new EditDistance(false);
		m_jung = new JungClustering();
		m_confName = confName;
		
	}
	
	public void initDiceScorer(String conf) throws CorruptIndexException, IOException, ParseException{
		m_diceClsScorer = new DiceClusterScorer(conf);
	}

	
	public void loadTargetTermData(File statFile) throws MorphDistancePrePException {
		if (statFile.getName().equals("אבעבועות.txt"))
			System.out.println("here");
		 m_inputTerms = m_morphPre.loadDataFile(statFile.getName(), m_confName);
	}
	
	public void printTargetTermData(BufferedWriter writer) throws IOException{
		if (m_inputTerms != null) {
			for (String term:m_inputTerms.keySet())
				writer.write(term + "\t" + m_inputTerms.get(term) + "\n");
		}
		else
			System.out.println("No data for current target term");
		
	}
	
	public LinkedList<Cluster> generateClustering(String scoreType) throws MorphDistancePrePException, MorphLemmatizerException {
       
    ClusterScorer clsScorer = new ClusterScorer(m_inputTerms,ScorerType.valueOf(scoreType));
    List<WeightedTerm> responsaScores =  new LinkedList<WeightedTerm>();
    
    m_jung.buildGraph(m_inputTerms.keySet(),m_distance);
    Set<Set<String>> responsePartition = m_jung.cluster();
    
    for(Set<String> cluster:responsePartition) {
    	HashSet<String> lemmas = new HashSet<String>();
    	for(String str:cluster)
    		lemmas.addAll(m_morphPre.getLemmasList(str));
//    		lemmas.addAll(MorphLemmatizer.getAllPossibleLemmas(str));
    	WeightedTerm wt = new WeightedTerm(new Term(cluster.toString(),lemmas.toString()),clsScorer.getClusterScore(cluster));
//    	WeightedTerm wt = new WeightedTerm(new Term(cluster.toString(),lemmas.toString()),clsScorer.getDiceScore(s,qg,line.trim(),cluster));
    	responsaScores.add(wt);
    }
    Collections.sort(responsaScores, new SortByWeightName());
    LinkedList<Cluster> responsaClusters = ClusterList.wtListToClusters(responsaScores);
    return responsaClusters;
	}
	
	public LinkedList<Cluster> generateDiceClustering(String targetTerm) throws MorphDistancePrePException, MorphLemmatizerException, CorruptIndexException, IOException, ParseException {
	    List<WeightedTerm> responsaScores =  new LinkedList<WeightedTerm>();
	    m_jung.buildGraph(m_inputTerms.keySet(),m_distance);
	    Set<Set<String>> responsePartition = m_jung.cluster();
	    m_diceClsScorer.initTargetTermData(m_inputTerms, targetTerm);
	    
	    for(Set<String> cluster:responsePartition) {
	    	HashSet<String> lemmas = new HashSet<String>();
	    	for(String str:cluster)
	    		lemmas.addAll(m_morphPre.getLemmasList(str));
	    	WeightedTerm wt = new WeightedTerm(new Term(cluster.toString(),lemmas.toString()),m_diceClsScorer.getDiceScore(cluster));
	    	responsaScores.add(wt);
	    }
	    Collections.sort(responsaScores, new SortByWeightName());
	    LinkedList<Cluster> responsaClusters = ClusterList.wtListToClusters(responsaScores);
	    return responsaClusters;
		}


	public static void main(String[] args) throws ConfigurationFileDuplicateKeyException, ConfigurationException, MorphLemmatizerException, MorphDistancePrePException, IOException, ParseException {
		
		ConfigurationFile conf = new ConfigurationFile(args[0]);
		ConfigurationParams clusterParam = conf.getModuleConfiguration("Clustering");
		String taggerDir = clusterParam.get("tagger-dir");
		Boolean isMila = clusterParam.getBoolean("isMila");
		
		// initialize the lemmatizer for clustering
		MorphLemmatizer.initLemmatizer(taggerDir, isMila);
		
		String confName = clusterParam.get("conf-name");
		Boolean bClusterAll = clusterParam.getBoolean("cluster-all");
		String termsDirName = clusterParam.get("terms-dir") + confName;
		File clsIndexFile = clusterParam.getDirectory("index-dir");
		int termIndex = clusterParam.getInt("term-index");
		int scoreIndex = clusterParam.getInt("score-index");
		String clsQueryField = clusterParam.get("query-field");
		Double scoreThreshold = clusterParam.getDouble("score-threshold");
		int topNum = clusterParam.getInt("top-num");
		String distMeasure = clusterParam.get("distance-measure");
		int morphType = clusterParam.getInt("morph-type");
		String scoreType = clusterParam.get("scorer-type");
		
		if(!bClusterAll) {
			ClusteringGeneration clsGen = new ClusteringGeneration(confName, termsDirName, clsIndexFile, clsQueryField, morphType, termIndex, scoreIndex, topNum, scoreThreshold, distMeasure);
			String clustersDirName = termsDirName + "\\clusters" + topNum + "_" + scoreType + "_" + morphType;
			File clustersDir = new File(clustersDirName);
			if (!clustersDir.exists())
				clustersDir.mkdir();
			File termsDir = new File(termsDirName);
			for (File f:termsDir.listFiles()) {
				if (f.isFile()) {
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clustersDir +"\\" + f.getName().substring(0,f.getName().indexOf(".")) + ".clusters"), "UTF8"));
					clsGen.loadTargetTermData(f);
					LinkedList<Cluster> clusters = clsGen.generateClustering(scoreType);
					for (Cluster cls:clusters)
						writer.write(cls.getTerms()+ "\t" + cls.getLemmas() + "\t" + cls.getScore() +"\n");
					writer.close();
				}
			}
		}
		else {
			File statDir = new File(clusterParam.get("terms-dir"));
			for(File confDir:statDir.listFiles()) {
				System.out.println(confDir);
				if (confDir.isDirectory()) {
					ClusteringGeneration clsGen = new ClusteringGeneration(confDir.getName(), confDir.getAbsolutePath(), clsIndexFile, clsQueryField, morphType, termIndex, scoreIndex, topNum, scoreThreshold, distMeasure);
					if (scoreType.equals("DICE")){
						TargetTerm2Id.loadTargetTerm2IdMapping(new File("C:\\ResponsaNew\\input\\origQuery.txt"));
						clsGen.initDiceScorer(confDir.getName());
					}
					String clustersDirName = confDir.getAbsolutePath() + "\\clusters" + topNum + "_" + scoreType + "_" + morphType;
					File clustersDir = new File(clustersDirName);
					if (!clustersDir.exists())
						clustersDir.mkdir();
					File termsDir = new File(confDir.getAbsolutePath());
					for (File f:termsDir.listFiles()) {
						if (f.isFile()) {
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clustersDir +"\\" + f.getName().substring(0,f.getName().indexOf(".")) + ".clusters"), "UTF8"));
							System.out.println(f.getAbsolutePath());
							clsGen.loadTargetTermData(f);
							LinkedList<Cluster> clusters;
							if (scoreType.equals("DICE"))
								clusters = clsGen.generateDiceClustering(f.getName().substring(0,f.getName().indexOf("_")));
							else
								clusters = clsGen.generateClustering(scoreType);
							for (Cluster cls:clusters)
								writer.write(cls.getTerms()+ "\t" + cls.getLemmas() + "\t" + cls.getScore() +"\n");
							writer.close();
						}
					}
				}
			}
		}
		
				
	}
	
	
	private MorphDistancePreprocessing m_morphPre = null;
	private Distance<CharSequence> m_distance = null;
	private JungClustering m_jung = null;
	private Map<String,Double> m_inputTerms = null;
	private DiceClusterScorer m_diceClsScorer = null;
	private String m_confName;
}
