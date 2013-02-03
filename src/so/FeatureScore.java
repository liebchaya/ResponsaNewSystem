package so;


public class FeatureScore implements Comparable<FeatureScore> {

	private int m_featureId;
	private double m_score;
	
	public FeatureScore(int featureId, double score) {
		m_featureId = featureId;
		m_score = score;
	}

	@Override
	public int compareTo(FeatureScore o) {
		
		if(m_score>o.m_score)
			return -1;
		else if(m_score<o.m_score)
			return 1;
		return 0;
	}
	
	public double getScore() {
		return m_score;
	}
	
	public int getFeatureId() {
		return m_featureId;
	}

}

