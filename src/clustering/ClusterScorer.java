package clustering;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

public class ClusterScorer {
	public enum ScorerType
	{
		MAXSCORE,
		AVGSCORE,
		MAXLENGTH,
		SUMSCORE,
		MAXLENGTHLEMMASIZE,
		DICE
	}
	
	public ClusterScorer(Map<String, Double> input, ScorerType type) {
			m_scoreMap = input;
			m_type = type;
		}
	
	public void setScorerType(ScorerType type) {
		m_type = type;
	}
	
	public Double getClusterScore(Set<String> cluster) throws MorphDistancePrePException, MorphLemmatizerException {
		Double score = 0.0;
		if(m_scoreMap == null)
			throw new MorphDistancePrePException("No scores table");
    	double maxScore = Double.MIN_VALUE;
    	double sumScore = 0;
    	for(String str:cluster) {
    		if(!m_scoreMap.containsKey(str))
				throw new MorphDistancePrePException("Missing term in scores table: " + str);
			double curScore = m_scoreMap.get(str);
			if(curScore > maxScore)
				maxScore = curScore;
			sumScore += curScore;
    	}
    	if (m_type.equals(ScorerType.MAXSCORE)) 
			score = maxScore;
		else if (m_type.equals(ScorerType.AVGSCORE)) 
			score = sumScore/cluster.size();
		else if (m_type.equals(ScorerType.MAXLENGTH)) 
			score = maxScore*cluster.size();
		else if (m_type.equals(ScorerType.SUMSCORE)) 
			score = sumScore;
		else if (m_type.equals(ScorerType.MAXLENGTHLEMMASIZE)) 
			score = maxScore*(cluster.size()/(getAllLemmasNum(cluster)+1));
    	return score;
	}
	
	private int getAllLemmasNum(Set<String> cluster) throws MorphLemmatizerException{
		Set<String> allLemmas = new HashSet<String>();
		for(String cls:cluster)
			allLemmas.addAll(MorphLemmatizer.getAllPossibleLemmas(cls));
		return allLemmas.size();
	}
	
	
	private Map<String, Double> m_scoreMap = null;
	private ScorerType m_type = ScorerType.MAXSCORE;
	
	
}
