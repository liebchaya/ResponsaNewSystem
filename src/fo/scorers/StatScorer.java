package fo.scorers;

public interface StatScorer {
	public double score(long iTotalElementCount, long iElementCount, long iFeatureCount, long iJointCount);
	public String getName();
}
