package fo.scorers;

public class DiceScorer implements StatScorer{
	

	public double score(long iTotalElementCount, long iElementCount, long iFeatureCount, long iJointCount)
	{
		//dice = (2*c.doc_clicks)/(a.doc_freq+b.doc_freq) 
		double score = ((double)((2*iJointCount)))/((double)(iElementCount+iFeatureCount));
		
		if(score <= 0)
			return 0;
		
		return score;
	}

	public String getName() {
		return m_name;
	}

	static private String m_name="Dice";

}	

	