package mwe.scorers;

public interface MWEScorer {
	public double score(String ngram);
	public String getName();
}
