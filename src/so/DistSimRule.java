package so;


public class DistSimRule {
	
	private int m_leftId;
	private int m_rightId;
	private double m_score;

	public DistSimRule(int left, int right,double score) {
		m_leftId = left;
		m_rightId = right;
		m_score = score;
	}

	public String toString() {
		return m_leftId+"\t"+m_rightId+"\t"+m_score;
	}
	
	public double getScore() {
		return m_score;
	}
	
	public int getLeft(){
		return m_leftId;
	}
	
	public int getRight(){
		return m_leftId;
	}
}
