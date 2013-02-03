package mwe.scorers;

public interface MWEScorer {
	public double score(String ngram);
	public boolean exist(String ngram);
	public String getName();
}
