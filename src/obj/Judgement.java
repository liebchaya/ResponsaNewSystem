package obj;


public class Judgement {

	private String m_query = null;
	private String m_term = null; 	//expansion term;
	private int m_judge = -99;
	private boolean m_hasJudge = false;
	
	
	public Judgement(String query, String text){
		m_query = query;
		m_term = text;
	}
	
	public Judgement(String query, String text, int judge){
		m_query = query;
		m_term = text;
		m_judge = judge;
		if (m_judge!=-99)
			m_hasJudge = true;
	}
	
	public Judgement(String query,  int judge){
		m_query = query;
		m_judge = judge;
		if (m_judge!=-99)
			m_hasJudge = true;
		
	}
	
	public Judgement(String query){
		m_query = query;
		m_term = "";
		m_judge = -99;
		
	}
	
		
	public String toString(){
		return m_query+"\t"+m_term+"\t"+m_judge+"\t";
	}
	
	public void setJudgement(int judge){
		m_judge = judge;
		m_hasJudge = true;
	}
	public String getQuery(){
		return m_query;
	}
	
	public String getText(){
		return m_term;
	}

	public int getJudge(){
		return m_judge;
	}
	
	
	

	public boolean equals(Object i){
		Judgement j =(Judgement)i;
		//term judgement
		if (m_query.equals(j.getQuery()) && m_term.equals(j.getText()))
			return true;
		else return false;
	}

	public boolean hasJudgement() {
		if (m_judge==-99)
			return false;
		else return true;
	}
	
	public boolean isPositiveJudge(){
	if (m_judge>0)
		return true;
	else return false;
	}
	
	public boolean isNegativeJudge(){
		if (m_judge<=0 && this.hasJudgement())
			return true;
		else return false;
	}
}
