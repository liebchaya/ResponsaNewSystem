package fo.scorers;

public class TfIdfScorer implements StatScorer{
	

	public double score(long iTotalElementCount, long iElementCount, long iFeatureCount, long iJointCount)
	{ 
		double score = iJointCount / (1.0 + Math.log(iFeatureCount));
		
		if(score <= 0)
			return 0;
		
		return score;
	}

	public String getName() {
		return m_name;
	}

	static private String m_name="TfIdf";

}	

	